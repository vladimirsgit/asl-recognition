from datetime import datetime, timedelta

from pydantic import EmailStr, BaseModel
from sqlmodel import SQLModel, Field
from typing import Optional
import uuid

from app.core.constants import constants


class UserEmailConfirmation(BaseModel):
    email: str
    code: str
