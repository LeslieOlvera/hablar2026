const app = require("./app");
const { testDbConnection } = require("./db");

const API_PORT = 3000;

testDbConnection();

app.listen(API_PORT, () => {
  console.log(`API corriendo en http://localhost:${API_PORT}`);
});
