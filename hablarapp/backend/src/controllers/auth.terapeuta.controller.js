const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { pool } = require("../db");
const { JWT_SECRET } = require("../middlewares/auth");

function signToken(payload) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: "7d" });
}

async function signupTerapeuta(req, res) {
  try {
    const { nombreT, correoT, contrasenaT } = req.body;

    if (!nombreT || !correoT || !contrasenaT) {
      return res.status(400).json({ message: "Faltan campos: nombreT, correoT, contrasenaT" });
    }

    const hash = await bcrypt.hash(contrasenaT, 10);

    const sql = `
      INSERT INTO Terapeuta (nombreT, correoT, contrasenaT)
      VALUES (?, ?, ?)
    `;

    const [result] = await pool.execute(sql, [nombreT, correoT, hash]);

    return res.status(201).json({ message: "Terapeuta registrado", idCedula: result.insertId });
  } catch (err) {
    if (err.code === "ER_DUP_ENTRY") return res.status(409).json({ message: "El correoT ya existe" });
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

async function loginTerapeuta(req, res) {
  try {
    const { correoT, contrasenaT } = req.body;

    if (!correoT || !contrasenaT) {
      return res.status(400).json({ message: "Faltan campos: correoT, contrasenaT" });
    }

    const [rows] = await pool.execute(
      "SELECT idCedula, nombreT, correoT, contrasenaT FROM Terapeuta WHERE correoT = ? LIMIT 1",
      [correoT]
    );

    if (rows.length === 0) return res.status(401).json({ message: "Credenciales invalidas" });

    const terapeuta = rows[0];
    const ok = await bcrypt.compare(contrasenaT, terapeuta.contrasenaT);
    if (!ok) return res.status(401).json({ message: "Credenciales invalidas" });

    const token = signToken({ role: "terapeuta", id: terapeuta.idCedula, correo: terapeuta.correoT });

    return res.json({
      message: "Login OK",
      role: "terapeuta",
      token,
      terapeuta: { idCedula: terapeuta.idCedula, nombreT: terapeuta.nombreT, correoT: terapeuta.correoT },
    });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

module.exports = { signupTerapeuta, loginTerapeuta };
