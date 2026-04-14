const express = require("express");
const path = require("path"); // Necesario para manejar rutas de carpetas

const indexRoutes = require("./routes/index");
const terapeutaAuthRoutes = require("./routes/auth.terapeuta.routes");
const pacienteAuthRoutes = require("./routes/auth.paciente.routes");
const pacientesRoutes = require("./routes/pacientes.routes");
// Nueva ruta para la asignación de ejercicios
const asignacionesRoutes = require("./routes/asignaciones.routes"); 

const app = express();
app.use(express.json());

// Middleware para servir archivos estáticos (Imágenes y Audios)
// Esto permite acceder a los archivos vía URL, ej: http://tu-ip:3000/uploads/orofaciales/foto.jpg
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

app.use("/", indexRoutes);

// Auth
app.use("/auth/terapeuta", terapeutaAuthRoutes);
app.use("/auth/paciente", pacienteAuthRoutes);

// CRUD usuarios (Actividad 6)
app.use("/pacientes", pacientesRoutes);

//Ruta para mandar asignacion a la DB
app.use("/asignaciones", asignacionesRoutes);

module.exports = app;