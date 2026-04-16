const multer = require("multer");
const path = require("path");
const fs = require("fs");

function asegurarCarpeta(ruta) {
  if (!fs.existsSync(ruta)) {
    fs.mkdirSync(ruta, { recursive: true });
  }
}

const storageFonetico = multer.diskStorage({
  destination: (req, file, cb) => {
    const ruta = path.join(__dirname, "../uploads/foneticos");
    asegurarCarpeta(ruta);
    cb(null, ruta);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname) || ".m4a";
    cb(null, `audio_${Date.now()}${ext}`);
  },
});

const storageOrofacial = multer.diskStorage({
  destination: (req, file, cb) => {
    const ruta = path.join(__dirname, "../uploads/orofaciales");
    asegurarCarpeta(ruta);
    cb(null, ruta);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname) || ".jpg";
    cb(null, `imagen_${Date.now()}${ext}`);
  },
});

const uploadFonetico = multer({ storage: storageFonetico });
const uploadOrofacial = multer({ storage: storageOrofacial });

module.exports = {
  uploadFonetico,
  uploadOrofacial,
};