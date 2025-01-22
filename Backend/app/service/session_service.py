import secrets
from os import access

import jwt
import uuid
import logging
from datetime import datetime, timedelta
import redis.asyncio as redis
from asyncpg.pgproto.pgproto import timedelta
from fastapi import Depends

from app.core.base.base_service import BaseService
from app.core.redis_client import redis_client
from app.core.config import Config
from app.exception.RefreshTokenExpiredError import RefreshTokenExpiredError
from app.schema.access_token import AccessToken
from app.schema.authorization_tokens import AuthorizationTokens
from app.schema.refresh_token import RefreshToken
from app.utils.crypto import hash_with_hashlib
from app.core.constants import constants
logger = logging.getLogger(__name__)

class SessionService(BaseService):
    def __init__(self, r_client: redis.Redis = Depends(redis_client)):
        self.r_client = r_client


    @staticmethod
    def generate_access_token(email: str) -> AccessToken:
        payload = {
            "sub": email,
            "exp": datetime.now() + constants.ACCESS_TOKEN_EXP
        }
        return AccessToken(token=jwt.encode(payload, Config.SECRET_KEY, Config.ALGORITHM))

    @staticmethod
    def generate_refresh_token() -> RefreshToken:
        return RefreshToken(token=secrets.token_urlsafe(32))


    @staticmethod
    def generate_tokens_after_login(email)-> AuthorizationTokens:
        access_token: AccessToken = SessionService.generate_access_token(email)
        refresh_token: RefreshToken = SessionService.generate_refresh_token()

        return AuthorizationTokens(access_token=access_token, refresh_token=refresh_token)


    @staticmethod
    def validate_access_token(token: str):
        payload = jwt.decode(token, Config.SECRET_KEY, algorithms=[Config.ALGORITHM])
        return payload


    async def validate_refresh_token(self, refresh_token: str) -> AuthorizationTokens:
        hashed_token = hash_with_hashlib(refresh_token)
        email_value = await self.r_client.get(hashed_token)
        if not email_value:
            error = RefreshTokenExpiredError()
            logger.error(error.message)
            raise error
        email_value = email_value.decode('utf-8')

        new_refresh_token = SessionService.generate_refresh_token()
        await self.r_client.delete(hashed_token)
        hashed_new_refresh_token = hash_with_hashlib(new_refresh_token.token)

        await self.r_client.set(name=hashed_new_refresh_token, value=email_value,  ex=int(constants.REFRESH_TOKEN_EXP.total_seconds()))

        new_access_token = SessionService.generate_access_token(email_value)

        return AuthorizationTokens(access_token=new_access_token, refresh_token=new_refresh_token)



