from datetime import datetime

from pydantic import EmailStr, BaseModel
from sqlmodel import SQLModel, Field
from typing import Optional
import uuid

from app.core.constants import constants


class User(SQLModel, table=True):
    __tablename__ = 'users'

    id: uuid.UUID = Field(default_factory=uuid.uuid4, primary_key=True)
    email: str = Field(nullable=False, max_length=constants.EMAIL_MAX_LEN)
    password: str = Field(nullable=False, min_length=constants.PASS_MIN_LEN)
    confirmed: bool = Field(nullable=False, default=False)

