from http.client import HTTPException
import logging
from fastapi import Depends, APIRouter

from app.model.user import User
from app.core.dependencies import get_logged_in_user

router = APIRouter()

logger = logging.getLogger(__name__)
@router.get("", response_model=str)
async def check_status(
        user: User = Depends(get_logged_in_user)
):
    logger.info("Checking server status...")
    return "Up and running"

