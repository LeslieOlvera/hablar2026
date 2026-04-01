const express = require("express");

const indexRoutes = require("./routes/index");
const terapeutaAuthRoutes = require("./routes/auth.terapeuta.routes");
const pacienteAuthRoutes = require("./routes/auth.paciente.routes");
const pacientesRoutes = require("./routes/pacientes.routes");

const cors = require("cors");

const app = express(); // Primero se define la app

// Ahora se aplican los middlewares
app.use(cors()); // Esto permite que el celular se conecte al servidor
app.use(express.json());

app.use("/", indexRoutes);

// Auth
app.use("/auth/terapeuta", terapeutaAuthRoutes);
app.use("/auth/paciente", pacienteAuthRoutes);

// CRUD usuarios (Actividad 6)
app.use("/pacientes", pacientesRoutes);

module.exports = app;