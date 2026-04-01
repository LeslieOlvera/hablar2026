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

async function testDbConnection() {
  try {
    const conn = await pool.getConnection();
    await conn.ping();
    conn.release();
    console.log("Conectado a MySQL ✅");
  } catch (err) {
    console.error("Error conectando a MySQL:", err.code, err.message);
  }
}

module.exports = { pool, testDbConnection };
