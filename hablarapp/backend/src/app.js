const express = require("express");

const indexRoutes = require("./routes/index");
const terapeutaAuthRoutes = require("./routes/auth.terapeuta.routes");
const pacienteAuthRoutes = require("./routes/auth.paciente.routes");
const pacientesRoutes = require("./routes/pacientes.routes");
// Nueva ruta para la asignación de ejercicios
const asignacionesRoutes = require("./routes/asignaciones.routes"); 

const app = express();
app.use(express.json());

app.use("/", indexRoutes);

// Auth
app.use("/auth/terapeuta", terapeutaAuthRoutes);
app.use("/auth/paciente", pacienteAuthRoutes);

// CRUD usuarios (Actividad 6)
app.use("/pacientes", pacientesRoutes);

//Ruta para mandar asignacion a la DB
app.use("/asignaciones", asignacionesRoutes);



module.exports = app;