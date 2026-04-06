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

// 1. GET / - QUITAMOS 'auth' y 'requireTerapeuta' para que Android pueda entrar sin Token
router.get("/", getPacientes); 

// 2. Las demás rutas las dejamos protegidas (por ahora no las usaremos en el Home)
router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);

module.exports = router;