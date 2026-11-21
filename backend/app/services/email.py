import anyio
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import logging
from app.config import settings

logger = logging.getLogger("uvicorn.error")


def is_smtp_configured() -> bool:
    return bool(settings.SMTP_HOST and settings.SMTP_HOST.strip() and settings.SMTP_USER and settings.SMTP_PASSWORD)


def _send_smtp_sync(to_email: str, msg_string: str) -> None:
    from_addr = (settings.SMTP_FROM or settings.SMTP_USER or "noreply@kidcurate.com").strip()
    if settings.SMTP_PORT == 465:
        server = smtplib.SMTP_SSL(settings.SMTP_HOST.strip(), settings.SMTP_PORT, timeout=12)
    else:
        server = smtplib.SMTP(settings.SMTP_HOST.strip(), settings.SMTP_PORT, timeout=12)
    try:
        if settings.SMTP_PORT in (587, 25):
            server.starttls()
        if settings.SMTP_USER and settings.SMTP_PASSWORD:
            server.login(settings.SMTP_USER.strip(), settings.SMTP_PASSWORD)
        server.sendmail(from_addr, [to_email], msg_string)
    finally:
        try:
            server.quit()
        except Exception:
            pass


async def send_otp_verification_email(to_email: str, code: str, purpose: str = "login") -> bool:
    """Send a 6-digit OTP verification code to a parent's Gmail/email via configured SMTP.
    Runs blocking SMTP network I/O in a worker thread so the FastAPI event loop never stalls.
    Never logs the raw OTP code.
    """
    if not is_smtp_configured():
        logger.info(
            f"[EmailService] SMTP not configured. OTP verification ({purpose}) requested for {to_email}. "
            "Configure SMTP_HOST=smtp.gmail.com, SMTP_PORT=587, SMTP_USER, and SMTP_PASSWORD (Gmail App Password) in backend/.env."
        )
        return False

    is_lock_reset = purpose == "reset_lock"
    subject = (
        "Your KidCurate Parent Lock Reset Code"
        if is_lock_reset
        else "Your KidCurate Verification Code"
    )
    action_text = (
        "reset your KidCurate Parent Lock"
        if is_lock_reset
        else "sign in and verify your KidCurate Parent Account"
    )

    try:
        from_addr = (settings.SMTP_FROM or settings.SMTP_USER or "noreply@kidcurate.com").strip()
        msg = MIMEMultipart("alternative")
        msg["Subject"] = subject
        msg["From"] = f"KidCurate Security <{from_addr}>"
        msg["To"] = to_email

        plain_body = (
            f"Hello,\n\n"
            f"Use the 6-digit verification code below to {action_text}:\n\n"
            f"    {code}\n\n"
            f"This code expires in 10 minutes and can only be used once.\n"
            f"Never share this code with anyone. If you did not request it, you can safely ignore this email.\n\n"
            f"- The KidCurate Security Team\n"
        )
        html_body = f"""\
<html>
  <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; background:#f8fafc; padding:24px; color:#0f172a;">
    <div style="max-width:460px; margin:0 auto; background:#ffffff; border-radius:16px; border:1px solid #e2e8f0; padding:28px;">
      <h2 style="margin:0 0 8px 0; color:#4f46e5; font-size:20px;">KidCurate Verification</h2>
      <p style="margin:0 0 18px 0; color:#475569; font-size:14px; line-height:1.5;">
        Use this 6-digit one-time verification code to {action_text}:
      </p>
      <div style="background:#eef2ff; border:1px solid #c7d2fe; border-radius:12px; padding:18px; text-align:center; margin:16px 0;">
        <span style="font-size:32px; font-weight:700; letter-spacing:8px; color:#312e81;">{code}</span>
      </div>
      <p style="margin:14px 0 0 0; color:#64748b; font-size:12px; line-height:1.5;">
        This code expires in <strong>10 minutes</strong> and can only be used once. Never share it with anyone.
      </p>
    </div>
  </body>
</html>
"""
        msg.attach(MIMEText(plain_body, "plain"))
        msg.attach(MIMEText(html_body, "html"))

        await anyio.to_thread.run_sync(_send_smtp_sync, to_email, msg.as_string())
        logger.info(f"[EmailService] OTP verification code ({purpose}) delivered to {to_email}")
        return True
    except Exception as e:
        logger.error(f"[EmailService] Failed to send OTP email to {to_email}: {e}")
        return False


async def send_password_reset_email(to_email: str, code: str) -> bool:
    """Send a password reset email using configured SMTP settings.
    If SMTP is not configured in development, logs an audit notice without leaking the code.
    Runs SMTP network I/O in a background worker thread to prevent event loop blocking.
    """
    if not is_smtp_configured():
        logger.info(
            f"[EmailService] SMTP not configured. Password reset requested for {to_email}. "
            "Dispatch skipped; configure SMTP_HOST in production."
        )
        return False

    try:
        from_addr = (settings.SMTP_FROM or settings.SMTP_USER or "noreply@kidcurate.com").strip()
        msg = MIMEMultipart("alternative")
        msg["Subject"] = "Your KidCurate Password Reset Code"
        msg["From"] = f"KidCurate Security <{from_addr}>"
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

        await anyio.to_thread.run_sync(_send_smtp_sync, to_email, msg.as_string())
        logger.info(f"[EmailService] Password reset verification code delivered to {to_email}")
        return True
    except Exception as e:
        logger.error(f"[EmailService] Failed to send password reset email to {to_email}: {e}")
        return False
