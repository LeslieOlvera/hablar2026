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

// Esta función la dejamos AQUÍ ADENTRO para que se ejecute sola
const testDbConnection = async () => {
  try {
    const conn = await pool.getConnection();
    await conn.ping();
    conn.release();
    console.log("Conectado a MySQL ✅");
  } catch (err) {
    console.error("Error conectando a MySQL:", err.code, err.message);
  }
};

// La ejecutamos de una vez
testDbConnection();

// EXPORTACIÓN "TODOTERRENO"
// Esto permite que todos tus controladores funcionen, usen llaves {} o no.
module.exports = pool; 
module.exports.pool = pool; 
module.exports.testDbConnection = testDbConnection;