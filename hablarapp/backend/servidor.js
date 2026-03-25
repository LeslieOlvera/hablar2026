const express = require("express");
const mysql = require("mysql2/promise");
const bcrypt = require("bcrypt");

const app = express();
app.use(express.json());

// ====== CONFIG DB (ajusta) ======
const DB_HOST = "127.0.0.1";
const DB_PORT = 3306;
const DB_USER = "root";
const DB_PASS = "maria123?";
const DB_NAME = "app_tshusuarios";

// ====== API PORT (NO 3306) ======
const API_PORT = 3000;

// Pool (mejor que createConnection)
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

// ====== PROBAR CONEXION ======
(async () => {
  try {
    const conn = await pool.getConnection();
    await conn.ping();
    conn.release();
    console.log("Conectado a MySQL ✅");
  } catch (err) {
    console.error("Error conectando a MySQL:", err.code, err.message);
  }
})();

// =====================================================
//                 AUTH: TERAPEUTA
// =====================================================

// Registro terapeuta (crea hash bcrypt)
app.post("/auth/terapeuta/signup", async (req, res) => {
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

    return res.status(201).json({
      message: "Terapeuta registrado",
      idCedula: result.insertId,
    });
  } catch (err) {
    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ message: "El correoT ya existe" });
    }
    return res.status(500).json({ error: err.code, message: err.message });
  }
});

// Login terapeuta
app.post("/auth/terapeuta/login", async (req, res) => {
  try {
    const { correoT, contrasenaT } = req.body;

    if (!correoT || !contrasenaT) {
      return res.status(400).json({ message: "Faltan campos: correoT, contrasenaT" });
    }

    const [rows] = await pool.execute(
      "SELECT idCedula, nombreT, correoT, contrasenaT FROM Terapeuta WHERE correoT = ? LIMIT 1",
      [correoT]
    );

    if (rows.length === 0) {
      return res.status(401).json({ message: "Credenciales invalidas" });
    }

    const terapeuta = rows[0];
    const ok = await bcrypt.compare(contrasenaT, terapeuta.contrasenaT);

    if (!ok) {
      return res.status(401).json({ message: "Credenciales invalidas" });
    }

    // Para app real: aqui devuelves JWT. Por ahora devolvemos datos basicos.
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
    return res.status(500).json({ error: err.code, message: err.message });
  }
});

// =====================================================
//                 AUTH: PACIENTE
// =====================================================

// Registro paciente (crea hash bcrypt)
app.post("/auth/paciente/signup", async (req, res) => {
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

    return res.status(201).json({
      message: "Paciente registrado",
      id_paciente: result.insertId,
    });
  } catch (err) {
    if (err.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ message: "El correoP ya existe" });
    }
    return res.status(500).json({ error: err.code, message: err.message });
  }
});

// Login paciente
app.post("/auth/paciente/login", async (req, res) => {
  try {
    const { correoP, contrasenaP } = req.body;

    if (!correoP || !contrasenaP) {
      return res.status(400).json({ message: "Faltan campos: correoP, contrasenaP" });
    }

    const [rows] = await pool.execute(
      "SELECT id_paciente, nombreP, correoP, contrasenaP, estrella FROM Paciente WHERE correoP = ? LIMIT 1",
      [correoP]
    );

    if (rows.length === 0) {
      return res.status(401).json({ message: "Credenciales invalidas" });
    }

    const paciente = rows[0];
    const ok = await bcrypt.compare(contrasenaP, paciente.contrasenaP);

    if (!ok) {
      return res.status(401).json({ message: "Credenciales invalidas" });
    }

    return res.json({
      message: "Login OK",
      role: "paciente",
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
});

// =====================================================
//                 RUTA DE PRUEBA
// =====================================================
app.get("/", (req, res) => res.send("API funcionando 🚀"));

app.listen(API_PORT, () => {
  console.log(`API corriendo en http://localhost:${API_PORT}`);
});
