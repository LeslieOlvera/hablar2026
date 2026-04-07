const express = require("express");
const router = express.Router();
const asignacionesController = require("../controllers/asignaciones.controller");
const { requireAuth } = require("../middlewares/auth"); // Si usas el middleware de token

// La ruta final será: POST /asignaciones/asignar
router.post("/asignar", asignacionesController.asignarEjercicios);

module.exports = router;