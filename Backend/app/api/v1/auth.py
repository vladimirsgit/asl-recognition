import logging

from jwt import PyJWTError
from pydantic import EmailStr

from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks, Body, Form
from fastapi.responses import HTMLResponse

from email_validator import EmailNotValidError
from sqlalchemy.ext.asyncio import AsyncSession
from starlette.requests import Request

from app.core.db import get_session
from app.exception.InvalidCodeError import InvalidCodeError
from app.exception.EmailAlreadyExistsError import EmailAlreadyExistsError
from app.exception.InvalidCredentialsError import InvalidCredentialsError
from app.exception.RefreshTokenExpiredError import RefreshTokenExpiredError
from app.exception.UserNotFoundError import UserNotFoundError
from app.model.user import User
from app.model.user_email_confirmation import UserEmailConfirmation
from app.schema.access_token import AccessToken
from app.schema.change_password_request import ChangePasswordRequest
from app.schema.forgot_password_request import ForgotPasswordRequest
from app.schema.login_request import LogInRequest
from app.schema.authorization_tokens import AuthorizationTokens
from app.schema.refresh_token import RefreshToken
from app.schema.signup_request import  SignUpRequest
from app.service import auth_service

from app.service.auth_service import AuthService

from starlette.status import HTTP_400_BAD_REQUEST, HTTP_404_NOT_FOUND, HTTP_401_UNAUTHORIZED

from app.service.session_service import SessionService
from app.service.user_service import UserService
import app.utils.mailer as mailer
from app.utils.oauth2 import oauth2_scheme
from app.core.dependencies import get_logged_in_user
logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/login", response_model=AuthorizationTokens)
async def login(
        login_request: LogInRequest,
        auth_service: AuthService = Depends(AuthService),
):
    try:
        logger.info(f'Processing log in request for {login_request.email}...')
        return await auth_service.login(login_request)
    except (UserNotFoundError, InvalidCredentialsError):
        raise HTTPException(status_code=HTTP_401_UNAUTHORIZED)


@router.get("/validate_token")
async def validate_token(
        user: User = Depends(get_logged_in_user),
):
    return {"valid"}

@router.post("/logout")
async def logout(
        refresh_token: RefreshToken,
        user: User = Depends(get_logged_in_user),
        auth_service: AuthService = Depends(AuthService)
):
    logger.info(f'Logging out user {user.email}')
    return await auth_service.logout(refresh_token)


@router.post("/refresh_session", response_model=AuthorizationTokens)
async def refresh_session(
        refresh_token: RefreshToken,
        session_service: SessionService = Depends(SessionService),
):
    try:
        return await session_service.validate_refresh_token(refresh_token.token)
    except RefreshTokenExpiredError:
        raise HTTPException(status_code=HTTP_401_UNAUTHORIZED)


@router.post("/signup")
async def signup(
        signup_request: SignUpRequest,
        background_tasks: BackgroundTasks,
        auth_service: AuthService = Depends(AuthService)
):
    logger.info(f"Processing signup request...")
    try:
        email, verification_code = await auth_service.signup(signup_request)
        background_tasks.add_task(mailer.send_verification_mail, email, verification_code)
    except (EmailNotValidError, ValueError, EmailAlreadyExistsError) as e:
        raise HTTPException(status_code=HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/confirm_email/{code}")
async def confirm_email(
        code: str,
        auth_service: AuthService = Depends(AuthService)
):
    logger.info(f"Processing email confirmation request...")
    try:
        await auth_service.confirm_email(code)
    except InvalidCodeError as e:
        raise HTTPException(status_code=HTTP_404_NOT_FOUND, detail=str(e))


@router.post("/forgot_password")
async def forgot_password(
        forgot_password_request: ForgotPasswordRequest,
        background_tasks: BackgroundTasks,
        auth_service: AuthService = Depends(AuthService)
):
    email_token_pair = await auth_service.forgot_password(forgot_password_request.email)
    if email_token_pair:
        background_tasks.add_task(mailer.send_password_change_mail, email_token_pair[0], email_token_pair[1])
    else:
        raise HTTPException(status_code=HTTP_404_NOT_FOUND, detail="Please enter a correct email.")

@router.post("/change_password/{token}")
async def change_password(
        token: str,
        new_password: str = Form(...),
        confirm_password: str = Form(...),
        auth_service: AuthService = Depends(AuthService)
):
    try:
        change_password_request = ChangePasswordRequest(new_password=new_password, confirm_password=confirm_password)
        return await auth_service.change_password(change_password_request, token)
    except ValueError as e:
        raise HTTPException(status_code=HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/change_password/{token}", response_class=HTMLResponse)
async def change_password(
        token: str,
):
    return f"""
    <html>
        <head>
            <title>Password reset</title>
        </head>
       <body style="background-color: #01042d; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0;">
            <div>
                <h2 style="text-align: center; color: white;">Password reset</h2>
                <form id="myform" style="text-align: center" method="post" action="/api/v1/auth/change_password/{token}">
                    <input type="password" name="new_password" placeholder="New password" required>
                    <input type="password" name="confirm_password" placeholder="Confirm password" required>
                    <button type="submit">Reset password</button>
                </form>
            </div>
            <script>
        const form = document.getElementById('myform');
        form.addEventListener('submit', async (e)  => {{
            e.preventDefault();
            const url = form.action;
            const formData = new FormData(form);
            
            const resp = await fetch (url, {{ method: 'POST', body: formData }})
            if (resp.ok) {{
                confirm('Success!');
                }}
            else {{
                alert('Invalid data');
            }}
        }});
            
        </script>
        </body>
        
    </html>
    """
