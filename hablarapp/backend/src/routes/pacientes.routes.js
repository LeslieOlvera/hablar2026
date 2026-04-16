const router = require("express").Router();
const path = require("path");
const fs = require("fs");
const multer = require("multer");

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
  subirFonetico,
  subirOrofacial,
} = require("../controllers/pacientes.controller");

// Crear carpetas si no existen
const foneticoDir = path.join(__dirname, "..", "uploads", "foneticos");
const orofacialDir = path.join(__dirname, "..", "uploads", "orofaciales");

fs.mkdirSync(foneticoDir, { recursive: true });
fs.mkdirSync(orofacialDir, { recursive: true });

// Configuración de multer
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    if (req.path === "/subir-fonetico") {
      cb(null, foneticoDir);
    } else if (req.path === "/subir-orofacial") {
      cb(null, orofacialDir);
    } else {
      cb(null, path.join(__dirname, "..", "uploads"));
    }
  },
  filename: (req, file, cb) => {
    const extension = path.extname(file.originalname);
    const nombre = `${Date.now()}-${Math.round(Math.random() * 1e9)}${extension}`;
    cb(null, nombre);
  },
});

const upload = multer({ storage });

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

// SUBIDA DE EVIDENCIAS
router.post("/subir-fonetico", auth, upload.single("audio"), subirFonetico);
router.post("/subir-orofacial", auth, upload.single("imagen"), subirOrofacial);

router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);

module.exports = router;