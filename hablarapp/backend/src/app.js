const express = require("express");
const path = require("path");

const indexRoutes = require("./routes/index");
const terapeutaAuthRoutes = require("./routes/auth.terapeuta.routes");
const pacienteAuthRoutes = require("./routes/auth.paciente.routes");
const pacientesRoutes = require("./routes/pacientes.routes");
const asignacionesRoutes = require("./routes/asignaciones.routes");

const app = express();

app.use(express.json());

// Servir archivos estáticos de la carpeta uploads
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

app.use("/", indexRoutes);

// Auth
app.use("/auth/terapeuta", terapeutaAuthRoutes);
app.use("/auth/paciente", pacienteAuthRoutes);

// CRUD usuarios
app.use("/pacientes", pacientesRoutes);

// Asignación de ejercicios
app.use("/asignaciones", asignacionesRoutes);

module.exports = app;