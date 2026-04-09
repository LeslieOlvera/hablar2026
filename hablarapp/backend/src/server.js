const app = require("./app");
const { pool, testDbConnection } = require("./db");

const API_PORT = 3000;


testDbConnection();


app.listen(API_PORT, '0.0.0.0', () => {
  console.log("--------------------------------------------------");
  console.log(` Servidor local ejecutándose en: http://localhost:${API_PORT}`);
  console.log(` URL para configurar en Android Studio: http://192.168.0.175:${API_PORT}`);
  console.log("--------------------------------------------------");
});