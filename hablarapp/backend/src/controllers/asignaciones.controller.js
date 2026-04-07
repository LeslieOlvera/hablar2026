const pool = require("../db"); // Asegúrate de que apunte a tu conexión de db.js

const asignarEjercicios = async (req, res) => {
    const asignaciones = req.body; // Array de ejercicios enviado desde Android

    if (!Array.isArray(asignaciones) || asignaciones.length === 0) {
        return res.status(400).json({ message: "No se recibieron datos" });
    }

    try {
        // Usamos un bucle para insertar cada ejercicio de la lista
        for (const asignacion of asignaciones) {
            await pool.query(
                "INSERT INTO Asignacion (fecha, fk_terapeutaA, fk_paciente, fk_idEjercicio) VALUES (?, ?, ?, ?)",
                [asignacion.fecha, asignacion.fk_terapeutaA, asignacion.fk_paciente, asignacion.fk_idEjercicio]
            );
        }
        res.json({ status: "success", message: "Ejercicios asignados correctamente" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Error en el servidor" });
    }
};

module.exports = { asignarEjercicios };