import anyio
import html
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import logging
from app.config import settings

logger = logging.getLogger("uvicorn.error")


def is_test_environment() -> bool:
    return (settings.ENVIRONMENT or "").strip().lower() == "test"


def is_smtp_configured() -> bool:
    host = (settings.SMTP_HOST or "").strip()
    user = (settings.SMTP_USER or "").strip()
    pw = (settings.SMTP_PASSWORD or "").replace(" ", "").strip()
    if not host or not user or not pw:
        return False
    if "youraddress@" in user.lower() or "your_16_char_app_password" in pw.lower():
        return False
    return True


def _send_smtp_sync(to_email: str, msg_string: str) -> None:
    from_addr = (settings.SMTP_FROM or settings.SMTP_USER or "kidcurateservices@gmail.com").strip()
    if settings.SMTP_PORT == 465:
        server = smtplib.SMTP_SSL(settings.SMTP_HOST.strip(), settings.SMTP_PORT, timeout=12)
    else:
        server = smtplib.SMTP(settings.SMTP_HOST.strip(), settings.SMTP_PORT, timeout=12)
    try:
        if settings.SMTP_PORT in (587, 25):
            server.starttls()
        if settings.SMTP_USER and settings.SMTP_PASSWORD:
            clean_pw = settings.SMTP_PASSWORD.replace(" ", "").strip()
            server.login(settings.SMTP_USER.strip(), clean_pw)
        server.sendmail(from_addr, [to_email], msg_string)
    finally:
        try:
            server.quit()
        except Exception:
            pass


def _clean_display_name(parent_name: str) -> str:
    cleaned = (parent_name or "").strip()
    if not cleaned or cleaned.lower() == "parent":
        return ""
    return cleaned


def build_otp_email_content(code: str, purpose: str = "login", parent_name: str = "") -> tuple[str, str, str]:
    """Build cohesive, app-authentic subject, plain text, and HTML content for 6-digit OTP verification emails."""
    clean_name = _clean_display_name(parent_name)
    safe_name = html.escape(clean_name)
    safe_code = html.escape(code.strip())

    if purpose == "reset_lock":
        subject = "Your KidCurate Parent Lock Reset Code"
        badge_label = "KIDCURATE · PARENT LOCK RECOVERY"
        headline = "Reset Your Parent Lock"
        action_text = "reset your KidCurate Parent Lock"
    elif purpose == "google_signup":
        subject = "Complete Your KidCurate Setup — Verification Code"
        badge_label = "KIDCURATE · FIRST-TIME VERIFICATION"
        headline = "Verify Your Google Account"
        action_text = "verify your Google account and complete your first-time KidCurate setup"
    else:
        subject = "Your KidCurate Verification Code"
        badge_label = "KIDCURATE · PARENT VERIFICATION"
        headline = "Verify Your Parent Account"
        action_text = "sign in and verify your KidCurate Parent Account"

    greeting_plain = f"Hello {clean_name}," if clean_name else "Hello,"
    greeting_html = f"Hello {safe_name}," if safe_name else "Hello,"

    plain_body = (
        f"{greeting_plain}\n\n"
        f"Use the 6-digit one-time verification code below to {action_text}:\n\n"
        f"    {code.strip()}\n\n"
        f"This code expires in 10 minutes and can only be used once.\n"
        f"Never share this code with anyone. If you did not request it, you can safely ignore this email.\n\n"
        f"- The KidCurate Team\n"
        f"Safe & Distraction-Free Video Space for Kids\n"
    )

    html_body = f"""\
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>{html.escape(subject)}</title>
  </head>
  <body style="margin:0; padding:0; background-color:#F4F6FB; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color:#0F172A;">
    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#F4F6FB; padding:32px 16px;">
      <tr>
        <td align="center">
          <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:540px; background-color:#FFFFFF; border-radius:24px; border:1px solid #E2E8F0; overflow:hidden; box-shadow:0 12px 32px rgba(79, 70, 229, 0.08);">
            <!-- Royal Indigo Hero Header -->
            <tr>
              <td style="background-color:#4F46E5; background-image:linear-gradient(135deg, #3730A3 0%, #4F46E5 55%, #6366F1 100%); padding:30px 32px 26px 32px; text-align:left;">
                <div style="display:inline-block; background-color:rgba(255,255,255,0.16); border:1px solid rgba(255,255,255,0.28); border-radius:999px; padding:6px 14px; font-size:11px; font-weight:700; letter-spacing:1.1px; color:#FFFFFF;">
                  {badge_label}
                </div>
                <h1 style="margin:16px 0 6px 0; font-size:24px; line-height:1.25; font-weight:800; color:#FFFFFF; letter-spacing:-0.3px;">
                  {headline}
                </h1>
                <p style="margin:0; font-size:14px; line-height:1.5; color:#E0E7FF;">
                  One-time parent security check to keep your family space protected.
                </p>
              </td>
            </tr>
            <!-- Brand Accent Bar -->
            <tr>
              <td style="padding:0; line-height:0; font-size:0;">
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                    <td width="50%" height="4" style="background-color:#4F46E5;"></td>
                    <td width="25%" height="4" style="background-color:#F59E0B;"></td>
                    <td width="25%" height="4" style="background-color:#10B981;"></td>
                  </tr>
                </table>
              </td>
            </tr>
            <!-- Body -->
            <tr>
              <td style="padding:30px 32px 28px 32px;">
                <p style="margin:0 0 10px 0; font-size:15px; font-weight:600; color:#0F172A;">
                  {greeting_html}
                </p>
                <p style="margin:0 0 22px 0; font-size:14px; line-height:1.65; color:#475569;">
                  Enter the 6-digit verification code below in the KidCurate app to {action_text}:
                </p>

                <!-- OTP Code Card -->
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="margin:0 0 22px 0;">
                  <tr>
                    <td align="center" style="background-color:#EEF2FF; border:1.5px solid #C7D2FE; border-radius:16px; padding:22px 16px;">
                      <div style="font-size:11px; font-weight:700; letter-spacing:1.4px; text-transform:uppercase; color:#4F46E5; margin-bottom:8px;">
                        One-Time Verification Code
                      </div>
                      <div style="font-family:'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace; font-size:34px; font-weight:800; letter-spacing:10px; color:#312E81; padding-left:10px;">
                        {safe_code}
                      </div>
                    </td>
                  </tr>
                </table>

                <!-- Security Notice -->
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#FFFBEB; border:1px solid #FDE68A; border-left:4px solid #F59E0B; border-radius:12px;">
                  <tr>
                    <td style="padding:14px 16px;">
                      <p style="margin:0; font-size:12.5px; line-height:1.55; color:#78350F;">
                        <strong>Security note:</strong> This code expires in <strong>10 minutes</strong> and can only be used once. Never share it with anyone. If you did not request this verification, you may safely ignore this email.
                      </p>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            <!-- Footer -->
            <tr>
              <td style="background-color:#F8FAFC; border-top:1px solid #E2E8F0; padding:18px 32px; text-align:center;">
                <p style="margin:0 0 4px 0; font-size:12px; font-weight:700; color:#4F46E5;">
                  KidCurate &middot; Safe &amp; Distraction-Free
                </p>
                <p style="margin:0; font-size:11px; color:#64748B;">
                  Parent-curated learning playlists built with care for families.
                </p>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </body>
</html>
"""
    return subject, plain_body, html_body


def build_welcome_email_content(parent_name: str = "") -> tuple[str, str, str]:
    """Build an elegant, app-authentic Welcome Note email matching KidCurate's visual identity."""
    clean_name = _clean_display_name(parent_name)
    safe_name = html.escape(clean_name)

    subject = "Welcome to KidCurate — Safe, Curated Screen Time Begins"
    hero_title = f"Welcome to KidCurate, {safe_name}" if safe_name else "Welcome to KidCurate"
    greeting_plain = f"Dear {clean_name}," if clean_name else "Hello,"
    greeting_html = f"Dear {safe_name}," if safe_name else "Welcome,"

    plain_body = (
        f"{greeting_plain}\n\n"
        f"Welcome to KidCurate — your family's calm, distraction-free video space.\n\n"
        f"We built KidCurate so parents can turn screen time into purposeful, joyful learning "
        f"without endless autoplay, distracting sidebars, or unexpected recommendations.\n\n"
        f"How to get started in 3 simple steps:\n"
        f"  1. Create a Child Profile: Add your child's name, pick a playful character avatar, "
        f"and choose instant tap-to-play or a 4-digit child PIN.\n"
        f"  2. Curate Their Safe Playlist: Search trusted educational videos or paste any YouTube link "
        f"directly into their daily lineup.\n"
        f"  3. Lock with Complete Confidence: Protect Parent Zone and fullscreen Child Lock using your "
        f"own independent Pattern, PIN, or Password — completely separate from your phone lockscreen.\n\n"
        f"Built for Peace of Mind:\n"
        f"  - Zero algorithmic rabbit holes or surprise autoplay\n"
        f"  - Daily screen time limits and automatic Bedtime Curfew mode\n"
        f"  - Early Reader karaoke subtitles to support young learners\n\n"
        f"Thank you for welcoming KidCurate into your home.\n\n"
        f"Warm regards,\n"
        f"The KidCurate Team\n"
        f"kidcurateservices@gmail.com\n"
    )

    html_body = f"""\
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>{html.escape(subject)}</title>
  </head>
  <body style="margin:0; padding:0; background-color:#F4F6FB; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color:#0F172A;">
    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#F4F6FB; padding:36px 16px;">
      <tr>
        <td align="center">
          <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="max-width:600px; background-color:#FFFFFF; border-radius:24px; border:1px solid #E2E8F0; overflow:hidden; box-shadow:0 16px 40px rgba(79, 70, 229, 0.10);">

            <!-- Royal Indigo Gradient Hero Header (mirrors KidCurate app identity) -->
            <tr>
              <td style="background-color:#4F46E5; background-image:linear-gradient(135deg, #3730A3 0%, #4F46E5 55%, #6366F1 100%); padding:38px 36px 34px 36px; text-align:left;">
                <div style="display:inline-block; background-color:rgba(255,255,255,0.16); border:1px solid rgba(255,255,255,0.30); border-radius:999px; padding:6px 14px; font-size:11px; font-weight:700; letter-spacing:1.2px; color:#FFFFFF;">
                  KIDCURATE &middot; SAFE &amp; DISTRACTION-FREE
                </div>
                <h1 style="margin:18px 0 10px 0; font-size:28px; line-height:1.22; font-weight:800; color:#FFFFFF; letter-spacing:-0.4px;">
                  {hero_title}
                </h1>
                <p style="margin:0; font-size:15px; line-height:1.6; color:#E0E7FF;">
                  Your verified Parent Account is ready. You now have a calm, parent-curated video sanctuary designed for curious young minds.
                </p>
              </td>
            </tr>

            <!-- Signature Tri-Color Accent Ribbon (Indigo / Sunshine Gold / Mint Emerald) -->
            <tr>
              <td style="padding:0; line-height:0; font-size:0;">
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                    <td width="50%" height="4" style="background-color:#4F46E5;"></td>
                    <td width="25%" height="4" style="background-color:#F59E0B;"></td>
                    <td width="25%" height="4" style="background-color:#10B981;"></td>
                  </tr>
                </table>
              </td>
            </tr>

            <!-- Warm Personal Welcome Note -->
            <tr>
              <td style="padding:32px 36px 12px 36px;">
                <p style="margin:0 0 12px 0; font-size:16px; font-weight:700; color:#0F172A;">
                  {greeting_html}
                </p>
                <p style="margin:0 0 14px 0; font-size:14.5px; line-height:1.7; color:#334155;">
                  Thank you for joining <strong>KidCurate</strong>. Every detail of the app is crafted around a simple promise: <em>you</em> choose what your child watches, so screen time stays safe, educational, and completely free from algorithmic distractions.
                </p>
                <p style="margin:0; font-size:14.5px; line-height:1.7; color:#334155;">
                  Here is how to set up your family's space in just a couple of minutes:
                </p>
              </td>
            </tr>

            <!-- 3-Step Journey Cards (matching KidCurate ThematicAvatar palette) -->
            <tr>
              <td style="padding:16px 36px 12px 36px;">
                <!-- Step 01: Sky Blue -->
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#F8FAFC; border:1px solid #E2E8F0; border-radius:16px; margin-bottom:12px;">
                  <tr>
                    <td width="56" valign="top" style="padding:16px 0 16px 16px;">
                      <div style="width:40px; height:40px; line-height:40px; text-align:center; border-radius:20px; background-color:#E0F2FE; border:2px solid #0284C7; font-size:14px; font-weight:800; color:#0284C7;">
                        01
                      </div>
                    </td>
                    <td valign="top" style="padding:16px 18px 16px 8px;">
                      <div style="font-size:15px; font-weight:700; color:#0F172A; margin-bottom:4px;">
                        Create a Child Profile
                      </div>
                      <div style="font-size:13.5px; line-height:1.55; color:#475569;">
                        Add your child's name, choose a cheerful character avatar, and enable instant tap-to-play or a 4-digit child PIN.
                      </div>
                    </td>
                  </tr>
                </table>

                <!-- Step 02: Violet -->
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#F8FAFC; border:1px solid #E2E8F0; border-radius:16px; margin-bottom:12px;">
                  <tr>
                    <td width="56" valign="top" style="padding:16px 0 16px 16px;">
                      <div style="width:40px; height:40px; line-height:40px; text-align:center; border-radius:20px; background-color:#F3E8FF; border:2px solid #8B5CF6; font-size:14px; font-weight:800; color:#7C3AED;">
                        02
                      </div>
                    </td>
                    <td valign="top" style="padding:16px 18px 16px 8px;">
                      <div style="font-size:15px; font-weight:700; color:#0F172A; margin-bottom:4px;">
                        Curate Their Safe Playlist
                      </div>
                      <div style="font-size:13.5px; line-height:1.55; color:#475569;">
                        Search trusted educational videos or paste any YouTube link directly into today's lineup — nothing else appears on screen.
                      </div>
                    </td>
                  </tr>
                </table>

                <!-- Step 03: Sunshine Gold -->
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#F8FAFC; border:1px solid #E2E8F0; border-radius:16px;">
                  <tr>
                    <td width="56" valign="top" style="padding:16px 0 16px 16px;">
                      <div style="width:40px; height:40px; line-height:40px; text-align:center; border-radius:20px; background-color:#FEF3C7; border:2px solid #D97706; font-size:14px; font-weight:800; color:#B45309;">
                        03
                      </div>
                    </td>
                    <td valign="top" style="padding:16px 18px 16px 8px;">
                      <div style="font-size:15px; font-weight:700; color:#0F172A; margin-bottom:4px;">
                        Hand Over with Complete Confidence
                      </div>
                      <div style="font-size:13.5px; line-height:1.55; color:#475569;">
                        Protect Parent Zone and fullscreen Child Lock using your own independent Pattern, PIN, or Password — completely separate from your phone's lockscreen.
                      </div>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>

            <!-- Built for Peace of Mind Highlight Card (Emerald & Indigo tint) -->
            <tr>
              <td style="padding:16px 36px 8px 36px;">
                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#EEF2FF; border:1px solid #C7D2FE; border-left:4px solid #10B981; border-radius:16px;">
                  <tr>
                    <td style="padding:20px 22px;">
                      <div style="font-size:12px; font-weight:800; letter-spacing:1px; text-transform:uppercase; color:#059669; margin-bottom:10px;">
                        Built for Family Peace of Mind
                      </div>
                      <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                        <tr>
                          <td width="16" valign="top" style="padding:3px 0; font-size:14px; font-weight:800; color:#4F46E5;">&bull;</td>
                          <td style="padding:3px 0; font-size:13.5px; line-height:1.5; color:#1E293B;">
                            <strong>Zero rabbit holes:</strong> No autoplay surprises, comments, or off-topic suggestions.
                          </td>
                        </tr>
                        <tr>
                          <td width="16" valign="top" style="padding:3px 0; font-size:14px; font-weight:800; color:#4F46E5;">&bull;</td>
                          <td style="padding:3px 0; font-size:13.5px; line-height:1.5; color:#1E293B;">
                            <strong>Healthy boundaries:</strong> Set daily screen time limits and automatic Bedtime Curfew hours.
                          </td>
                        </tr>
                        <tr>
                          <td width="16" valign="top" style="padding:3px 0; font-size:14px; font-weight:800; color:#4F46E5;">&bull;</td>
                          <td style="padding:3px 0; font-size:13.5px; line-height:1.5; color:#1E293B;">
                            <strong>Early Reader support:</strong> Optional synchronized subtitles help young children connect spoken words with text.
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>

            <!-- Warm Closing Sign-off -->
            <tr>
              <td style="padding:22px 36px 30px 36px;">
                <p style="margin:0 0 16px 0; font-size:14px; line-height:1.65; color:#475569;">
                  If you ever have feedback or need assistance, simply reply to this email — we are always happy to help.
                </p>
                <p style="margin:0; font-size:14.5px; line-height:1.6; color:#0F172A;">
                  Warm regards,<br />
                  <strong style="color:#4F46E5;">The KidCurate Team</strong>
                </p>
              </td>
            </tr>

            <!-- Elegant Footer -->
            <tr>
              <td style="background-color:#F8FAFC; border-top:1px solid #E2E8F0; padding:20px 36px; text-align:center;">
                <p style="margin:0 0 4px 0; font-size:12.5px; font-weight:700; color:#4F46E5;">
                  KidCurate &middot; Safe &amp; Distraction-Free Video Space for Kids
                </p>
                <p style="margin:0 0 4px 0; font-size:11.5px; color:#64748B;">
                  You are receiving this welcome note because you just verified your new KidCurate Parent Account.
                </p>
                <p style="margin:0; font-size:11px; color:#94A3B8;">
                  kidcurateservices@gmail.com
                </p>
              </td>
            </tr>

          </table>
        </td>
      </tr>
    </table>
  </body>
</html>
"""
    return subject, plain_body, html_body


async def send_otp_verification_email(
    to_email: str,
    code: str,
    purpose: str = "login",
    parent_name: str = "",
) -> bool:
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

    if is_test_environment():
        logger.info(f"[EmailService] Test environment: skipped live OTP SMTP send ({purpose}) to {to_email}")
        return True

    subject, plain_body, html_body = build_otp_email_content(
        code=code,
        purpose=purpose,
        parent_name=parent_name,
    )

    try:
        from_addr = (settings.SMTP_FROM or settings.SMTP_USER or "kidcurateservices@gmail.com").strip()
        msg = MIMEMultipart("alternative")
        msg["Subject"] = subject
        msg["From"] = f"Kid Curate <{from_addr}>"
        msg["To"] = to_email

        msg.attach(MIMEText(plain_body, "plain", "utf-8"))
        msg.attach(MIMEText(html_body, "html", "utf-8"))

        await anyio.to_thread.run_sync(_send_smtp_sync, to_email, msg.as_string())
        logger.info(f"[EmailService] OTP verification code ({purpose}) delivered to {to_email}")
        return True
    except Exception as e:
        logger.error(f"[EmailService] Failed to send OTP email to {to_email}: {e}")
        return False


async def send_welcome_email(to_email: str, parent_name: str = "") -> bool:
    """Send an elegant, app-styled welcome note to newly verified first-time parents.
    Runs blocking SMTP network I/O in a worker thread.
    """
    if not is_smtp_configured():
        logger.info(f"[EmailService] SMTP not configured. Welcome email skipped for {to_email}.")
        return False

    if is_test_environment():
        logger.info(f"[EmailService] Test environment: skipped live welcome SMTP send to {to_email}")
        return True

    subject, plain_body, html_body = build_welcome_email_content(parent_name=parent_name)

    try:
        from_addr = (settings.SMTP_FROM or settings.SMTP_USER or "kidcurateservices@gmail.com").strip()
        msg = MIMEMultipart("alternative")
        msg["Subject"] = subject
        msg["From"] = f"Kid Curate <{from_addr}>"
        msg["To"] = to_email

        msg.attach(MIMEText(plain_body, "plain", "utf-8"))
        msg.attach(MIMEText(html_body, "html", "utf-8"))

        await anyio.to_thread.run_sync(_send_smtp_sync, to_email, msg.as_string())
        logger.info(f"[EmailService] Welcome note delivered to {to_email}")
        return True
    except Exception as e:
        logger.error(f"[EmailService] Failed to send welcome email to {to_email}: {e}")
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

    if is_test_environment():
        logger.info(f"[EmailService] Test environment: skipped live password reset SMTP send to {to_email}")
        return True

    try:
        from_addr = (settings.SMTP_FROM or settings.SMTP_USER or "kidcurateservices@gmail.com").strip()
        msg = MIMEMultipart("alternative")
        msg["Subject"] = "Your KidCurate Password Reset Code"
        msg["From"] = f"Kid Curate <{from_addr}>"
        msg["To"] = to_email

        body = (
            f"Hello,\n\n"
            f"Your KidCurate password reset verification code is:\n\n"
            f"    {code}\n\n"
            f"This code will expire in 15 minutes and can only be used once.\n"
            f"If you did not request this password reset, please ignore this email.\n\n"
            f"- The KidCurate Security Team\n"
        )
        msg.attach(MIMEText(body, "plain", "utf-8"))

        await anyio.to_thread.run_sync(_send_smtp_sync, to_email, msg.as_string())
        logger.info(f"[EmailService] Password reset verification code delivered to {to_email}")
        return True
    except Exception as e:
        logger.error(f"[EmailService] Failed to send password reset email to {to_email}: {e}")
        return False
