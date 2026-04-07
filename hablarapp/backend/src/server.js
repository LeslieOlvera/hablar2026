const app = require("./app");
const { pool, testDbConnection } = require("./db");

const API_PORT = 3000;

// Probamos la conexión a la base de datos antes de iniciar el servidor
// Esto asegura que si la DB está caída, lo sepamos de inmediato
testDbConnection();

// El servidor escucha en '0.0.0.0' para permitir conexiones desde la red local
// Esto es indispensable para que tu celular (en la misma Wi-Fi) pueda ver el servidor
app.listen(API_PORT, '0.0.0.0', () => {
  console.log("--------------------------------------------------");
  console.log(`✅ Servidor local ejecutándose en: http://localhost:${API_PORT}`);
  console.log(`🚀 URL para configurar en Android Studio: http://192.168.0.175:${API_PORT}`);
  console.log("--------------------------------------------------");
});