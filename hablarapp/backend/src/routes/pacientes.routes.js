const router = require("express").Router();
const multer = require("multer");
const path = require("path");
const fs = require("fs");

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
// ASEGURAR CARPETAS
// ===============================
const carpetaOrofaciales = path.join(__dirname, "../uploads/orofaciales");
const carpetaFoneticos = path.join(__dirname, "../uploads/foneticos");

if (!fs.existsSync(carpetaOrofaciales)) {
  fs.mkdirSync(carpetaOrofaciales, { recursive: true });
}

if (!fs.existsSync(carpetaFoneticos)) {
  fs.mkdirSync(carpetaFoneticos, { recursive: true });
}

// ===============================
// MULTER
// ===============================
const storageOrofacial = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, carpetaOrofaciales);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname) || ".jpg";
    cb(null, `oro_${Date.now()}${ext}`);
  },
});

const storageFonetico = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, carpetaFoneticos);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname) || ".m4a";
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

// ===============================
// SUBIDA DE EVIDENCIAS
// ===============================
router.post(
  "/subir-orofacial",
  auth,
  uploadOrofacial.single("imagen"),
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