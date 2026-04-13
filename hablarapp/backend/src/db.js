const mysql = require("mysql2/promise");

const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Servidor escuchando en http://0.0.0.0:${PORT}`);
    console.log(`Tu celular debe usar: http://192.168.x.x:${PORT}`);
});


const DB_HOST = "10.127.92.191";
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

const testDbConnection = async () => {
  try {
    const conn = await pool.getConnection();
    console.log("Conectado a MySQL  (Modo Promesa)");
    conn.release();
  } catch (err) {
    console.error(" Error conectando a MySQL:", err.message);
  }
};


testDbConnection();


module.exports = { pool, testDbConnection };