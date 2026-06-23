import json
import os

from google import genai

from app.schemas.analysis import (
    AnalyzeRequest,
    AnalyzeResponse,
    InteractionResult,
    InteractionStatus,
    OverallRisk,
)


class GeminiService:
    def __init__(self):
        self.client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

    async def analyze_interactions(self, request: AnalyzeRequest) -> AnalyzeResponse:
        prompt = self._build_prompt(request)
        response = self.client.models.generate_content(
            model="gemini-2.5-flash", contents=prompt
        )
        return self._parse_response(response.text)

    def _build_prompt(self, request: AnalyzeRequest) -> str:
        supplements_text = ""
        for s in request.supplements:
            if s.ingredients:
                ingredients_text = ", ".join(
                    f"{i.name} {i.amount}{i.unit}" if i.amount else i.name
                    for i in s.ingredients
                )
            else:
                ingredients_text = "성분 정보 없음"
            supplements_text += f"- {s.name}: {ingredients_text}\n"

        user_info_parts = []
        if request.ageGroup:
            user_info_parts.append(f"연령대: {request.ageGroup}")
        if request.gender:
            user_info_parts.append(f"성별: {request.gender}")
        user_info = ", ".join(user_info_parts) if user_info_parts else "정보 없음"

        return f"""당신은 영양제 상호작용 전문가입니다. 다음 영양제들의 상호작용을 분석해주세요.

복용 중인 영양제:
{supplements_text}
사용자 정보: {user_info}

다음 JSON 형식으로만 응답해주세요:
{{
  "interactions": [
    {{
      "ingredient1": "영양소1",
      "ingredient2": "영양소2",
      "status": "SYNERGY | CAUTION | AVOID",
      "description": "상호작용 설명 (한국어, 1-2문장)"
    }}
  ],
  "summary": "전체 복용 패턴에 대한 종합 평가 (한국어, 2-3문장)",
  "overallRisk": "LOW | MEDIUM | HIGH"
}}

status 기준:
- SYNERGY: 함께 복용하면 효과가 증가하는 조합
- CAUTION: 주의가 필요하지만 복용 가능한 조합
- AVOID: 함께 복용을 피해야 하는 조합

상호작용이 없으면 interactions는 빈 배열로 반환하세요.
반드시 유효한 JSON만 반환하고 다른 텍스트는 포함하지 마세요."""

    def _parse_response(self, text: str) -> AnalyzeResponse:
        text = text.strip()
        if text.startswith("```"):
            lines = text.split("\n")
            text = "\n".join(lines[1:-1])

        data = json.loads(text)

        interactions = [
            InteractionResult(
                ingredient1=i["ingredient1"],
                ingredient2=i["ingredient2"],
                status=InteractionStatus(i["status"]),
                description=i["description"],
            )
            for i in data.get("interactions", [])
        ]

        return AnalyzeResponse(
            interactions=interactions,
            summary=data.get("summary", ""),
            overallRisk=OverallRisk(data.get("overallRisk", "LOW")),
        )
