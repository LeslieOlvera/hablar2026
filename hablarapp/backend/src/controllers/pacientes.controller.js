const path = require("path");
const { pool } = require("../db");

function calcularEstrellas(porcentaje) {
  const p = parseFloat(porcentaje);

  if (Number.isNaN(p)) return 0;
  if (p >= 90) return 5;
  if (p >= 70) return 4;
  if (p >= 50) return 3;
  if (p >= 30) return 2;
  if (p >= 10) return 1;

  return 0;
}

// ======================================================
// OBTENER PROGRESO DE UN DÍA ESPECÍFICO
// ======================================================
async function getProgresoDia(req, res) {
  try {
    const { id } = req.params;
    const { fecha } = req.query;

    if (!fecha) {
      return res.status(400).json({
        message: "La fecha es requerida",
      });
    }

    const sql = `
      SELECT 
        r.id_realizar,
        r.id_ejercicio,
        e.nivel,
        e.descripcion,
        r.porcentaje,
        r.duracion,
        r.estrellas_ganadas,
        r.tipo_archivo,
        r.archivo_url,
        r.archivo_eliminado,
        r.estado_modelo,
        r.clasificacion_modelo,
        r.confianza_modelo,
        r.fecha_clasificacion,
        r.fechaRealiza
      FROM realizar r
      LEFT JOIN ejercicio e ON r.id_ejercicio = e.id_Ejercicio
      WHERE r.id_paciente = ?
        AND DATE(r.fechaRealiza) = ?
      ORDER BY r.id_realizar DESC
    `;

    const [rows] = await pool.execute(sql, [id, fecha]);

    return res.json(rows);
  } catch (err) {
    console.error("Error en getProgresoDia:", err);

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

// ======================================================
// HISTORIAL MENSUAL PARA CALENDARIO
// ======================================================
async function getHistorialMensual(req, res) {
  try {
    const { id, mes, anio } = req.params;

    const sql = `
      SELECT DISTINCT DAY(fechaRealiza) AS dia
      FROM realizar
      WHERE id_paciente = ?
        AND MONTH(fechaRealiza) = ?
        AND YEAR(fechaRealiza) = ?
    `;

    const [rows] = await pool.execute(sql, [id, mes, anio]);
    const dias = rows.map((row) => row.dia);

    return res.json(dias);
  } catch (err) {
    console.error("Error en getHistorialMensual:", err);

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

// ======================================================
// OBTENER PACIENTES
// ======================================================
async function getPacientes(req, res) {
  try {
    const { fk_idCedula } = req.query;

    let sql = `
      SELECT 
        id_paciente,
        nombreP,
        correoP,
        estrella,
        fk_idCedula
      FROM paciente
    `;

    const params = [];

    if (fk_idCedula) {
      sql += " WHERE fk_idCedula = ?";
      params.push(fk_idCedula);
    }

    const [rows] = await pool.execute(sql, params);

    return res.json(rows);
  } catch (err) {
    console.error("Error en getPacientes:", err);

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

// ======================================================
// OBTENER PACIENTE POR ID
// ======================================================
async function getPacienteById(req, res) {
  try {
    const { id } = req.params;

    const [rows] = await pool.execute(
      `
      SELECT 
        id_paciente,
        nombreP,
        correoP,
        estrella,
        fk_idCedula
      FROM paciente
      WHERE id_paciente = ?
      `,
      [id]
    );

    if (rows.length === 0) {
      return res.status(404).json({
        message: "Paciente no encontrado o terapia finalizada",
      });
    }

    return res.json(rows[0]);
  } catch (err) {
    console.error("Error en getPacienteById:", err);

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

// ======================================================
// ACTUALIZAR PACIENTE
// ======================================================
async function updatePaciente(req, res) {
  try {
    const { id } = req.params;
    const { nombreP, correoP, estrella } = req.body;

    await pool.execute(
      `
      UPDATE paciente
      SET nombreP = ?, correoP = ?, estrella = ?
      WHERE id_paciente = ?
      `,
      [nombreP, correoP, estrella, id]
    );

    return res.json({
      message: "Paciente actualizado",
    });
  } catch (err) {
    console.error("Error en updatePaciente:", err);

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

// ======================================================
// ELIMINAR PACIENTE
// ======================================================
async function deletePaciente(req, res) {
  try {
    const { id } = req.params;

    await pool.execute(
      "DELETE FROM paciente WHERE id_paciente = ?",
      [id]
    );

    return res.json({
      success: true,
      message: "Paciente eliminado correctamente",
    });
  } catch (err) {
    console.error("Error en deletePaciente:", err);

    return res.status(500).json({
      error: err.code,
      message: err.message,
    });
  }
}

// ======================================================
// OBTENER EJERCICIOS ASIGNADOS
// ======================================================
const getEjerciciosAsignados = async (req, res) => {
  try {
    const { id } = req.params;

    const sql = `
      SELECT DISTINCT 
        e.id_Ejercicio AS id,
        e.nivel,
        e.descripcion AS nombre
      FROM asignacion a
      INNER JOIN ejercicio e ON a.fk_idEjercicio = e.id_Ejercicio
      WHERE a.fk_paciente = ?
        AND a.completado = 0
        AND a.fecha >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    `;

    const [rows] = await pool.execute(sql, [id]);

    return res.json(rows);
  } catch (error) {
    console.error("Error obteniendo ejercicios asignados:", error);

    return res.status(500).json({
      message: "Error del servidor",
      error: error.message,
    });
  }
};

// ======================================================
// SUBIR AUDIO FONÉTICO
// ======================================================
const subirFonetico = async (req, res) => {
  try {
    const idPaciente = req.user?.id;

    const idEjercicio =
      req.body.id_ejercicio ||
      req.body.idEjercicio ||
      req.body.fk_idEjercicio;

    if (!idPaciente) {
      return res.status(401).json({
        message: "Usuario no autenticado",
      });
    }

    if (!idEjercicio) {
      return res.status(400).json({
        message: "Falta id_ejercicio",
      });
    }

    if (!req.file) {
      return res.status(400).json({
        message: "No se recibió el audio",
      });
    }

    const archivoUrl = `/uploads/foneticos/${req.file.filename}`;
    const rutaFisica = path.resolve(req.file.path);

    console.log("=====================================");
    console.log("AUDIO FONÉTICO RECIBIDO");
    console.log("Paciente:", idPaciente);
    console.log("Ejercicio:", idEjercicio);
    console.log("Nombre archivo:", req.file.filename);
    console.log("Ruta física:", rutaFisica);
    console.log("URL relativa:", archivoUrl);
    console.log("=====================================");

    const [result] = await pool.execute(
      `
      INSERT INTO realizar
        (
          fechaRealiza,
          porcentaje,
          duracion,
          estrellas_ganadas,
          tipo_archivo,
          archivo_url,
          archivo_eliminado,
          estado_modelo,
          clasificacion_modelo,
          confianza_modelo,
          fecha_clasificacion,
          error_modelo,
          id_paciente,
          id_ejercicio
        )
      VALUES
        (
          NOW(),
          NULL,
          NULL,
          0,
          'audio',
          ?,
          0,
          'pendiente',
          NULL,
          NULL,
          NULL,
          NULL,
          ?,
          ?
        )
      `,
      [archivoUrl, idPaciente, idEjercicio]
    );

    const idRealizar = result.insertId;

    return res.status(201).json({
      message: "Audio guardado correctamente",
      archivoUrl,
      rutaFisica,
      idRealizar,
      estadoModelo: "pendiente",
    });
  } catch (error) {
    console.error("Error en subirFonetico:", error);

    return res.status(500).json({
      message: "Error al guardar el audio",
      error: error.message,
    });
  }
};

// ======================================================
// SUBIR IMAGEN OROFACIAL
// ======================================================
const subirOrofacial = async (req, res) => {
  try {
    const idPaciente = req.user?.id;

    const idEjercicio =
      req.body.id_ejercicio ||
      req.body.idEjercicio ||
      req.body.fk_idEjercicio;

    if (!idPaciente) {
      return res.status(401).json({
        message: "Usuario no autenticado",
      });
    }

    if (!idEjercicio) {
      return res.status(400).json({
        message: "Falta id_ejercicio",
      });
    }

    if (!req.file) {
      return res.status(400).json({
        message: "No se recibió la imagen",
      });
    }

    const archivoUrl = `/uploads/orofaciales/${req.file.filename}`;
    const rutaFisica = path.resolve(req.file.path);

    console.log("=====================================");
    console.log("IMAGEN OROFACIAL RECIBIDA");
    console.log("Paciente:", idPaciente);
    console.log("Ejercicio:", idEjercicio);
    console.log("Nombre archivo:", req.file.filename);
    console.log("Ruta física:", rutaFisica);
    console.log("URL relativa:", archivoUrl);
    console.log("=====================================");

    const [result] = await pool.execute(
      `
      INSERT INTO realizar
        (
          fechaRealiza,
          porcentaje,
          duracion,
          estrellas_ganadas,
          tipo_archivo,
          archivo_url,
          archivo_eliminado,
          estado_modelo,
          clasificacion_modelo,
          confianza_modelo,
          fecha_clasificacion,
          error_modelo,
          id_paciente,
          id_ejercicio
        )
      VALUES
        (
          NOW(),
          NULL,
          NULL,
          0,
          'imagen',
          ?,
          0,
          'pendiente',
          NULL,
          NULL,
          NULL,
          NULL,
          ?,
          ?
        )
      `,
      [archivoUrl, idPaciente, idEjercicio]
    );

    const idRealizar = result.insertId;

    return res.status(201).json({
      message: "Imagen guardada correctamente",
      archivoUrl,
      rutaFisica,
      idRealizar,
      estadoModelo: "pendiente",
    });
  } catch (error) {
    console.error("Error en subirOrofacial:", error);

    return res.status(500).json({
      message: "Error al guardar la imagen",
      error: error.message,
    });
  }
};

// ======================================================
// GUARDAR PROGRESO MANUAL / GENERAL
// ======================================================
const guardarProgreso = async (req, res) => {
  try {
    const idPaciente = req.user?.id;

    const idEjercicio =
      req.body.id_ejercicio ||
      req.body.idEjercicio ||
      req.body.fk_idEjercicio;

    const porcentaje = req.body.porcentaje ?? null;
    const duracion = req.body.duracion ?? null;

    const estrellasGanadas =
      req.body.estrellas_ganadas ??
      calcularEstrellas(porcentaje);

    if (!idPaciente) {
      return res.status(401).json({
        message: "Usuario no autenticado",
      });
    }

    if (!idEjercicio) {
      return res.status(400).json({
        message: "Falta id_ejercicio",
      });
    }

    const [rows] = await pool.execute(
      `
      SELECT id_realizar
      FROM realizar
      WHERE id_paciente = ?
        AND id_ejercicio = ?
        AND DATE(fechaRealiza) = CURDATE()
      ORDER BY id_realizar DESC
      LIMIT 1
      `,
      [idPaciente, idEjercicio]
    );

    let idRealizar = null;

    if (rows.length > 0) {
      idRealizar = rows[0].id_realizar;

      await pool.execute(
        `
        UPDATE realizar
        SET 
          porcentaje = ?,
          duracion = ?,
          estrellas_ganadas = ?
        WHERE id_realizar = ?
        `,
        [porcentaje, duracion, estrellasGanadas, idRealizar]
      );
    } else {
      const [result] = await pool.execute(
        `
        INSERT INTO realizar
          (
            fechaRealiza,
            porcentaje,
            duracion,
            estrellas_ganadas,
            tipo_archivo,
            archivo_url,
            archivo_eliminado,
            estado_modelo,
            clasificacion_modelo,
            confianza_modelo,
            fecha_clasificacion,
            error_modelo,
            id_paciente,
            id_ejercicio
          )
        VALUES
          (
            NOW(),
            ?,
            ?,
            ?,
            NULL,
            NULL,
            0,
            NULL,
            NULL,
            NULL,
            NULL,
            NULL,
            ?,
            ?
          )
        `,
        [
          porcentaje,
          duracion,
          estrellasGanadas,
          idPaciente,
          idEjercicio,
        ]
      );

      idRealizar = result.insertId;
    }

    await pool.execute(
      `
      UPDATE asignacion
      SET completado = 1
      WHERE fk_paciente = ?
        AND fk_idEjercicio = ?
        AND completado = 0
      ORDER BY id_asignacion ASC
      LIMIT 1
      `,
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