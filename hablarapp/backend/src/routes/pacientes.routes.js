const router = require("express").Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { auth, requireTerapeuta, allowTerapeutaOrSelfPaciente } = require("../middlewares/auth");
const { 
    getPacientes, getPacienteById, updatePaciente, deletePaciente,
    guardarProgreso, getProgresoDia, getEjerciciosAsignados,
    subirOrofacial, subirFonetico
} = require("../controllers/pacientes.controller");

// Crear carpetas si no existen
const uploadDir = path.join(__dirname, '../../uploads');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir);
if (!fs.existsSync(path.join(uploadDir, 'orofaciales'))) fs.mkdirSync(path.join(uploadDir, 'orofaciales'));
if (!fs.existsSync(path.join(uploadDir, 'foneticos'))) fs.mkdirSync(path.join(uploadDir, 'foneticos'));

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const type = file.mimetype.startsWith('image/') ? 'orofaciales' : 'foneticos';
        cb(null, path.join(__dirname, '../../uploads', type));
    },
    filename: (req, file, cb) => {
        cb(null, `${Date.now()}-${file.originalname}`);
    }
});
const upload = multer({ storage });

router.get("/", auth, requireTerapeuta, getPacientes);
router.post("/guardar-progreso", auth, guardarProgreso);
router.get("/:id/asignados", auth, getEjerciciosAsignados);
router.get("/:id/progreso-dia", auth, getProgresoDia);
router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);

// Rutas de subida
router.post("/subir-orofacial", auth, upload.single('foto'), subirOrofacial);
router.post("/subir-fonetico", auth, upload.single('audio'), subirFonetico);

module.exports = router;