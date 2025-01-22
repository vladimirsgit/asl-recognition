from pydantic import BaseModel


class SignUpRequest(BaseModel):
    email: str
    initial_password: str
    confirmed_password: str