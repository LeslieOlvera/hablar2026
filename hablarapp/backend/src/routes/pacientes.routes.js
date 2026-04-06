const router = require("express").Router();
const { auth, requireTerapeuta, allowTerapeutaOrSelfPaciente } = require("../middlewares/auth");

// 1. IMPORTACIÓN CORREGIDA (Se agregó la coma después de guardarProgreso)
const { 
    getPacientes, 
    getPacienteById, 
    updatePaciente, 
    deletePaciente,
    guardarProgreso, // <--- Tenía que llevar coma aquí
    getProgresoDia   // <--- Ahora sí será reconocida
} = require("../controllers/pacientes.controller");

// --- RUTAS ---

// Obtener lista de pacientes (Solo terapeutas)
router.get("/", auth, requireTerapeuta, getPacientes);

// Guardar progreso (El niño envía sus estrellas al terminar)
router.post("/guardar-progreso", auth, guardarProgreso);

// Obtener progreso de un día específico (Calendario)
// NOTA: Se coloca ANTES de /:id para que Express no confunda "progreso-dia" con un ID
router.get("/:id/progreso-dia", auth, getProgresoDia);

// Rutas con ID (Ver, Editar, Eliminar)
router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);

module.exports = router;