const router = require("express").Router();
const { signupPaciente, loginPaciente } = require("../controllers/auth.paciente.controller");

router.post("/signup", signupPaciente);
router.post("/login", loginPaciente);

module.exports = router;

