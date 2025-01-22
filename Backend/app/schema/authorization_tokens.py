from pydantic import BaseModel

from app.schema.access_token import AccessToken
from app.schema.refresh_token import RefreshToken


class AuthorizationTokens(BaseModel):
    access_token: AccessToken
    refresh_token: RefreshToken
