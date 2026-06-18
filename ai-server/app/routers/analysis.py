from fastapi import APIRouter, HTTPException

from app.schemas.analysis import AnalyzeRequest, AnalyzeResponse
from app.services.gemini_service import GeminiService

router = APIRouter()
gemini_service = GeminiService()


@router.post("/interactions", response_model=AnalyzeResponse)
async def analyze_interactions(request: AnalyzeRequest):
    if len(request.supplements) < 2:
        raise HTTPException(
            status_code=400, detail="상호작용 분석을 위해 2개 이상의 영양제가 필요합니다."
        )
    try:
        return await gemini_service.analyze_interactions(request)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"분석 중 오류가 발생했습니다: {str(e)}")
