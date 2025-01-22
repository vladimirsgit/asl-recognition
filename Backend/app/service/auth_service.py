from datetime import datetime, timedelta
from fastapi import HTTPException
import bcrypt
import redis
from fastapi import Depends
from sqlmodel.ext.asyncio.session import AsyncSession
import logging
from typing import Tuple, Optional
from pydantic import EmailStr
from app.core.base.base_service import BaseService
from app.exception.EmailAlreadyExistsError import EmailAlreadyExistsError
from app.exception.InvalidCredentialsError import InvalidCredentialsError
from app.exception.UserNotFoundError import UserNotFoundError
from app.model.user import User
from app.model.user_email_confirmation import UserEmailConfirmation
from app.schema.access_token import AccessToken
from app.schema.change_password_request import ChangePasswordRequest
from app.schema.login_request import LogInRequest
from app.schema.authorization_tokens import AuthorizationTokens
from app.schema.refresh_token import RefreshToken
from app.schema.signup_request import SignUpRequest
import email_validator as ev
from app.core.constants import constants
from app.service.session_service import SessionService
from app.service.user_email_confirmation_service import UserEmailConfirmationService
from app.service.user_service import UserService
from app.utils.crypto import hash_with_bcrypt, get_email_confirmation_code, check_pass, hash_with_hashlib, get_password_change_token
from app.core.redis_client import redis_client
from starlette.status import HTTP_400_BAD_REQUEST

logger = logging.getLogger(__name__)

class AuthService(BaseService):
    def __init__(self, user_email_confirmation_service: UserEmailConfirmationService = Depends(UserEmailConfirmationService),
                 user_service: UserService = Depends(UserService),
                 r_client: redis.Redis = Depends(redis_client)):
        self.user_email_confirmation_service = user_email_confirmation_service
        self.user_service = user_service
        self.r_client = r_client


    async def logout(self, refresh_token: RefreshToken):
        await self.r_client.delete(hash_with_hashlib(refresh_token.token))


    async def login(self, login_request: LogInRequest) -> AuthorizationTokens:
        email, password = login_request.email, login_request.password
        user: User = await self.user_service.read(email)

        if not check_pass(password, user.password) or not user.confirmed:
            error = InvalidCredentialsError()
            logger.error(error.message)
            raise error

        authorization_tokens: AuthorizationTokens = SessionService.generate_tokens_after_login(email)
        hashed_refresh_token = hash_with_hashlib(authorization_tokens.refresh_token.token)

        await self.r_client.set(name=hashed_refresh_token, value=email, ex=int(constants.REFRESH_TOKEN_EXP.total_seconds()))

        return authorization_tokens


    async def signup(self, sign_up_request: SignUpRequest) -> Tuple[str, str]:
        email, password = await self.validate_signup(sign_up_request)
        hashed_password = hash_with_bcrypt(password)
        await self.user_service.save(User(email=email, password=hashed_password, confirmed=False))

        verification_code = get_email_confirmation_code()
        hashed_code = hash_with_hashlib(verification_code)
        code_exists: bool = await self.user_email_confirmation_service.code_exists(hashed_code)
        while code_exists:
            code_exists = await self.user_email_confirmation_service.code_exists(hashed_code)
            verification_code = get_email_confirmation_code()
            hashed_code = hash_with_hashlib(verification_code)

        user_email_confirmation: UserEmailConfirmation = UserEmailConfirmation(email=email, code=hashed_code)
        await self.user_email_confirmation_service.save(user_email_confirmation)
        return email, verification_code


    async def validate_signup(self, sign_up_request: SignUpRequest) -> Tuple[str, str]:
        email = sign_up_request.email
        initial_password = sign_up_request.initial_password
        confirmed_password = sign_up_request.confirmed_password

        email = ev.validate_email(email).normalized

        await self.user_service.ensure_email_unique(email)
        await self.__validate_password(initial_password, confirmed_password)

        return email, initial_password


    async def __validate_password(self, initial_password, confirmed_password):
        pass_err_msg = ""
        if initial_password != confirmed_password:
            pass_err_msg = "Passwords don't match."
        if len(initial_password) > constants.PASS_MAX_LEN:
            pass_err_msg = "Password too long. Max 32 characters."
        if len(initial_password)  < constants.PASS_MIN_LEN:
            pass_err_msg = "Password too short. Min is 4 characters."
        if pass_err_msg:
            value_error = ValueError(pass_err_msg)
            logger.error(value_error)
            raise value_error


    async def confirm_email(self, code):
        hashed_code = hash_with_hashlib(code)
        email: str = await self.user_email_confirmation_service.read(hashed_code)
        await self.user_email_confirmation_service.delete(hashed_code)
        await self.user_service.confirm_email(email)


    async def forgot_password(self, email: str) -> Optional[Tuple[str, str]]:
        try:
            user: User = await self.user_service.read(email)
        except UserNotFoundError:
            return None
        if not user.confirmed:
            return None
        token = get_password_change_token()
        hashed_token = hash_with_hashlib(token)
        await self.r_client.set(name=hashed_token, value=email, ex=int(constants.PASSWORD_CHANGE_TOKEN_EXP.total_seconds()))

        return user.email, token


    async def change_password(self, change_password_request: ChangePasswordRequest, token: str):
        hashed_token: str = hash_with_hashlib(token)
        email = await self.r_client.get(hashed_token)
        if not email:
            raise HTTPException(status_code=HTTP_400_BAD_REQUEST)

        email = email.decode()

        await self.__validate_password(change_password_request.new_password, change_password_request.confirm_password)
        hashed_password = hash_with_bcrypt(change_password_request.new_password)
        user = await self.user_service.read(email)
        user.password = hashed_password
        await self.user_service.save(user)
        await self.r_client.delete(hashed_token)
