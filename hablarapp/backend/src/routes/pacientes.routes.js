const router = require("express").Router();
const { auth, requireTerapeuta, allowTerapeutaOrSelfPaciente } = require("../middlewares/auth");

// NOTA: Asegúrate de que el archivo en la carpeta controllers se llame "pacientes.controller.js"
const { 
    getPacientes, 
    getPacienteById, 
    updatePaciente, 
    deletePaciente 
} = require("../controllers/pacientes.controller"); // <-- Aquí estaba el error (faltaba la 's')

// CRUD protegido con tus middlewares originales
// El GET / ahora recibirá el fk_idCedula desde Android para filtrar
router.get("/", auth, requireTerapeuta, getPacientes); 
router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);
router.delete("/:id", auth, requireTerapeuta, deletePaciente);

module.exports = router;