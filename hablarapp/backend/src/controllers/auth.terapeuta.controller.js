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

async function signupTerapeuta(req, res) {
  try {
    const { idCedula, nombreT, correoT, contrasenaT } = req.body;

    if (!idCedula || !nombreT || !correoT || !contrasenaT) {
      return res.status(400).json({
        message: "Faltan campos: idCedula, nombreT, correoT, contrasenaT",
      });
    }

    const correoNormalizado = normalizarCorreo(correoT);
    const hash = await bcrypt.hash(contrasenaT, 10);

    const sql = `
      INSERT INTO terapeuta (idCedula, nombreT, correoT, contrasenaT)
      VALUES (?, ?, ?, ?)
    `;

    await pool.execute(sql, [
      idCedula,
      nombreT,
      correoNormalizado,
      hash,
    ]);

    return res.status(201).json({
      message: "Terapeuta registrado con éxito",
      idCedula,
    });
  } catch (err) {
    console.error("=== ERROR EN REGISTRO TERAPEUTA ===");
    console.error(err);

    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({
        message: "El correo o la cédula ya están registrados",
      });
    }

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

async function loginTerapeuta(req, res) {
  try {
    const correoT = normalizarCorreo(req.body.correoT);
    const { contrasenaT } = req.body;

    if (!correoT || !contrasenaT) {
      return res.status(400).json({
        message: "Faltan campos: correoT, contrasenaT",
      });
    }

    const [rows] = await pool.execute(
      `
      SELECT idCedula, nombreT, correoT, contrasenaT
      FROM terapeuta
      WHERE LOWER(TRIM(correoT)) = ?
      LIMIT 1
      `,
      [correoT]
    );

    if (rows.length === 0) {
      return res.status(401).json({
        message: "Credenciales invalidas",
      });
    }

    const terapeuta = rows[0];
    const ok = await bcrypt.compare(contrasenaT, terapeuta.contrasenaT);

    if (!ok) {
      return res.status(401).json({
        message: "Credenciales invalidas",
      });
    }

    const token = signToken({
      role: "terapeuta",
      id: terapeuta.idCedula,
      correo: terapeuta.correoT,
    });

    return res.json({
      message: "Login OK",
      role: "terapeuta",
      token,
      terapeuta: {
        idCedula: terapeuta.idCedula,
        nombreT: terapeuta.nombreT,
        correoT: terapeuta.correoT,
      },
    });
  } catch (err) {
    console.error("ERROR EN LOGIN TERAPEUTA:", err);

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

// ======================================================
// ENVIAR CÓDIGO DE RECUPERACIÓN A TERAPEUTA
// ======================================================
async function sendCodeTerapeuta(req, res) {
  try {
    const correoT = normalizarCorreo(req.body.correoT);

    if (!correoT) {
      return res.status(400).json({
        message: "El correoT es obligatorio.",
      });
    }

    const [rows] = await pool.execute(
      `
      SELECT idCedula, nombreT, correoT
      FROM terapeuta
      WHERE LOWER(TRIM(correoT)) = ?
      LIMIT 1
      `,
      [correoT]
    );

    if (rows.length === 0) {
      return res.status(404).json({
        message: "El correo no está registrado como terapeuta.",
      });
    }

    const terapeuta = rows[0];
    const code = generarCodigoRecuperacion();

    codesCache[correoT] = {
      code,
      expiresAt: Date.now() + 10 * 60 * 1000,
    };

    await enviarCodigoRecuperacion({
      to: terapeuta.correoT,
      nombre: terapeuta.nombreT,
      codigo: code,
      tipoUsuario: "terapeuta",
    });

    console.log(`Código de recuperación enviado a terapeuta: ${terapeuta.correoT}`);

    return res.json({
      success: true,
      message: "Código enviado al correo registrado.",
    });
  } catch (err) {
    console.error("ERROR sendCodeTerapeuta:", err);

    return res.status(500).json({
      message: "Error al enviar el código de recuperación.",
      error: err.message,
    });
  }
}

// ======================================================
// VERIFICAR CÓDIGO DE TERAPEUTA
// ======================================================
async function verifyCodeTerapeuta(req, res) {
  try {
    const correoT = normalizarCorreo(req.body.correoT);
    const code = String(req.body.code || "").trim();

    if (!correoT || !code) {
      return res.status(400).json({
        message: "correoT y code son obligatorios.",
      });
    }

    const registro = codesCache[correoT];

    if (!registro) {
      return res.status(400).json({
        message: "Código no solicitado o expirado.",
      });
    }

    if (Date.now() > registro.expiresAt) {
      delete codesCache[correoT];

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
    console.error("ERROR verifyCodeTerapeuta:", err);

    return res.status(500).json({
      message: "Error interno del servidor.",
      error: err.message,
    });
  }
}

// ======================================================
// RESTABLECER CONTRASEÑA DE TERAPEUTA
// ======================================================
async function resetPasswordTerapeuta(req, res) {
  try {
    const correoT = normalizarCorreo(req.body.correoT);
    const code = String(req.body.code || "").trim();
    const nuevaContrasena = req.body.nuevaContrasena || "";

    if (!correoT || !code || !nuevaContrasena) {
      return res.status(400).json({
        message: "correoT, code y nuevaContrasena son obligatorios.",
      });
    }

    const registro = codesCache[correoT];

    if (!registro) {
      return res.status(401).json({
        message: "Sesión de recuperación inválida o expirada.",
      });
    }

    if (Date.now() > registro.expiresAt) {
      delete codesCache[correoT];

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
      UPDATE terapeuta
      SET contrasenaT = ?
      WHERE LOWER(TRIM(correoT)) = ?
      `,
      [hash, correoT]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        message: "No se encontró el terapeuta para actualizar la contraseña.",
      });
    }

    delete codesCache[correoT];

    return res.json({
      success: true,
      message: "Contraseña actualizada.",
    });
  } catch (err) {
    console.error("ERROR resetPasswordTerapeuta:", err);

    return res.status(500).json({
      message: "Error al actualizar contraseña.",
      error: err.message,
    });
  }
}

module.exports = {
  signupTerapeuta,
  loginTerapeuta,
  sendCodeTerapeuta,
  verifyCodeTerapeuta,
  resetPasswordTerapeuta,
};