const { pool } = require("../db");

const asignarEjercicios = async (req, res) => {
  const asignaciones = req.body;

  if (!Array.isArray(asignaciones) || asignaciones.length === 0) {
    return res.status(400).json({ message: "No se recibieron datos para asignar." });
  }

  try {
    for (const asignacion of asignaciones) {
      await pool.execute(
        "INSERT INTO asignacion (fecha, fk_terapeutaA, fk_paciente, fk_idEjercicio) VALUES (?, ?, ?, ?)",
        [
          asignacion.fecha,
          asignacion.fk_terapeutaA,
          asignacion.fk_paciente,
          asignacion.fk_idEjercicio
        ]
      );
    }

    res.status(200).json({
      status: "success",
      message: "Ejercicios asignados correctamente en la base de datos."
    });

  } catch (error) {
    console.error("ERROR EN MYSQL:", error.sqlMessage || error.message);
    res.status(500).json({
      message: "Error al guardar en la base de datos",
      error: error.sqlMessage || error.message
    });
  }
};

module.exports = { asignarEjercicios };