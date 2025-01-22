import smtplib
from email.mime.text import MIMEText
import ssl

from app.core.config import Config
from app.model.user_email_confirmation import UserEmailConfirmation


def send_verification_mail(email, verification_code):
    message = MIMEText(f'''
<p>Hello,</p>
<p>Thank you for registering! To complete your registration, please enter the following code in the earlier app prompt: <p style="font-size: 30px; font-weight: bold; letter-spacing: 4px;  background-color: #000000; color: #FFFFFF;">{verification_code}</p> to confirm your email address.</p>
<p>If you did not request this, please ignore this email.</p>
<p>Thanks,<br>ASL Team</p>
''', "html")

    message["From"] = Config.EMAIL_USERNAME
    message["To"] = email
    message["Subject"] = "Email confirmation"

    ctx = ssl.create_default_context()

    with smtplib.SMTP_SSL(Config.EMAIL_HOST, Config.EMAIL_PORT, context=ctx) as server:
        server.login(Config.EMAIL_USERNAME, Config.EMAIL_PASSWORD)
        server.send_message(message)

def send_password_change_mail(email, token):
    message = MIMEText(f'''
    <p>Hello,</p>
    <p>Please click <a href="https://192.168.43.114:443/api/v1/auth/change_password/{token}"> here </a> to change your password.</p>
    <p>If you did not request this, please ignore this email.</p>
    <p>Thanks,<br>ASL Team</p>
    ''', "html")

    message["From"] = Config.EMAIL_USERNAME
    message["To"] = email
    message["Subject"] = "Change password request"

    ctx = ssl.create_default_context()

    with smtplib.SMTP_SSL(Config.EMAIL_HOST, Config.EMAIL_PORT, context=ctx) as server:
        server.login(Config.EMAIL_USERNAME, Config.EMAIL_PASSWORD)
        server.send_message(message)
