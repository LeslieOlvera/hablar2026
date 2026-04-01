const { pool } = require("../db");

// GET /pacientes (solo los pacientes del terapeuta que hace la petición)
async function getPacientes(req, res) {
  try {
    // Si usas el middleware de autenticación, el id del terapeuta viene en req.user.id
    // Aquí asumo que recibes la cedula por query o por el token
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

// GET /pacientes/:id
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

// PUT /pacientes/:id
async function updatePaciente(req, res) {
  try {
    const id = Number(req.params.id);
    const { nombreP, correoP, estrella, fk_idCedula } = req.body;

    const fields = [];
    const values = [];

    if (nombreP !== undefined) { fields.push("nombreP = ?"); values.push(nombreP); }
    if (correoP !== undefined) { fields.push("correoP = ?"); values.push(correoP); }
    if (estrella !== undefined) { fields.push("estrella = ?"); values.push(estrella); }
    // Permitir cambiar de terapeuta si fuera necesario
    if (fk_idCedula !== undefined) { fields.push("fk_idCedula = ?"); values.push(fk_idCedula); }

    if (fields.length === 0) {
      return res.status(400).json({ message: "Nada para actualizar" });
    }

    values.push(id);

    const sql = `UPDATE Paciente SET ${fields.join(", ")} WHERE id_paciente = ?`;
    const [result] = await pool.execute(sql, values);

    if (result.affectedRows === 0) return res.status(404).json({ message: "Paciente no encontrado" });

    return res.json({ message: "Paciente actualizado" });
  } catch (err) {
    if (err.code === "ER_NO_REFERENCED_ROW_2") {
        return res.status(400).json({ message: "La cédula del terapeuta no existe" });
    }
    if (err.code === "ER_DUP_ENTRY") return res.status(409).json({ message: "correoP ya existe" });
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// DELETE /pacientes/:id
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

module.exports = { getPacientes, getPacienteById, updatePaciente, deletePaciente };