from fastapi.params import Depends
import logging
from typing import Tuple, Optional, List
from pydantic import EmailStr
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.base.base_service import BaseService
from app.crud.user_crud import UserCRUD
from app.exception.UserNotFoundError import UserNotFoundError
from app.model.user import User
from app.model.user_email_confirmation import UserEmailConfirmation
from app.schema.access_token import AccessToken
from app.schema.signup_request import SignUpRequest
import email_validator as ev
from app.core.constants import constants
from app.service.session_service import SessionService
from app.utils.crypto import hash_with_bcrypt, get_email_confirmation_code
from app.utils.oauth2 import oauth2_scheme

logger = logging.getLogger(__name__)

class UserService(BaseService):
    def __init__(self, user_crud: UserCRUD = Depends(UserCRUD)):
        self.user_crud = user_crud

    async def read(self, email: str) -> User:
        return await self.user_crud.read(email)

    async def ensure_email_unique(self, email: str):
        await self.user_crud.ensure_email_unique(email)

    async def save(self, user: User):
        return await self.user_crud.save_user(user)

    async def delete(self, user: User):
        await self.user_crud.delete(user)

    async def confirm_email(self, email: str):
        await self.user_crud.confirm_email(email)

    @staticmethod
    async def delete_multiple_by_email(db: AsyncSession, emails: List[str]):
        await UserCRUD.delete_multiple_by_email(db, emails)


