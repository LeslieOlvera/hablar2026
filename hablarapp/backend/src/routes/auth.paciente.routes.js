const router = require("express").Router();
const { 
    signupPaciente, 
    loginPaciente, 
    sendCodePaciente, 
    verifyCodePaciente, 
    resetPasswordPaciente 
} = require("../controllers/auth.paciente.controller");

// --- RUTAS DE ACCESO ---
router.post("/signup", signupPaciente);
router.post("/login", loginPaciente);



// --- RUTAS DE RECUPERACIÓN DE CONTRASEÑA ---

// 1. Generar y enviar código al correo
router.post("/send-code", sendCodePaciente);

// 2. Verificar si el código ingresado es correcto
router.post("/verify-code", verifyCodePaciente);

// 3. Establecer la nueva contraseña
router.post("/reset-password", resetPasswordPaciente);

module.exports = router;
