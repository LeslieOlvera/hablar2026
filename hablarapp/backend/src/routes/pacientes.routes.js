const router = require("express").Router();
const { auth, requireTerapeuta, allowTerapeutaOrSelfPaciente } = require("../middlewares/auth");
const { 
    getPacientes, getPacienteById, updatePaciente, deletePaciente,
    guardarProgreso, getProgresoDia, getEjerciciosAsignados 
} = require("../controllers/pacientes.controller");

router.get("/", auth, requireTerapeuta, getPacientes);
router.post("/guardar-progreso", auth, guardarProgreso);

// RUTA PARA OBTENER LAS TAREAS DEL DÍA
router.get("/:id/asignados", auth, getEjerciciosAsignados);

router.get("/:id/progreso-dia", auth, getProgresoDia);
router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);

module.exports = router;