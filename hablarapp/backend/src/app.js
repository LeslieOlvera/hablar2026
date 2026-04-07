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

// --- NUEVA SECCIÓN: ASIGNACIONES ---
// Esta es la ruta que usará el terapeuta para mandar los ejercicios a la DB
// En Android, tu URL de Retrofit debería apuntar a: /asignaciones/asignar
app.use("/asignaciones", asignacionesRoutes);

module.exports = app;