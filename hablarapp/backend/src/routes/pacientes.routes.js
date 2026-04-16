const router = require("express").Router();
const {
  auth,
  requireTerapeuta,
  allowTerapeutaOrSelfPaciente,
} = require("../middlewares/auth");

const {
  getPacientes,
  getPacienteById,
  updatePaciente,
  deletePaciente,
  guardarProgreso,
  getProgresoDia,
  getEjerciciosAsignados,
  getHistorialMensual,
} = require("../controllers/pacientes.controller");

// --- RUTAS DE PACIENTES ---
router.get("/", auth, requireTerapeuta, getPacientes);
router.post("/guardar-progreso", auth, guardarProgreso);

router.get("/:id/asignados", auth, getEjerciciosAsignados);
router.get("/:id/progreso-dia", auth, getProgresoDia);
router.get(
  "/progreso/historial/:id/:mes/:anio",
  auth,
  allowTerapeutaOrSelfPaciente,
  getHistorialMensual
);

router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);

module.exports = router;
