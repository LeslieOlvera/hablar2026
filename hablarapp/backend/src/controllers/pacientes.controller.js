const { pool } = require("../db");

// --- FUNCIÓN INTERNA: Calcula estrellas según tu lógica ---
function calcularEstrellas(porcentaje) {
    const p = parseFloat(porcentaje);
    if (p >= 90) return 5;
    if (p >= 70) return 4;
    if (p >= 50) return 3;
    if (p >= 30) return 2;
    if (p >= 10) return 1;
    return 0;
}

// 1. REGISTRAR AVANCE
async function guardarProgreso(req, res) {
    const connection = await pool.getConnection();
    try {
        const { id_paciente, id_ejercicio, porcentaje, duracion } = req.body;
        const fechaHoy = new Date().toISOString().split('T')[0];
        const estrellasNuevas = calcularEstrellas(porcentaje);

        await connection.beginTransaction();

        const sqlRealizar = `
            INSERT INTO Realizar (fechaRealiza, porcentaje, duracion, estrellas_ganadas, id_paciente, id_ejercicio)
            VALUES (?, ?, ?, ?, ?, ?)
        `;
        await connection.execute(sqlRealizar, [fechaHoy, porcentaje, duracion, estrellasNuevas, id_paciente, id_ejercicio]);

        const sqlSumarEstrellas = `
            UPDATE Paciente SET estrella = estrella + ? WHERE id_paciente = ?
        `;
        await connection.execute(sqlSumarEstrellas, [estrellasNuevas, id_paciente]);

        await connection.commit();
        return res.json({ message: "Progreso guardado correctamente", estrellasGanadas: estrellasNuevas });
    } catch (err) {
        await connection.rollback();
        console.error("Error en guardarProgreso:", err);
        return res.status(500).json({ error: err.code, message: err.message });
    } finally {
        connection.release();
    }
}

// 2. OBTENER PROGRESO POR DÍA
async function getProgresoDia(req, res) {
    try {
        const { id } = req.params; 
        const { fecha } = req.query; 

        if (!fecha) return res.status(400).json({ message: "La fecha es requerida (YYYY-MM-DD)" });

        // MEJORA: Usamos LEFT JOIN para que, si el ejercicio no existe en la tabla Ejercicio,
        // al menos veamos el ID y el porcentaje en el calendario.
        const sql = `
            SELECT 
                r.id_ejercicio, 
                e.nivel, 
                r.porcentaje, 
                r.duracion, 
                r.estrellas_ganadas
            FROM Realizar r
            LEFT JOIN Ejercicio e ON r.id_ejercicio = e.id_Ejercicio
            WHERE r.id_paciente = ? AND r.fechaRealiza = ?
            ORDER BY r.id_realizar DESC
        `;

        const [rows] = await pool.execute(sql, [id, fecha]);
        return res.json(rows);
    } catch (err) {
        console.error("Error en getProgresoDia:", err);
        return res.status(500).json({ error: err.code, message: err.message });
    }
}

// 3. GET /pacientes
async function getPacientes(req, res) {
  try {
    const { fk_idCedula } = req.query; 
    let sql = "SELECT id_paciente, nombreP, correoP, estrella, fk_idCedula FROM Paciente";
    let params = [];
    if (fk_idCedula) {
      sql += " WHERE fk_idCedula = ?";
      params.push(fk_idCedula);
    }
    sql += " ORDER BY id_paciente DESC";
    const [rows] = await pool.execute(sql, params);
    return res.json(rows);
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// 4. GET /pacientes/:id
async function getPacienteById(req, res) {
  try {
    const id = Number(req.params.id);
    const [rows] = await pool.execute(
      "SELECT id_paciente, nombreP, correoP, estrella, fk_idCedula FROM Paciente WHERE id_paciente = ? LIMIT 1",
      [id]
    );
    if (rows.length === 0) return res.status(404).json({ message: "Paciente no encontrado" });
    return res.json(rows[0]);
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// 5. PUT /pacientes/:id
async function updatePaciente(req, res) {
  try {
    const id = Number(req.params.id);
    const { nombreP, correoP, estrella, fk_idCedula } = req.body;
    const fields = [];
    const values = [];
    if (nombreP !== undefined) { fields.push("nombreP = ?"); values.push(nombreP); }
    if (correoP !== undefined) { fields.push("correoP = ?"); values.push(correoP); }
    if (estrella !== undefined) { fields.push("estrella = ?"); values.push(estrella); }
    if (fk_idCedula !== undefined) { fields.push("fk_idCedula = ?"); values.push(fk_idCedula); }

    if (fields.length === 0) return res.status(400).json({ message: "Nada para actualizar" });
    values.push(id);
    const sql = `UPDATE Paciente SET ${fields.join(", ")} WHERE id_paciente = ?`;
    const [result] = await pool.execute(sql, values);
    if (result.affectedRows === 0) return res.status(404).json({ message: "Paciente no encontrado" });
    return res.json({ message: "Paciente actualizado" });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// 6. DELETE /pacientes/:id
async function deletePaciente(req, res) {
  try {
    const id = Number(req.params.id);
    const [result] = await pool.execute("DELETE FROM Paciente WHERE id_paciente = ?", [id]);
    if (result.affectedRows === 0) return res.status(404).json({ message: "Paciente no encontrado" });
    return res.json({ message: "Paciente eliminado" });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

module.exports = { 
    getPacientes, 
    getPacienteById, 
    updatePaciente, 
    deletePaciente, 
    guardarProgreso,
    getProgresoDia 
};