from dotenv import load_dotenv

load_dotenv()

from fastapi import FastAPI

from app.routers import analysis

app = FastAPI(title="PILL AI Server", version="1.0.0")

app.include_router(analysis.router, prefix="/analyze", tags=["analysis"])


@app.get("/health")
def health_check():
    return {"status": "ok"}
