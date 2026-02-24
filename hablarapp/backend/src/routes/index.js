const router = require("express").Router();

router.get("/", (req, res) => res.send("API funcionando 🚀"));
router.get("/health", (req, res) => res.json({ ok: true }));

module.exports = router;
