const express = require("express");
const mysql = require("mysql2/promise");
const bcrypt = require("bcrypt");
const cors = require("cors"); // Agregado para evitar bloqueos de red

const app = express();
app.use(express.json());
app.use(cors()); // Permite que el celular se conecte sin bloqueos

// ====== CONFIG DB ======
const DB_HOST = "127.0.0.1";
const DB_PORT = 3306;
const DB_USER = "root";
const DB_PASS = "maria123?";
const DB_NAME = "app_tshusuarios";

const API_PORT = 3000;

const pool = mysql.createPool({
  host: DB_HOST,
  port: DB_PORT,
  user: DB_USER,
  password: DB_PASS,
  database: DB_NAME,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
});

// ====== PROBAR CONEXIÓN AL ARRANCAR ======
(async () => {
  try {
    const conn = await pool.getConnection();
    await conn.ping();
    conn.release();
    console.log("---------------------------------");
    console.log("Base de Datos: app_tshusuarios ✅");
    console.log("Estado: Conectado con éxito");
    console.log("---------------------------------");
  } catch (err) {
    console.error("❌ ERROR DE CONEXIÓN A LA DB:", err.message);
  }
})();

// =====================================================
//                AUTH: TERAPEUTA
// =====================================================

app.post("/auth/terapeuta/signup", async (req, res) => {
  console.log("\n[PETICIÓN] POST /auth/terapeuta/signup");
  console.log("Datos recibidos:", req.body);

  try {
    const { idCedula, nombreT, correoT, contrasenaT } = req.body;

    // Validación manual
    if (!idCedula || !nombreT || !correoT || !contrasenaT) {
      console.log("❌ Error: Faltan campos en el JSON");
      return res.status(400).json({ message: "Faltan campos obligatorios" });
    }

    const hash = await bcrypt.hash(contrasenaT, 10);

    const sql = `
      INSERT INTO Terapeuta (idCedula, nombreT, correoT, contrasenaT)
      VALUES (?, ?, ?, ?)
    `;

    // Convertimos idCedula a Number por si llega como String desde Android
    await pool.execute(sql, [Number(idCedula), nombreT, correoT, hash]);

    console.log("✅ Terapeuta registrado con éxito:", idCedula);
    return res.status(201).json({
      message: "Terapeuta registrado exitosamente",
      idCedula: idCedula,
    });

  } catch (err) {
    console.error("❌ ERROR EN SIGNUP TERAPEUTA:");
    console.error("Código:", err.code);
    console.error("Mensaje:", err.message);

    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ message: "La cédula o el correo ya existen" });
    }
    return res.status(500).json({ error: err.code, details: err.message });
  }
});

app.post("/auth/terapeuta/login", async (req, res) => {
  console.log("\n[PETICIÓN] POST /auth/terapeuta/login");
  try {
    const { correoT, contrasenaT } = req.body;

    if (!correoT || !contrasenaT) {
      return res.status(400).json({ message: "Faltan campos" });
    }

    const [rows] = await pool.execute(
      "SELECT idCedula, nombreT, correoT, contrasenaT FROM Terapeuta WHERE correoT = ? LIMIT 1",
      [correoT]
    );

    if (rows.length === 0) {
      console.log("❌ Intento de login: Correo no encontrado");
      return res.status(401).json({ message: "Credenciales invalidas" });
    }

    const terapeuta = rows[0];
    const ok = await bcrypt.compare(contrasenaT, terapeuta.contrasenaT);

    if (!ok) {
      console.log("❌ Intento de login: Contraseña incorrecta");
      return res.status(401).json({ message: "Credenciales invalidas" });
    }

    console.log("✅ Login exitoso:", correoT);
    return res.json({
      message: "Login OK",
      role: "terapeuta",
      terapeuta: {
        idCedula: terapeuta.idCedula,
        nombreT: terapeuta.nombreT,
        correoT: terapeuta.correoT,
      },
    });
  } catch (err) {
    console.error("❌ ERROR EN LOGIN TERAPEUTA:", err);
    return res.status(500).json({ error: err.code, message: err.message });
  }
});

// =====================================================
//                AUTH: PACIENTE
// =====================================================

app.post("/auth/paciente/signup", async (req, res) => {
  console.log("\n[PETICIÓN] POST /auth/paciente/signup");
  try {
    const { nombreP, correoP, contrasenaP, estrella, fk_idCedula } = req.body;

    if (!nombreP || !correoP || !contrasenaP || !fk_idCedula) {
      return res.status(400).json({ message: "Faltan campos obligatorios" });
    }

    const hash = await bcrypt.hash(contrasenaP, 10);

    const sql = `
      INSERT INTO Paciente (nombreP, correoP, contrasenaP, estrella, fk_idCedula)
      VALUES (?, ?, ?, ?, ?)
    `;

    const [result] = await pool.execute(sql, [
      nombreP, 
      correoP, 
      hash, 
      estrella || 0, 
      Number(fk_idCedula)
    ]);

    console.log("✅ Paciente registrado con éxito:", correoP);
    return res.status(201).json({
      message: "Paciente registrado exitosamente",
      id_paciente: result.insertId,
    });
  } catch (err) {
    console.error("❌ ERROR EN SIGNUP PACIENTE:", err.message);
    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ message: "El correo del paciente ya existe" });
    }
    if (err.code === "ER_NO_REFERENCED_ROW_2") {
      return res.status(400).json({ message: "La cédula del terapeuta no existe" });
    }
    return res.status(500).json({ error: err.code, message: err.message });
  }
});

// =====================================================
//                INICIO DEL SERVIDOR
// =====================================================
app.get("/", (req, res) => res.send("API HablaR 🚀 Servidor Activo"));

app.listen(API_PORT, "0.0.0.0", () => { // "0.0.0.0" permite conexiones externas (celular)
  console.log(`🚀 API corriendo en puerto ${API_PORT}`);
  console.log(`Escuchando peticiones en red local...`);
});