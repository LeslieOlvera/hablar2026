const router = require("express").Router();
const { auth, requireTerapeuta, allowTerapeutaOrSelfPaciente } = require("../middlewares/auth");
const { getPacientes, getPacienteById, updatePaciente, deletePaciente } = require("../controllers/pacientes.controller");

// CRUD protegido
router.get("/", auth, requireTerapeuta, getPacientes);                     // GET /pacientes
router.get("/:id", auth, allowTerapeutaOrSelfPaciente, getPacienteById);   // GET /pacientes/:id
router.put("/:id", auth, allowTerapeutaOrSelfPaciente, updatePaciente);    // PUT /pacientes/:id
router.delete("/:id", auth, requireTerapeuta, deletePaciente);             // DELETE /pacientes/:id

module.exports = router;
