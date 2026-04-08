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

async function guardarProgreso(req, res) {
    const connection = await pool.getConnection();
    try {
        const { id_paciente, id_ejercicio, porcentaje, duracion } = req.body;
        const fechaHoy = new Date().toISOString().split('T')[0];
        const estrellasNuevas = calcularEstrellas(porcentaje);

        await connection.beginTransaction();

        // 1. Insertar registro en Realizar
        const sqlRealizar = `INSERT INTO Realizar (fechaRealiza, porcentaje, duracion, estrellas_ganadas, id_paciente, id_ejercicio) VALUES (?, ?, ?, ?, ?, ?)`;
        await connection.execute(sqlRealizar, [fechaHoy, porcentaje, duracion, estrellasNuevas, id_paciente, id_ejercicio]);

        // 2. Sumar estrellas al paciente
        const sqlSumarEstrellas = `UPDATE Paciente SET estrella = estrella + ? WHERE id_paciente = ?`;
        await connection.execute(sqlSumarEstrellas, [estrellasNuevas, id_paciente]);

        // 3. CORRECCIÓN: Marcar la asignación como completada para que desaparezca de pendientes
        const sqlCompletarAsignacion = `
            UPDATE Asignacion 
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

async function getProgresoDia(req, res) {
    try {
        const { id } = req.params; 
        const { fecha } = req.query; 
        if (!fecha) return res.status(400).json({ message: "La fecha es requerida" });
        const sql = `SELECT r.id_ejercicio, e.nivel, r.porcentaje, r.duracion, r.estrellas_ganadas FROM Realizar r LEFT JOIN Ejercicio e ON r.id_ejercicio = e.id_Ejercicio WHERE r.id_paciente = ? AND r.fechaRealiza = ? ORDER BY r.id_realizar DESC`;
        const [rows] = await pool.execute(sql, [id, fecha]);
        return res.json(rows);
    } catch (err) {
        return res.status(500).json({ error: err.code, message: err.message });
    }
}

async function getPacientes(req, res) {
  try {
    const { fk_idCedula } = req.query; 
    let sql = "SELECT id_paciente, nombreP, correoP, estrella, fk_idCedula FROM Paciente";
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
    const [rows] = await pool.execute("SELECT id_paciente, nombreP, correoP, estrella, fk_idCedula FROM Paciente WHERE id_paciente = ?", [id]);
    return res.json(rows[0]);
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

async function updatePaciente(req, res) {
  try {
    const id = req.params.id;
    const { nombreP, correoP, estrella } = req.body;
    await pool.execute("UPDATE Paciente SET nombreP = ?, correoP = ?, estrella = ? WHERE id_paciente = ?", [nombreP, correoP, estrella, id]);
    return res.json({ message: "Paciente actualizado" });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

async function deletePaciente(req, res) {
  try {
    const id = req.params.id;
    await pool.execute("DELETE FROM Paciente WHERE id_paciente = ?", [id]);
    return res.json({ message: "Paciente eliminado" });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// --- NUEVA FUNCIÓN PARA EL HOME DEL PACIENTE (CORREGIDA CON PLAZO DE 7 DÍAS) ---
async function getEjerciciosAsignados(req, res) {
    try {
        const { id } = req.params;
        const sql = `
            SELECT e.id_Ejercicio as id, e.nivel, e.descripcion as nombre
            FROM Asignacion a
            INNER JOIN Ejercicio e ON a.fk_idEjercicio = e.id_Ejercicio
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

module.exports = { 
    getPacientes, getPacienteById, updatePaciente, deletePaciente, 
    guardarProgreso, getProgresoDia, getEjerciciosAsignados 
};