const router = require("express").Router();
const multer = require("multer");
const path = require("path");

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
  subirOrofacial,
  subirFonetico,
} = require("../controllers/pacientes.controller");

// ===============================
// MULTER
// ===============================
const storageOrofacial = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, path.join(__dirname, "../uploads/orofaciales"));
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname);
    cb(null, `oro_${Date.now()}${ext}`);
  },
});

const storageFonetico = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, path.join(__dirname, "../uploads/foneticos"));
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname);
    cb(null, `fon_${Date.now()}${ext}`);
  },
});

const uploadOrofacial = multer({ storage: storageOrofacial });
const uploadFonetico = multer({ storage: storageFonetico });

// ===============================
// RUTAS DE PACIENTES
// ===============================
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

// SUBIDA DE EVIDENCIAS
router.post(
  "/subir-orofacial",
  auth,
  uploadOrofacial.single("foto"),
  subirOrofacial
);

router.post(
  "/subir-fonetico",
  auth,
  uploadFonetico.single("audio"),
  subirFonetico
);

router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);

module.exports = router;