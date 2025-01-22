from dotenv import load_dotenv
import os

load_dotenv()
class Config:
    # DATABASE
    DATABASE_HOST = os.getenv("DATABASE_HOST")
    DATABASE_PORT = os.getenv("DATABASE_PORT")
    DATABASE_USERNAME = os.getenv("POSTGRES_USER")
    DATABASE_PASSWORD = os.getenv("POSTGRES_PASSWORD")
    DATABASE_NAME = os.getenv("POSTGRES_DB")

    # EMAIL
    EMAIL_HOST = os.getenv("EMAIL_HOST")
    EMAIL_PORT = os.getenv("EMAIL_PORT")
    EMAIL_USERNAME = os.getenv("EMAIL_USERNAME")
    EMAIL_PASSWORD = os.getenv("EMAIL_PASSWORD")

    # JWT
    SECRET_KEY = os.getenv("SECRET_KEY")
    ALGORITHM = os.getenv("ALGORITHM")
