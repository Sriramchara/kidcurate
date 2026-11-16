import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import logging
from app.config import settings

logger = logging.getLogger("uvicorn.error")


async def send_password_reset_email(to_email: str, code: str) -> bool:
    """Send a password reset email using configured SMTP settings.
    If SMTP is not configured in development, logs an audit notice without leaking the code.
    """
    if not settings.SMTP_HOST or not settings.SMTP_HOST.strip():
        logger.info(
            f"[EmailService] SMTP not configured. Password reset requested for {to_email}. "
            "Dispatch skipped; configure SMTP_HOST in production."
        )
        return False

    try:
        msg = MIMEMultipart("alternative")
        msg["Subject"] = "Your KidCurate Password Reset Code"
        msg["From"] = settings.SMTP_FROM
        msg["To"] = to_email

        body = (
            f"Hello,\n\n"
            f"Your KidCurate password reset verification code is:\n\n"
            f"    {code}\n\n"
            f"This code will expire in 15 minutes and can only be used once.\n"
            f"If you did not request this password reset, please ignore this email.\n\n"
            f"- The KidCurate Security Team\n"
        )
        msg.attach(MIMEText(body, "plain"))

        server = smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT, timeout=10)
        if settings.SMTP_PORT in (587, 25):
            server.starttls()
        if settings.SMTP_USER and settings.SMTP_PASSWORD:
            server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
        server.sendmail(settings.SMTP_FROM, [to_email], msg.as_string())
        server.quit()
        logger.info(f"[EmailService] Password reset verification code delivered to {to_email}")
        return True
    except Exception as e:
        logger.error(f"[EmailService] Failed to send password reset email to {to_email}: {e}")
        return False
