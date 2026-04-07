const app = require("./app");
const { testDbConnection } = require("./db");
const { testDbConnection } = require('./config/db');
const API_PORT = 3000;

// Probamos la conexión antes de iniciar
testDbConnection();

// '0.0.0.0' permite que dispositivos externos (tu cel) se conecten
app.listen(API_PORT, '0.0.0.0', () => {
  console.log(`✅ Servidor local en: http://localhost:${API_PORT}`);
  console.log(`🚀 URL para tu celular: http://192.168.0.175:${API_PORT}`);
});