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

// Guarda el progreso del ejercicio y actualiza estrellas del paciente
async function guardarProgreso(req, res) {
    const connection = await pool.getConnection();
    try {
        const { id_paciente, id_ejercicio, porcentaje, duracion } = req.body;
        const fechaHoy = new Date().toISOString().split('T')[0];
        const estrellasNuevas = calcularEstrellas(porcentaje);

        await connection.beginTransaction();

        const sqlRealizar = `INSERT INTO realizar (fechaRealiza, porcentaje, duracion, estrellas_ganadas, id_paciente, id_ejercicio) VALUES (?, ?, ?, ?, ?, ?)`;
        await connection.execute(sqlRealizar, [fechaHoy, porcentaje, duracion, estrellasNuevas, id_paciente, id_ejercicio]);

        const sqlSumarEstrellas = `UPDATE paciente SET estrella = estrella + ? WHERE id_paciente = ?`;
        await connection.execute(sqlSumarEstrellas, [estrellasNuevas, id_paciente]);

        const sqlCompletarAsignacion = `
            UPDATE asignacion 
            SET completado = 1 
            WHERE fk_paciente = ? AND fk_idEjercicio = ? AND completado = 0 
            LIMIT 1`;
        await connection.execute(sqlCompletarAsignacion, [id_paciente, id_ejercicio]);

        await connection.commit();
        return res.json({ message: "Progreso guardado y ejercicio completado", estrellasGanadas: estrellasNuevas });
    } catch (err) {
        await connection.rollback();
        return res.status(500).json({ error: err.code, message: err.message });
    } finally {
        connection.release();
    }
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

async function getEjerciciosAsignados(req, res) {
    try {
        const { id } = req.params;
        const sql = `
            SELECT e.id_Ejercicio as id, e.nivel, e.descripcion as nombre
            FROM asignacion a
            INNER JOIN ejercicio e ON a.fk_idEjercicio = e.id_Ejercicio
            WHERE a.fk_paciente = ? 
              AND a.completado = 0 
              AND a.fecha >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        `;
        const [rows] = await pool.execute(sql, [id]);
        return res.json(rows);
    } catch (err) {
        return res.status(500).json({ error: err.code, message: err.message });
    }
}

async function subirOrofacial(req, res) {
    try {
        const { id_ejercicio } = req.body;
        const imagen_url = req.file ? `/uploads/orofaciales/${req.file.filename}` : null;
        if (!id_ejercicio || !imagen_url) return res.status(400).json({ message: "Faltan datos o imagen" });
        const sql = "INSERT INTO ejercicio_orofacial (id_ejercicio, imagen_url) VALUES (?, ?)";
        await pool.execute(sql, [id_ejercicio, imagen_url]);
        res.status(201).json({ success: true, message: "Imagen guardada", url: imagen_url });
    } catch (err) {
        res.status(500).json({ error: err.code, message: err.message });
    }
}

async function subirFonetico(req, res) {
    try {
        const { id_ejercicio } = req.body;
        const voz_url = req.file ? `/uploads/foneticos/${req.file.filename}` : null;
        if (!id_ejercicio || !voz_url) return res.status(400).json({ message: "Faltan datos o audio" });
        const sql = "INSERT INTO ejercicios_fonetico (id_ejercicio, voz_url) VALUES (?, ?)";
        await pool.execute(sql, [id_ejercicio, voz_url]);
        res.status(201).json({ success: true, message: "Audio guardado", url: voz_url });
    } catch (err) {
        res.status(500).json({ error: err.code, message: err.message });
    }
}

module.exports = { 
    getPacientes, 
    getPacienteById, 
    updatePaciente, 
    deletePaciente, 
    guardarProgreso, 
    getProgresoDia, 
    getHistorialMensual, // <-- Exportada
    getEjerciciosAsignados,
    subirOrofacial, 
    subirFonetico
};