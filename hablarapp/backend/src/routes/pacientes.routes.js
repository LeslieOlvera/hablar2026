const router = require("express").Router();
const {
  auth,
  requireTerapeuta,
  allowTerapeutaOrSelfPaciente,
} = require("../middlewares/auth");

const {
  uploadFonetico,
  uploadOrofacial,
} = require("../middlewares/upload");

const {
  getPacientes,
  getPacienteById,
  updatePaciente,
  deletePaciente,
  guardarProgreso,
  getProgresoDia,
  getEjerciciosAsignados,
  getHistorialMensual,
  subirFonetico,
  subirOrofacial,
} = require("../controllers/pacientes.controller");

// --- RUTAS DE PACIENTES ---
router.get("/", auth, requireTerapeuta, getPacientes);

router.post("/guardar-progreso", auth, guardarProgreso);
router.post("/subir-fonetico", auth, uploadFonetico.single("audio"), subirFonetico);
router.post("/subir-orofacial", auth, uploadOrofacial.single("imagen"), subirOrofacial);

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