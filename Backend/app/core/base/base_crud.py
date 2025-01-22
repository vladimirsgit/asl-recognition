from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.db import get_session

class BaseCRUD:
    def __init__(self, db: AsyncSession = Depends(get_session)):
        self.db = db