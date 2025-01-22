import bcrypt
import secrets
import hashlib

from app.core.constants import constants

def hash_with_bcrypt(data: str) -> str:
    salt = bcrypt.gensalt()
    hashed_data = bcrypt.hashpw(data.encode('utf-8'), salt)
    return hashed_data.decode('utf-8')


def hash_with_hashlib(data: str) -> str:
    data = data.encode('utf-8')
    hash_obj = hashlib.sha224(data)

    return hash_obj.hexdigest()


def check_pass(data: str, hashed_pass: str) -> bool:
    return bcrypt.checkpw(data.encode('utf-8'), hashed_pass.encode('utf-8'))


def get_email_confirmation_code() -> str:
    code = ""
    for _ in range(6):
        code += str(secrets.randbelow(10))
    return code

def get_password_change_token() -> str:
    return secrets.token_urlsafe(32)