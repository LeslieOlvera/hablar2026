const bcrypt = require("bcrypt");
const { pool } = require("../db");

async function signupTerapeuta(req, res) {
  // 1. Mensaje de vida inmediato
  console.log(">>> LLEGÓ UNA PETICIÓN DE REGISTRO TERAPEUTA");
  console.log("Body recibido:", req.body);

  try {
    const { idCedula, nombreT, correoT, contrasenaT } = req.body;

    // Validación ultra simple
    if (!idCedula || !nombreT || !correoT || !contrasenaT) {
       console.error("Faltan datos en el body");
       return res.status(400).json({ message: "Faltan datos" });
    }

    const hash = await bcrypt.hash(contrasenaT, 10);

    const sql = `INSERT INTO Terapeuta (idCedula, nombreT, correoT, contrasenaT) VALUES (?, ?, ?, ?)`;
    
    // Ejecución con log
    console.log("Intentando insertar en MySQL...");
    await pool.execute(sql, [parseInt(idCedula), nombreT, correoT, hash]);
    
    console.log("¡Inserción exitosa!");
    return res.status(201).json({ message: "OK", id: idCedula });

  } catch (err) {
    // ESTO TIENE QUE SALIR EN LA TERMINAL SÍ O SÍ
    console.log("**************************************");
    console.log("ERROR DETECTADO:", err);
    console.log("CÓDIGO:", err.code);
    console.log("MENSAJE:", err.message);
    console.log("**************************************");

    return res.status(500).json({ 
      error: "Error interno", 
      sqlError: err.code,
      details: err.message 
    });
  }
}

module.exports = { signupTerapeuta };