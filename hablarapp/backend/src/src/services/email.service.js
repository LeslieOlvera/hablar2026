const nodemailer = require("nodemailer");

const smtpPort = Number(process.env.SMTP_PORT || 465);
const smtpSecure = String(process.env.SMTP_SECURE || "true") === "true";

const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST,
  port: smtpPort,
  secure: smtpSecure,
  auth: {
    user: process.env.SMTP_USER,
    pass: process.env.SMTP_PASS,
  },
});

async function enviarCorreo({ to, subject, html, text }) {
  if (!process.env.SMTP_USER || !process.env.SMTP_PASS) {
    throw new Error("Faltan credenciales SMTP en el archivo .env");
  }

  return transporter.sendMail({
    from: process.env.SMTP_FROM || process.env.SMTP_USER,
    to,
    subject,
    text,
    html,
  });
}

async function enviarCodigoRecuperacion({ to, nombre, codigo, tipoUsuario }) {
  const subject = "Código de recuperación - Hablar App";

  const text = `
Hola ${nombre || tipoUsuario || ""}.

Tu código de recuperación es: ${codigo}

Este código es temporal. Si no solicitaste recuperar tu contraseña, ignora este correo.

Hablar App
`;

  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 520px; margin: 0 auto; color: #333;">
      <h2 style="color: #6A4CFF;">Código de recuperación</h2>

      <p>Hola ${nombre || tipoUsuario || ""},</p>

      <p>Recibimos una solicitud para recuperar tu contraseña en <strong>Hablar App</strong>.</p>

      <p>Tu código de recuperación es:</p>

      <div style="
        font-size: 28px;
        font-weight: bold;
        letter-spacing: 4px;
        background: #F2F0FF;
        color: #4B32C3;
        padding: 16px;
        text-align: center;
        border-radius: 10px;
        margin: 20px 0;
      ">
        ${codigo}
      </div>

      <p>Ingresa este código en la aplicación para continuar con el cambio de contraseña.</p>

      <p style="font-size: 13px; color: #777;">
        Si no solicitaste este código, puedes ignorar este correo.
      </p>

      <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />

      <p style="font-size: 12px; color: #999;">
        Hablar App
      </p>
    </div>
  `;

  return enviarCorreo({
    to,
    subject,
    text,
    html,
  });
}

module.exports = {
  enviarCorreo,
  enviarCodigoRecuperacion,
};