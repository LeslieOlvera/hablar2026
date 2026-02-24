const jwt = require("jsonwebtoken");

const JWT_SECRET = "dev_secret_cambia_esto"; // luego lo mueves a .env

function auth(req, res, next) {
  const h = req.headers.authorization || "";
  if (!h.startsWith("Bearer ")) {
    return res.status(401).json({ message: "No token (Bearer)" });
  }
  const token = h.slice(7);
  try {
    req.user = jwt.verify(token, JWT_SECRET); // {role,id,correo,iat,exp}
    next();
  } catch {
    return res.status(401).json({ message: "Token invalido o expirado" });
  }
}

// Solo terapeuta
function requireTerapeuta(req, res, next) {
  if (req.user.role !== "terapeuta") return res.status(403).json({ message: "Forbidden (solo terapeuta)" });
  next();
}

// Terapeuta o el mismo paciente (por id)
function allowTerapeutaOrSelfPaciente(req, res, next) {
  const idParam = Number(req.params.id);
  if (req.user.role === "terapeuta") return next();
  if (req.user.role === "paciente" && Number(req.user.id) === idParam) return next();
  return res.status(403).json({ message: "Forbidden" });
}

module.exports = { auth, requireTerapeuta, allowTerapeutaOrSelfPaciente, JWT_SECRET };
