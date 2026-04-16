const express = require("express");
const path = require("path");
const fs = require("fs");

const indexRoutes = require("./routes/index");
const terapeutaAuthRoutes = require("./routes/auth.terapeuta.routes");
const pacienteAuthRoutes = require("./routes/auth.paciente.routes");
const pacientesRoutes = require("./routes/pacientes.routes");
const asignacionesRoutes = require("./routes/asignaciones.routes");

const app = express();

// ===============================
// ASEGURAR CARPETAS DE UPLOADS
// ===============================
const uploadsPath = path.join(__dirname, "uploads");
const orofacialesPath = path.join(uploadsPath, "orofaciales");
const foneticosPath = path.join(uploadsPath, "foneticos");

if (!fs.existsSync(uploadsPath)) {
  fs.mkdirSync(uploadsPath);
}

if (!fs.existsSync(orofacialesPath)) {
  fs.mkdirSync(orofacialesPath, { recursive: true });
}

if (!fs.existsSync(foneticosPath)) {
  fs.mkdirSync(foneticosPath, { recursive: true });
}

// ===============================
// MIDDLEWARES
// ===============================
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Servir archivos estáticos de la carpeta uploads
app.use("/uploads", express.static(uploadsPath));

// ===============================
// RUTAS
// ===============================
app.use("/", indexRoutes);

// Auth
app.use("/auth/terapeuta", terapeutaAuthRoutes);
app.use("/auth/paciente", pacienteAuthRoutes);

// CRUD usuarios
app.use("/pacientes", pacientesRoutes);

// Asignación de ejercicios
app.use("/asignaciones", asignacionesRoutes);

module.exports = app;