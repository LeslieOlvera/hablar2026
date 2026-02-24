const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { pool } = require("../db");
const { JWT_SECRET } = require("../middlewares/auth");

function signToken(payload) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: "7d" });
}

async function signupPaciente(req, res) {
  try {
    const { nombreP, correoP, contrasenaP, estrella } = req.body;

    if (!nombreP || !correoP || !contrasenaP) {
      return res.status(400).json({ message: "Faltan campos: nombreP, correoP, contrasenaP" });
    }

    const hash = await bcrypt.hash(contrasenaP, 10);

    const sql = `
      INSERT INTO Paciente (nombreP, correoP, contrasenaP, estrella)
      VALUES (?, ?, ?, ?)
    `;

    const [result] = await pool.execute(sql, [nombreP, correoP, hash, estrella ?? null]);

    return res.status(201).json({ message: "Paciente registrado", id_paciente: result.insertId });
  } catch (err) {
    if (err.code === "ER_DUP_ENTRY") return res.status(409).json({ message: "El correoP ya existe" });
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

async function loginPaciente(req, res) {
  try {
    const { correoP, contrasenaP } = req.body;

    if (!correoP || !contrasenaP) {
      return res.status(400).json({ message: "Faltan campos: correoP, contrasenaP" });
    }

    const [rows] = await pool.execute(
      "SELECT id_paciente, nombreP, correoP, contrasenaP, estrella FROM Paciente WHERE correoP = ? LIMIT 1",
      [correoP]
    );

    if (rows.length === 0) return res.status(401).json({ message: "Credenciales invalidas" });

    const paciente = rows[0];
    const ok = await bcrypt.compare(contrasenaP, paciente.contrasenaP);
    if (!ok) return res.status(401).json({ message: "Credenciales invalidas" });

    const token = signToken({ role: "paciente", id: paciente.id_paciente, correo: paciente.correoP });

    return res.json({
      message: "Login OK",
      role: "paciente",
      token,
      paciente: {
        id_paciente: paciente.id_paciente,
        nombreP: paciente.nombreP,
        correoP: paciente.correoP,
        estrella: paciente.estrella,
      },
    });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

module.exports = { signupPaciente, loginPaciente };
