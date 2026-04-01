const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { pool } = require("../db");
const { JWT_SECRET } = require("../middlewares/auth");

function signToken(payload) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: "7d" });
}

async function signupTerapeuta(req, res) {
  try {
    // 1. Agregamos idCedula a la extracción de datos
    const { idCedula, nombreT, correoT, contrasenaT } = req.body;

    // 2. Validación: idCedula ahora es obligatoria
    if (!idCedula || !nombreT || !correoT || !contrasenaT) {
      return res.status(400).json({ 
        message: "Faltan campos: idCedula, nombreT, correoT, contrasenaT" 
      });
    }

    const hash = await bcrypt.hash(contrasenaT, 10);

    // 3. SQL: Incluimos idCedula en el INSERT
    const sql = `
      INSERT INTO Terapeuta (idCedula, nombreT, correoT, contrasenaT)
      VALUES (?, ?, ?, ?)
    `;

    // 4. Ejecución: idCedula es la llave primaria manual
    await pool.execute(sql, [idCedula, nombreT, correoT, hash]);

    return res.status(201).json({ 
      message: "Terapeuta registrado con éxito", 
      idCedula: idCedula 
    });

  } catch (err) {
    console.error("=== ERROR EN REGISTRO TERAPEUTA ===");
    console.error(err);

    // Error por si la cédula o el correo ya existen
    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ 
        message: "El correo o la cédula ya están registrados" 
      });
    }
    
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

    const token = signToken({ 
      role: "terapeuta", 
      id: terapeuta.idCedula, 
      correo: terapeuta.correoT 
    });

    return res.json({
      message: "Login OK",
      role: "terapeuta",
      token,
      terapeuta: { 
        idCedula: terapeuta.idCedula, 
        nombreT: terapeuta.nombreT, 
        correoT: terapeuta.correoT 
      },
    });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

module.exports = { signupTerapeuta, loginTerapeuta };