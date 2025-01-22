from asyncpg.pgproto.pgproto import timedelta
from fastapi import Depends
from sqlmodel.ext.asyncio.session import AsyncSession
import logging
from typing import Tuple, Optional, List
from pydantic import EmailStr
from app.core.base.base_service import BaseService
from app.core.redis_client import redis_client
from app.exception.InvalidCodeError import InvalidCodeError
from app.exception.UserNotFoundError import UserNotFoundError
from app.model.user import User
from app.model.user_email_confirmation import UserEmailConfirmation
from app.schema.signup_request import SignUpRequest
import email_validator as ev
from app.core.constants import constants
import redis.asyncio as redis
from app.service.user_service import UserService
from app.utils.crypto import hash_with_bcrypt, get_email_confirmation_code

logger = logging.getLogger(__name__)


class UserEmailConfirmationService(BaseService):
    def __init__(self, r_client: redis.Redis = Depends(redis_client)):
        self.r_client = r_client

    async def read(self, code) -> str:
        email = await self.r_client.get(code)
        if not email:
            error = InvalidCodeError()
            logger.error(error.message)
            raise error
        return email.decode('utf-8')

    async def code_exists(self,code) -> bool:
        return bool(await self.r_client.get(code))

    async def save(self, user_email_confirmation: UserEmailConfirmation):
        await self.r_client.set(name=user_email_confirmation.code, value=user_email_confirmation.email, ex=constants.EMAIL_VALIDATION_CODE_EXP)

    async def delete(self, code: str):
        await self.r_client.delete(code)
