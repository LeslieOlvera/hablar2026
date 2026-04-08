const router = require("express").Router();
const { 
    signupTerapeuta, 
    loginTerapeuta,
    sendCodeTerapeuta,
    verifyCodeTerapeuta,
    resetPasswordTerapeuta 
} = require("../controllers/auth.terapeuta.controller");

// --- RUTAS DE ACCESO ---
router.post("/signup", signupTerapeuta);
router.post("/login", loginTerapeuta);

// --- RUTAS DE RECUPERACIÓN DE CONTRASEÑA ---

// 1. Generar y mostrar código para el terapeuta
router.post("/send-code", sendCodeTerapeuta);

// 2. Verificar el código ingresado por el terapeuta
router.post("/verify-code", verifyCodeTerapeuta);

// 3. Actualizar la contraseña del terapeuta
router.post("/reset-password", resetPasswordTerapeuta);

module.exports = router;