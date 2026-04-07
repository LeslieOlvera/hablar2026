const mysql = require("mysql2/promise");

const DB_HOST = "127.0.0.1";
const DB_PORT = 3306;
const DB_USER = "root";
const DB_PASS = "maria123?";
const DB_NAME = "app_tshusuarios";

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

// Prueba la conexión al arrancar el servidor
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


// Al final de tu db.js (reemplaza las exportaciones anteriores)
module.exports = pool;         // Permite: const pool = require("../db")
module.exports.pool = pool;    // Permite: const { pool } = require("../db")