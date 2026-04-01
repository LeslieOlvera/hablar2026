const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { pool } = require("../db");
const { JWT_SECRET } = require("../middlewares/auth");

function signToken(payload) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: "7d" });
}

async function signupPaciente(req, res) {
  try {
    // 1. Añadimos fk_idCedula a la destructuración del body
    const { nombreP, correoP, contrasenaP, estrella, fk_idCedula } = req.body;

    // 2. Validamos que la cédula del terapeuta también esté presente
    if (!nombreP || !correoP || !contrasenaP || !fk_idCedula) {
      return res.status(400).json({ 
        message: "Faltan campos obligatorios: nombreP, correoP, contrasenaP, fk_idCedula" 
      });
    }

    const hash = await bcrypt.hash(contrasenaP, 10);

    // 3. Actualizamos el SQL para incluir fk_idCedula
    const sql = `
      INSERT INTO Paciente (nombreP, correoP, contrasenaP, estrella, fk_idCedula)
      VALUES (?, ?, ?, ?, ?)
    `;

    const [result] = await pool.execute(sql, [
   nombreP, 
  correoP, 
  hash, 
  estrella ?? 0, 
  fk_idCedula
    ]);

    return res.status(201).json({ 
      message: "Paciente registrado exitosamente", 
      id_paciente: result.insertId 
    });

  } catch (err) {
    // Error si el correo ya existe
    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ message: "El correoP ya existe" });
    }
    // Error si la cédula del terapeuta no existe en la tabla Terapeuta
    if (err.code === "ER_NO_REFERENCED_ROW_2") {
      return res.status(400).json({ message: "La cédula del terapeuta ingresada no es válida o no existe" });
    }
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

async function loginPaciente(req, res) {
  try {
    const { correoP, contrasenaP } = req.body;

    if (!correoP || !contrasenaP) {
      return res.status(400).json({ message: "Faltan campos: correoP, contrasenaP" });
    }

    // 4. Seleccionamos también fk_idCedula para saber quién es su terapeuta al entrar
    const [rows] = await pool.execute(
      "SELECT id_paciente, nombreP, correoP, contrasenaP, estrella, fk_idCedula FROM Paciente WHERE correoP = ? LIMIT 1",
      [correoP]
    );

    if (rows.length === 0) return res.status(401).json({ message: "Credenciales invalidas" });

    const paciente = rows[0];
    const ok = await bcrypt.compare(contrasenaP, paciente.contrasenaP);
    if (!ok) return res.status(401).json({ message: "Credenciales invalidas" });

    const token = signToken({ 
      role: "paciente", 
      id: paciente.id_paciente, 
      correo: paciente.correoP 
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
        fk_idCedula: paciente.fk_idCedula // Enviamos la cédula de vuelta al frontend
      },
    });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

module.exports = { signupPaciente, loginPaciente };