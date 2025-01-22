import asyncio
from contextlib import asynccontextmanager

from fastapi import FastAPI, APIRouter, Request, Depends
from starlette.responses import JSONResponse
from app.core.db import init_db
from app.core.redis_client import redis_client
import logging

from app.api.v1 import auth, health_status
logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s: %(levelname)s: %(name)s - %(message)s"
)

logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    await redis_client.init_redis()
    yield
    await redis_client.close_conn()


app = FastAPI(lifespan=lifespan)

api_router = APIRouter(prefix="/api/v1")
api_router.include_router(health_status.router, prefix="/status", tags=["status"])
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
app.include_router(api_router)

@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    logger.error(exc)
    return JSONResponse(
        status_code=500,
        content={"message": "An unexpected error occurred. Please try again."},
    )
