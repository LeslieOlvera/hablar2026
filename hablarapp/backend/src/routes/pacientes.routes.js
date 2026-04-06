const router = require("express").Router();
const { auth, requireTerapeuta, allowTerapeutaOrSelfPaciente } = require("../middlewares/auth");

// Importamos las funciones del controlador
const { 
    getPacientes, 
    getPacienteById, 
    updatePaciente, 
    deletePaciente 
} = require("../controllers/pacientes.controller");

// --- RUTAS ---
router.get("/", auth, requireTerapeuta, getPacientes);
router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);
module.exports = router;