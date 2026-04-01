const { pool } = require("../db");

// GET /pacientes (solo los pacientes que pertenecen al terapeuta logueado)
async function getPacientes(req, res) {
  try {
    const { cedula } = req.query; 

    // Validación: Si se envía cédula, que sea un número
    if (cedula && isNaN(cedula)) {
      return res.status(400).json({ message: "La cédula debe ser un valor numérico" });
    }

    let sql = "SELECT id_paciente, nombreP, correoP, estrella, fk_idCedula FROM Paciente";
    const values = [];

    if (cedula) {
      sql += " WHERE fk_idCedula = ?";
      values.push(cedula);
    }

    sql += " ORDER BY id_paciente DESC";

    const [rows] = await pool.execute(sql, values);
    return res.json(rows);
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// GET /pacientes/:id (terapeuta o self)
async function getPacienteById(req, res) {
  try {
    const id = Number(req.params.id);
    
    if (isNaN(id)) return res.status(400).json({ message: "ID de paciente inválido" });

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

// PUT /pacientes/:id (terapeuta o self)
async function updatePaciente(req, res) {
  try {
    const id = Number(req.params.id);
    if (isNaN(id)) return res.status(400).json({ message: "ID de paciente inválido" });

    const { nombreP, correoP, estrella, fk_idCedula } = req.body;

    const fields = [];
    const values = [];

    if (nombreP !== undefined) { fields.push("nombreP = ?"); values.push(nombreP); }
    if (correoP !== undefined) { fields.push("correoP = ?"); values.push(correoP); }
    if (estrella !== undefined) { fields.push("estrella = ?"); values.push(estrella); }
    
    // Validación extra para fk_idCedula si se intenta actualizar
    if (fk_idCedula !== undefined) { 
      if (isNaN(fk_idCedula)) return res.status(400).json({ message: "La cédula debe ser numérica" });
      fields.push("fk_idCedula = ?"); 
      values.push(fk_idCedula); 
    }

    if (fields.length === 0) {
      return res.status(400).json({ message: "Nada para actualizar (nombreP/correoP/estrella/fk_idCedula)" });
    }

    values.push(id);

    const sql = `UPDATE Paciente SET ${fields.join(", ")} WHERE id_paciente = ?`;
    const [result] = await pool.execute(sql, values);

    if (result.affectedRows === 0) return res.status(404).json({ message: "Paciente no encontrado" });

    return res.json({ message: "Paciente actualizado" });
  } catch (err) {
    if (err.code === "ER_DUP_ENTRY") return res.status(409).json({ message: "correoP ya existe" });
    if (err.code === "ER_NO_REFERENCED_ROW_2") return res.status(400).json({ message: "La cédula del terapeuta no existe" });
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

// DELETE /pacientes/:id (solo terapeuta)
async function deletePaciente(req, res) {
  try {
    const id = Number(req.params.id);
    
    if (isNaN(id)) return res.status(400).json({ message: "ID de paciente inválido" });

    const [result] = await pool.execute("DELETE FROM Paciente WHERE id_paciente = ?", [id]);

    if (result.affectedRows === 0) return res.status(404).json({ message: "Paciente no encontrado" });

    return res.json({ message: "Paciente eliminado" });
  } catch (err) {
    return res.status(500).json({ error: err.code, message: err.message });
  }
}

module.exports = { getPacientes, getPacienteById, updatePaciente, deletePaciente };