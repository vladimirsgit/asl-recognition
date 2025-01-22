FROM python:3.12.7-slim


WORKDIR /code

COPY requirements.txt ./

RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["uvicorn", "main:app", "--reload", "--reload-delay", "0.1", "--host", "0.0.0.0", "--port", "8000"]