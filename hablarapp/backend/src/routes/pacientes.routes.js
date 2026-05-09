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
// ASEGURAR CARPETAS BASE
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
// UTILIDADES
// ===============================
function obtenerExtensionSegura(file, extensionDefault) {
  const ext = path.extname(file.originalname || "").toLowerCase();

  if (!ext) {
    return extensionDefault;
  }

  return ext;
}

function generarNombreUnico(prefijo, extension) {
  const timestamp = Date.now();
  const random = Math.round(Math.random() * 1e9);

  return `${prefijo}_${timestamp}_${random}${extension}`;
}

// ===============================
// MULTER - IMÁGENES OROFACIALES
// ===============================
// Estructura final:
// src/uploads/orofaciales/paciente_ID/oro_TIMESTAMP_RANDOM.png

const storageOrofacial = multer.diskStorage({
  destination: (req, file, cb) => {
    const idPaciente = req.user?.id;

    if (!idPaciente) {
      return cb(new Error("Usuario no autenticado para guardar imagen"));
    }

    const carpetaPaciente = path.join(
      carpetaOrofaciales,
      `paciente_${idPaciente}`
    );

    if (!fs.existsSync(carpetaPaciente)) {
      fs.mkdirSync(carpetaPaciente, { recursive: true });
    }

    cb(null, carpetaPaciente);
  },

  filename: (req, file, cb) => {
    const ext = obtenerExtensionSegura(file, ".png");
    const nombreArchivo = generarNombreUnico("oro", ext);

    cb(null, nombreArchivo);
  },
});

// ===============================
// MULTER - AUDIOS FONÉTICOS
// ===============================
// Estructura final:
// src/uploads/foneticos/paciente_ID/fon_TIMESTAMP_RANDOM.m4a

const storageFonetico = multer.diskStorage({
  destination: (req, file, cb) => {
    const idPaciente = req.user?.id;

    if (!idPaciente) {
      return cb(new Error("Usuario no autenticado para guardar audio"));
    }

    const carpetaPaciente = path.join(
      carpetaFoneticos,
      `paciente_${idPaciente}`
    );

    if (!fs.existsSync(carpetaPaciente)) {
      fs.mkdirSync(carpetaPaciente, { recursive: true });
    }

    cb(null, carpetaPaciente);
  },

  filename: (req, file, cb) => {
    const ext = obtenerExtensionSegura(file, ".m4a");
    const nombreArchivo = generarNombreUnico("fon", ext);

    cb(null, nombreArchivo);
  },
});

const uploadOrofacial = multer({
  storage: storageOrofacial,
});

const uploadFonetico = multer({
  storage: storageFonetico,
});

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
// Importante:
// auth debe ir ANTES de multer para que req.user.id exista
// y podamos crear la carpeta paciente_ID.

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