const { pool } = require("../db");

function calcularEstrellas(porcentaje) {
    const p = parseFloat(porcentaje);
    if (p >= 90) return 5;
    if (p >= 70) return 4;
    if (p >= 50) return 3;
    if (p >= 30) return 2;
    if (p >= 10) return 1;
    return 0;
}


// Obtiene los ejercicios realizados en un día específico
async function getProgresoDia(req, res) {
    try {
        const { id } = req.params; 
        const { fecha } = req.query; 
        if (!fecha) return res.status(400).json({ message: "La fecha es requerida" });
        const sql = `SELECT r.id_ejercicio, e.nivel, r.porcentaje, r.duracion, r.estrellas_ganadas 
                     FROM realizar r 
                     LEFT JOIN ejercicio e ON r.id_ejercicio = e.id_Ejercicio 
                     WHERE r.id_paciente = ? AND r.fechaRealiza = ? 
                     ORDER BY r.id_realizar DESC`;
        const [rows] = await pool.execute(sql, [id, fecha]);
        return res.json(rows);
    } catch (err) {
        return res.status(500).json({ error: err.code, message: err.message });
    }
}

/**
 * NUEVA FUNCIÓN: getHistorialMensual
 * Devuelve un arreglo de días [1, 5, 12...] en los que hubo actividad
 * para pintar el calendario de verde automáticamente.
 */
async function getHistorialMensual(req, res) {
    try {
        const { id, mes, anio } = req.params;
        const sql = `
            SELECT DISTINCT DAY(fechaRealiza) as dia 
            FROM realizar 
            WHERE id_paciente = ? 
              AND MONTH(fechaRealiza) = ? 
              AND YEAR(fechaRealiza) = ?
        `;
        const [rows] = await pool.execute(sql, [id, mes, anio]);
        const dias = rows.map(row => row.dia);
        return res.json(dias);
    } catch (err) {
        return res.status(500).json({ error: err.code, message: err.message });
    }
}

async function getPacientes(req, res) {
  try {
    const { fk_idCedula } = req.query; 
    let sql = "SELECT id_paciente, nombreP, correoP, estrella, fk_idCedula FROM paciente";
    let params = [];
    if (fk_idCedula) { sql += " WHERE fk_idCedula = ?"; params.push(fk_idCedula); }
    const [rows] = await pool.execute(sql, params);
    return res.json(rows);
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

async function getPacienteById(req, res) {
  try {
    const id = req.params.id;
    const [rows] = await pool.execute("SELECT id_paciente, nombreP, correoP, estrella, fk_idCedula FROM paciente WHERE id_paciente = ?", [id]);
    if (rows.length === 0) {
      return res.status(404).json({ message: "Paciente no encontrado o Terapia finalizada" });
    }
    return res.json(rows[0]);
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

async function updatePaciente(req, res) {
  try {
    const id = req.params.id;
    const { nombreP, correoP, estrella } = req.body;
    await pool.execute("UPDATE paciente SET nombreP = ?, correoP = ?, estrella = ? WHERE id_paciente = ?", [nombreP, correoP, estrella, id]);
    return res.json({ message: "Paciente actualizado" });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

async function deletePaciente(req, res) {
  try {
    const id = req.params.id;
    await pool.execute("DELETE FROM paciente WHERE id_paciente = ?", [id]);
    return res.json({ success: true, message: "Paciente eliminado correctamente" });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

const getEjerciciosAsignados = async (req, res) => {
    const { id } = req.params;

    try {
        const sql = `
            SELECT DISTINCT e.id_Ejercicio as id, e.nivel, e.descripcion as nombre
            FROM asignacion a
            INNER JOIN ejercicio e ON a.fk_idEjercicio = e.id_Ejercicio
            WHERE a.fk_paciente = ? 
              AND a.completado = 0 
              AND a.fecha >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        `;

        const [rows] = await pool.execute(sql, [id]);
        res.json(rows);
    } catch (error) {
        console.error("Error obteniendo ejercicios asignados:", error);
        res.status(500).json({ message: "Error del servidor" });
    }
};
const subirFonetico = async (req, res) => {
  try {
    const idPaciente = req.user?.id;
    const idEjercicio =
      req.body.id_ejercicio ||
      req.body.idEjercicio ||
      req.body.fk_idEjercicio;

    if (!idPaciente) {
      return res.status(401).json({ message: "Usuario no autenticado" });
    }

    if (!idEjercicio) {
      return res.status(400).json({ message: "Falta id_ejercicio" });
    }

    if (!req.file) {
      return res.status(400).json({ message: "No se recibió el audio" });
    }

    const archivoUrl = `/uploads/foneticos/${req.file.filename}`;

    const [result] = await pool.execute(
      `INSERT INTO realizar
        (fechaRealiza, porcentaje, duracion, estrellas_ganadas, tipo_archivo, archivo_url,
         clasificacion_modelo, confianza_modelo, id_paciente, id_ejercicio)
       VALUES
        (NOW(), NULL, NULL, 0, 'audio', ?, NULL, NULL, ?, ?)`,
      [archivoUrl, idPaciente, idEjercicio]
    );

    return res.status(201).json({
      message: "Audio guardado correctamente",
      archivoUrl,
      idRealizar: result.insertId,
    });
  } catch (error) {
    console.error("Error en subirFonetico:", error);
    return res.status(500).json({
      message: "Error al guardar el audio",
      error: error.message,
    });
  }
};

const subirOrofacial = async (req, res) => {
  try {
    const idPaciente = req.user?.id;
    const idEjercicio =
      req.body.id_ejercicio ||
      req.body.idEjercicio ||
      req.body.fk_idEjercicio;

    if (!idPaciente) {
      return res.status(401).json({ message: "Usuario no autenticado" });
    }

    if (!idEjercicio) {
      return res.status(400).json({ message: "Falta id_ejercicio" });
    }

    if (!req.file) {
      return res.status(400).json({ message: "No se recibió la imagen" });
    }

    const archivoUrl = `/uploads/orofaciales/${req.file.filename}`;

    const [result] = await pool.execute(
      `INSERT INTO realizar
        (fechaRealiza, porcentaje, duracion, estrellas_ganadas, tipo_archivo, archivo_url,
         clasificacion_modelo, confianza_modelo, id_paciente, id_ejercicio)
       VALUES
        (NOW(), NULL, NULL, 0, 'imagen', ?, NULL, NULL, ?, ?)`,
      [archivoUrl, idPaciente, idEjercicio]
    );

    return res.status(201).json({
      message: "Imagen guardada correctamente",
      archivoUrl,
      idRealizar: result.insertId,
    });
  } catch (error) {
    console.error("Error en subirOrofacial:", error);
    return res.status(500).json({
      message: "Error al guardar la imagen",
      error: error.message,
    });
  }
};

const guardarProgreso = async (req, res) => {
  try {
    const idPaciente = req.user?.id;

    const idEjercicio =
      req.body.id_ejercicio ||
      req.body.idEjercicio ||
      req.body.fk_idEjercicio;

    const porcentaje = req.body.porcentaje ?? null;
    const duracion = req.body.duracion ?? null;
    const estrellasGanadas = req.body.estrellas_ganadas ?? 0;

    if (!idPaciente) {
      return res.status(401).json({ message: "Usuario no autenticado" });
    }

    if (!idEjercicio) {
      return res.status(400).json({ message: "Falta id_ejercicio" });
    }

    // Buscar si ya existe un intento del mismo paciente para ese ejercicio hoy
    const [rows] = await pool.execute(
      `SELECT id_realizar
       FROM realizar
       WHERE id_paciente = ?
         AND id_ejercicio = ?
         AND DATE(fechaRealiza) = CURDATE()
       ORDER BY id_realizar DESC
       LIMIT 1`,
      [idPaciente, idEjercicio]
    );

    let idRealizar = null;

    if (rows.length > 0) {
      idRealizar = rows[0].id_realizar;

      await pool.execute(
        `UPDATE realizar
         SET porcentaje = ?, duracion = ?, estrellas_ganadas = ?
         WHERE id_realizar = ?`,
        [porcentaje, duracion, estrellasGanadas, idRealizar]
      );
    } else {
      const [result] = await pool.execute(
        `INSERT INTO realizar
          (fechaRealiza, porcentaje, duracion, estrellas_ganadas, tipo_archivo, archivo_url,
           clasificacion_modelo, confianza_modelo, id_paciente, id_ejercicio)
         VALUES
          (NOW(), ?, ?, ?, NULL, NULL, NULL, NULL, ?, ?)`,
        [porcentaje, duracion, estrellasGanadas, idPaciente, idEjercicio]
      );

      idRealizar = result.insertId;
    }

    // Marcar como completada solo una asignación pendiente de ese ejercicio
    await pool.execute(
      `UPDATE asignacion
       SET completado = 1
       WHERE fk_paciente = ?
         AND fk_idEjercicio = ?
         AND completado = 0
       ORDER BY id_asignacion ASC
       LIMIT 1`,
      [idPaciente, idEjercicio]
    );

    return res.status(200).json({
      message: "Progreso guardado correctamente",
      idRealizar,
    });
  } catch (error) {
    console.error("Error en guardarProgreso:", error);
    return res.status(500).json({
      message: "Error al guardar progreso",
      error: error.message,
    });
  }
};



module.exports = {
  getPacientes,
  getPacienteById,
  updatePaciente,
  deletePaciente,
  guardarProgreso,
  getProgresoDia,
  getEjerciciosAsignados,
  getHistorialMensual,
  subirFonetico,
  subirOrofacial,
};