const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { pool } = require("../db");
const { JWT_SECRET } = require("../middlewares/auth");
const { enviarCodigoRecuperacion } = require("../services/email.service");

// Objeto para guardar códigos temporalmente en memoria
const codesCache = {};

function signToken(payload) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: "7d" });
}

function normalizarCorreo(correo) {
  return String(correo || "").trim().toLowerCase();
}

function generarCodigoRecuperacion() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

async function signupPaciente(req, res) {
  try {
    const { nombreP, correoP, contrasenaP, estrella, fk_idCedula } = req.body;

    if (!nombreP || !correoP || !contrasenaP || !fk_idCedula) {
      return res.status(400).json({
        message: "Faltan campos: nombreP, correoP, contrasenaP o fk_idCedula",
      });
    }

    const correoNormalizado = normalizarCorreo(correoP);
    const hash = await bcrypt.hash(contrasenaP, 10);

    const sql = `
      INSERT INTO paciente (nombreP, correoP, contrasenaP, estrella, fk_idCedula)
      VALUES (?, ?, ?, ?, ?)
    `;

    const [result] = await pool.execute(sql, [
      nombreP,
      correoNormalizado,
      hash,
      estrella || 0,
      fk_idCedula,
    ]);

    return res.status(201).json({
      message: "Paciente registrado",
      id_paciente: result.insertId,
    });
  } catch (err) {
    console.error("ERROR EN REGISTRO PACIENTE:", err);

    if (err.code === "ER_NO_REFERENCED_ROW_2") {
      return res.status(400).json({
        message: "La cédula del terapeuta no existe. Registra al terapeuta primero.",
      });
    }

    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({
        message: "El correoP ya existe",
      });
    }

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

async function loginPaciente(req, res) {
  try {
    const correoP = normalizarCorreo(req.body.correoP);
    const { contrasenaP } = req.body;

    if (!correoP || !contrasenaP) {
      return res.status(400).json({
        message: "Faltan campos: correoP, contrasenaP",
      });
    }

    const sql = `
      SELECT
        p.id_paciente,
        p.nombreP,
        p.correoP,
        p.contrasenaP,
        p.estrella,
        p.fk_idCedula,
        t.nombreT
      FROM paciente p
      INNER JOIN terapeuta t ON p.fk_idCedula = t.idCedula
      WHERE LOWER(TRIM(p.correoP)) = ?
      LIMIT 1
    `;

    const [rows] = await pool.execute(sql, [correoP]);

    if (rows.length === 0) {
      return res.status(401).json({
        message: "Credenciales invalidas",
      });
    }

    const paciente = rows[0];
    const ok = await bcrypt.compare(contrasenaP, paciente.contrasenaP);

    if (!ok) {
      return res.status(401).json({
        message: "Credenciales invalidas",
      });
    }

    const token = signToken({
      role: "paciente",
      id: paciente.id_paciente,
      correo: paciente.correoP,
      terapeuta: paciente.fk_idCedula,
    });

    return res.json({
      message: "Login OK",
      role: "paciente",
      token,
      paciente: {
        id_paciente: paciente.id_paciente,
        nombreP: paciente.nombreP,
        correoP: paciente.correoP,
        estrella: paciente.estrella,
        fk_idCedula: paciente.fk_idCedula,
        nombreT: paciente.nombreT,
      },
    });
  } catch (err) {
    console.error("ERROR EN LOGIN PACIENTE:", err);

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

// ======================================================
// ENVIAR CÓDIGO DE RECUPERACIÓN A PACIENTE
// ======================================================
async function sendCodePaciente(req, res) {
  try {
    const correoP = normalizarCorreo(req.body.correoP);

    if (!correoP) {
      return res.status(400).json({
        message: "El correoP es obligatorio.",
      });
    }

    const [rows] = await pool.execute(
      `
      SELECT id_paciente, nombreP, correoP
      FROM paciente
      WHERE LOWER(TRIM(correoP)) = ?
      LIMIT 1
      `,
      [correoP]
    );

    if (rows.length === 0) {
      return res.status(404).json({
        message: "El correo no está registrado como paciente.",
      });
    }

    const paciente = rows[0];
    const code = generarCodigoRecuperacion();

    codesCache[correoP] = {
      code,
      expiresAt: Date.now() + 10 * 60 * 1000,
    };

    await enviarCodigoRecuperacion({
      to: paciente.correoP,
      nombre: paciente.nombreP,
      codigo: code,
      tipoUsuario: "paciente",
    });

    console.log(`Código de recuperación enviado a paciente: ${paciente.correoP}`);

    return res.json({
      success: true,
      message: "Código enviado al correo registrado.",
    });
  } catch (err) {
    console.error("ERROR sendCodePaciente:", err);

    return res.status(500).json({
      message: "Error al enviar el código de recuperación.",
      error: err.message,
    });
  }
}

// ======================================================
// VERIFICAR CÓDIGO DE PACIENTE
// ======================================================
async function verifyCodePaciente(req, res) {
  try {
    const correoP = normalizarCorreo(req.body.correoP);
    const code = String(req.body.code || "").trim();

    if (!correoP || !code) {
      return res.status(400).json({
        message: "correoP y code son obligatorios.",
      });
    }

    const registro = codesCache[correoP];

    if (!registro) {
      return res.status(400).json({
        message: "Código no solicitado o expirado.",
      });
    }

    if (Date.now() > registro.expiresAt) {
      delete codesCache[correoP];

      return res.status(400).json({
        message: "El código expiró. Solicita uno nuevo.",
      });
    }

    if (registro.code !== code) {
      return res.status(400).json({
        message: "Código incorrecto.",
      });
    }

    return res.json({
      success: true,
      message: "Código verificado correctamente.",
    });
  } catch (err) {
    console.error("ERROR verifyCodePaciente:", err);

    return res.status(500).json({
      message: "Error interno del servidor.",
      error: err.message,
    });
  }
}

// ======================================================
// RESTABLECER CONTRASEÑA DE PACIENTE
// ======================================================
async function resetPasswordPaciente(req, res) {
  try {
    const correoP = normalizarCorreo(req.body.correoP);
    const code = String(req.body.code || "").trim();
    const nuevaContrasena = req.body.nuevaContrasena || "";

    if (!correoP || !code || !nuevaContrasena) {
      return res.status(400).json({
        message: "correoP, code y nuevaContrasena son obligatorios.",
      });
    }

    const registro = codesCache[correoP];

    if (!registro) {
      return res.status(401).json({
        message: "Sesión de recuperación inválida o expirada.",
      });
    }

    if (Date.now() > registro.expiresAt) {
      delete codesCache[correoP];

      return res.status(401).json({
        message: "El código expiró. Solicita uno nuevo.",
      });
    }

    if (registro.code !== code) {
      return res.status(401).json({
        message: "Código incorrecto.",
      });
    }

    const hash = await bcrypt.hash(nuevaContrasena, 10);

    const [result] = await pool.execute(
      `
      UPDATE paciente
      SET contrasenaP = ?
      WHERE LOWER(TRIM(correoP)) = ?
      `,
      [hash, correoP]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        message: "No se encontró el paciente para actualizar la contraseña.",
      });
    }

    delete codesCache[correoP];

    return res.json({
      success: true,
      message: "Contraseña actualizada.",
    });
  } catch (err) {
    console.error("ERROR resetPasswordPaciente:", err);

    return res.status(500).json({
      message: "Error al actualizar contraseña.",
      error: err.message,
    });
  }
}

module.exports = {
  signupPaciente,
  loginPaciente,
  sendCodePaciente,
  verifyCodePaciente,
  resetPasswordPaciente,
};