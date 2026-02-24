const { pool } = require("../db");

// GET /pacientes (solo terapeuta)
async function getPacientes(req, res) {
  try {
    const [rows] = await pool.execute(
      "SELECT id_paciente, nombreP, correoP, estrella FROM Paciente ORDER BY id_paciente DESC"
    );
    return res.json(rows);
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// GET /pacientes/:id (terapeuta o self)
async function getPacienteById(req, res) {
  try {
    const id = Number(req.params.id);
    const [rows] = await pool.execute(
      "SELECT id_paciente, nombreP, correoP, estrella FROM Paciente WHERE id_paciente = ? LIMIT 1",
      [id]
    );
    if (rows.length === 0) return res.status(404).json({ message: "Paciente no encontrado" });
    return res.json(rows[0]);
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// PUT /pacientes/:id (terapeuta o self)
async function updatePaciente(req, res) {
  try {
    const id = Number(req.params.id);
    const { nombreP, correoP, estrella } = req.body;

    const fields = [];
    const values = [];

    if (nombreP !== undefined) { fields.push("nombreP = ?"); values.push(nombreP); }
    if (correoP !== undefined) { fields.push("correoP = ?"); values.push(correoP); }
    if (estrella !== undefined) { fields.push("estrella = ?"); values.push(estrella); }

    if (fields.length === 0) {
      return res.status(400).json({ message: "Nada para actualizar (nombreP/correoP/estrella)" });
    }

    values.push(id);

    const sql = `UPDATE Paciente SET ${fields.join(", ")} WHERE id_paciente = ?`;
    const [result] = await pool.execute(sql, values);

    if (result.affectedRows === 0) return res.status(404).json({ message: "Paciente no encontrado" });

    return res.json({ message: "Paciente actualizado" });
  } catch (err) {
    if (err.code === "ER_DUP_ENTRY") return res.status(409).json({ message: "correoP ya existe" });
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// DELETE /pacientes/:id (solo terapeuta)
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
