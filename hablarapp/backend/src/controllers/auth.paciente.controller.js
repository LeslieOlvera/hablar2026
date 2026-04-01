const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { pool } = require("../db");
const { JWT_SECRET } = require("../middlewares/auth");

function signToken(payload) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: "7d" });
}

async function signupPaciente(req, res) {
  try {
    // 1. Agregamos fk_idCedula que viene desde Android
    const { nombreP, correoP, contrasenaP, estrella, fk_idCedula } = req.body;

    // 2. Validación: fk_idCedula es OBLIGATORIA por tu CONSTRAINT en SQL
    if (!nombreP || !correoP || !contrasenaP || !fk_idCedula) {
      return res.status(400).json({ 
        message: "Faltan campos: nombreP, correoP, contrasenaP o fk_idCedula" 
      });
    }

    const hash = await bcrypt.hash(contrasenaP, 10);

    // 3. SQL con la columna fk_idCedula incluida
    const sql = `
      INSERT INTO Paciente (nombreP, correoP, contrasenaP, estrella, fk_idCedula)
      VALUES (?, ?, ?, ?, ?)
    `;

    // 4. Ejecución (id_paciente no se manda porque es AUTO_INCREMENT)
    const [result] = await pool.execute(sql, [
      nombreP, 
      correoP, 
      hash, 
      estrella || 0, 
      fk_idCedula
    ]);

    return res.status(201).json({ 
      message: "Paciente registrado", 
      id_paciente: result.insertId 
    });

  } catch (err) {
    console.error("ERROR EN REGISTRO PACIENTE:", err);

    // Error específico de Llave Foránea (si la cédula no existe en la tabla Terapeuta)
    if (err.code === "ER_NO_REFERENCED_ROW_2") {
      return res.status(400).json({ 
        message: "La cédula del terapeuta no existe. Registra al terapeuta primero." 
      });
    }

    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ message: "El correoP ya existe" });
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

    // Agregamos fk_idCedula al SELECT para saber quién es su terapeuta al loguearse
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
        correo: paciente.correoP,
        terapeuta: paciente.fk_idCedula 
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
        fk_idCedula: paciente.fk_idCedula
      },
    });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

module.exports = { signupPaciente, loginPaciente };