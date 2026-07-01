from enum import Enum
from typing import Optional

from pydantic import BaseModel


class InteractionStatus(str, Enum):
    SYNERGY = "SYNERGY"
    CAUTION = "CAUTION"
    AVOID = "AVOID"


class OverallRisk(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class IngredientInfo(BaseModel):
    name: str
    amount: Optional[float] = None
    unit: Optional[str] = None


class SupplementInfo(BaseModel):
    name: str
    ingredients: list[IngredientInfo] = []
    rawMaterial: Optional[str] = None
    primaryFunction: Optional[str] = None


class AnalyzeRequest(BaseModel):
    supplements: list[SupplementInfo]
    ageGroup: Optional[str] = None
    gender: Optional[str] = None


class InteractionResult(BaseModel):
    ingredient1: str
    ingredient2: str
    status: InteractionStatus
    description: str


class AnalyzeResponse(BaseModel):
    interactions: list[InteractionResult]
    summary: str
    overallRisk: OverallRisk
