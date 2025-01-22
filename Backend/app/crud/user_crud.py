from typing import Optional, List

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.base.base_crud import BaseCRUD
from datetime import datetime
from sqlmodel import select, and_, delete

from app.exception.EmailAlreadyExistsError import EmailAlreadyExistsError
from app.exception.UserNotFoundError import UserNotFoundError
from app.model.user import User

import logging

logger = logging.getLogger(__name__)
class UserCRUD(BaseCRUD):
    async def read(self, email: str) -> Optional[User]:
        stmt = select(User).where(User.email == email)
        res = await self.db.execute(stmt)
        user: Optional[User] = res.scalar_one_or_none()
        if not user:
            error = UserNotFoundError()
            logger.error(error.message)
            raise error
        return user

    async def ensure_email_unique(self, email):
        stmt = select(User).where(User.email == email)
        res = await self.db.execute(stmt)
        res = res.scalar_one_or_none()
        if res:
            error = EmailAlreadyExistsError()
            logger.error(error.message)
            raise error


    async def save_user(self, user: User):
        self.db.add(user)

    async def delete(self, user: User):
        await self.db.delete(user)

    async def confirm_email(self, email: str):
        user: User = await self.read(email)
        user.confirmed = True
        self.db.add(user)


    @staticmethod
    async def delete_multiple_by_email(db: AsyncSession, emails: List[str]):
        stmt = delete(User).where(User.email.in_(emails))
        await db.execute(stmt)

