const express = require("express");
const router = express.Router();
const pacienteController = require("../controllers/paciente.controller");

// --- RUTAS DE PACIENTE ---

// GET /pacientes 
// Esta es la ruta principal. Si Android envía ?fk_idCedula=XXX, 
// el controlador filtrará los resultados automáticamente.
router.get("/", pacienteController.getPacientes);

// GET /pacientes/:id
// Para obtener el detalle de un solo niño por su ID automático
router.get("/:id", pacienteController.getPacienteById);

// PUT /pacientes/:id
// Para actualizar datos del niño (nombre, estrellas, etc.)
router.put("/:id", pacienteController.updatePaciente);

// DELETE /pacientes/:id
// Para eliminar un paciente de la lista
router.delete("/:id", pacienteController.deletePaciente);

module.exports = router;