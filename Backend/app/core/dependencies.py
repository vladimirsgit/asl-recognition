from fastapi import Depends, HTTPException
import logging

from jwt import PyJWTError
from starlette.status import HTTP_401_UNAUTHORIZED

from app.exception.UserNotFoundError import UserNotFoundError
from app.model.user import User
from app.service.session_service import SessionService
from app.service.user_service import UserService
from app.utils.oauth2 import oauth2_scheme

logger = logging.getLogger(__name__)

async def get_logged_in_user(token: str = Depends(oauth2_scheme), user_service: UserService = Depends(UserService)):
    try:
        payload = SessionService.validate_access_token(token)
    except PyJWTError:
        raise HTTPException(status_code=HTTP_401_UNAUTHORIZED)

    email = payload.get('sub')
    if not email:
        raise HTTPException(status_code=HTTP_401_UNAUTHORIZED)

    try:
        user: User = await user_service.read(email)
        if not user.confirmed:
            raise HTTPException(status_code=HTTP_401_UNAUTHORIZED)
    except UserNotFoundError:
        raise HTTPException(status_code=HTTP_401_UNAUTHORIZED)

    return user