const router = require("express").Router();
const { auth, requireTerapeuta, allowTerapeutaOrSelfPaciente } = require("../middlewares/auth");

// 1. IMPORTANTE: Agregamos guardarProgreso a la lista de importación
const { 
    getPacientes, 
    getPacienteById, 
    updatePaciente, 
    deletePaciente,
    guardarProgreso // <--- Agrega esto
} = require("../controllers/pacientes.controller");

// --- RUTAS ---

router.get("/", auth, requireTerapeuta, getPacientes);

// 2. Agregamos la ruta POST para que el niño guarde sus estrellas
// Se usa "auth" porque el niño debe estar logueado para guardar su progreso
router.post("/guardar-progreso", auth, guardarProgreso);

router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);
router.get("/:id/progreso-dia", auth, getProgresoDia);
module.exports = router;