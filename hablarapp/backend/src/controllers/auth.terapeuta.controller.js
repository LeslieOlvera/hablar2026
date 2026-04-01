const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { pool } = require("../db");

// Función para registrar un Terapeuta
async function signupTerapeuta(req, res) {
  try {
    // 1. Extraer datos del cuerpo de la petición
    const { idCedula, nombreT, correoT, contrasenaT } = req.body;

    // 2. Validación de campos vacíos
    if (!idCedula || !nombreT || !correoT || !contrasenaT) {
      return res.status(400).json({ 
        message: "Faltan campos obligatorios: idCedula, nombreT, correoT, contrasenaT" 
      });
    }

    // 3. Validación de tipo de dato: La cédula debe ser numérica
    if (isNaN(idCedula)) {
      return res.status(400).json({ message: "La cédula debe ser un valor numérico" });
    }

    // 4. Encriptar la contraseña (Costo de 10 es el estándar seguro)
    const hash = await bcrypt.hash(contrasenaT, 10);

    // 5. Preparar el SQL (Asegúrate de que la tabla se llame 'Terapeuta')
    const sql = `
      INSERT INTO Terapeuta (idCedula, nombreT, correoT, contrasenaT) 
      VALUES (?, ?, ?, ?)
    `;

    // 6. Ejecución en la base de datos
    // Usamos parseInt para asegurar que llegue como número a MySQL
    await pool.execute(sql, [parseInt(idCedula), nombreT, correoT, hash]);

    // Respuesta de éxito
    return res.status(201).json({ 
      message: "Terapeuta registrado con éxito", 
      idCedula: idCedula 
    });

  } catch (err) {
    // --- ESTO SE VERÁ EN TU TERMINAL DE NODE.JS ---
    console.error("\n[!] ERROR EN REGISTRO TERAPEUTA:");
    console.error("Código de error MySQL:", err.code);
    console.error("Mensaje detallado:", err.message);
    console.error("-----------------------------------\n");

    // Manejo de errores específicos para el cliente (Android)
    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ 
        message: "El correo o la cédula ya están registrados en el sistema" 
      });
    }

    // Error genérico 500 con el código de MySQL para depurar
    return res.status(500).json({ 
      error: err.code, 
      message: "Error interno del servidor. Revisa la terminal." 
    });
  }
}

module.exports = { signupTerapeuta };