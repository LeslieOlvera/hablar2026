const router = require("express").Router();
const { signupTerapeuta, loginTerapeuta } = require("../controllers/auth.terapeuta.controller");

router.post("/signup", signupTerapeuta);
router.post("/login", loginTerapeuta);

module.exports = router;
