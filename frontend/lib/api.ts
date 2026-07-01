const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

function getToken() {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("token");
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  let res: Response;
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });
  } catch {
    throw new Error("서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
  }
  const text = await res.text();
  if (!res.ok) {
    if (res.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
      throw new Error("로그인이 만료되었습니다. 다시 로그인해주세요.");
    }
    let message = `오류가 발생했습니다 (${res.status})`;
    try {
      const error = JSON.parse(text);
      if (error.message) message = error.message;
    } catch {}
    throw new Error(message);
  }
  if (!text) return undefined as T;
  return JSON.parse(text) as T;
}

export const api = {
  signup: (email: string, password: string, name: string, birthYear?: number, gender?: "MALE" | "FEMALE") =>
    request("/api/auth/signup", {
      method: "POST",
      body: JSON.stringify({ email, password, name, birthYear, gender }),
    }),

  login: async (email: string, password: string) => {
    const data = await request<{ accessToken: string; name: string }>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    localStorage.setItem("token", data.accessToken);
    localStorage.setItem("name", data.name);
    localStorage.setItem("email", email);
    return data;
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("name");
    localStorage.removeItem("email");
  },

  searchSupplements: (keyword: string, page = 1, size = 10) =>
    request<SupplementSearchResult[]>(
      `/api/supplements/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`
    ),

  saveSupplementToDB: (item: SupplementSearchResult) =>
    request<{ id: number }>("/api/supplements", {
      method: "POST",
      body: JSON.stringify(item),
    }),

  registerUserSupplement: (supplementId: number) =>
    request("/api/user-supplements", {
      method: "POST",
      body: JSON.stringify({ supplementId }),
    }),

  getMySupplements: () => request<UserSupplement[]>("/api/user-supplements"),

  analyze: (ageGroup?: string, gender?: string) =>
    request<AnalysisResponse>("/api/analysis", {
      method: "POST",
      body: JSON.stringify({ ageGroup, gender }),
    }),

  getNutritionSummary: () => request<NutritionSummary>("/api/nutrition/summary"),

  getRecommendations: (symptom: string) =>
    request<RecommendationResponse>(`/api/recommendations?symptom=${symptom}`),

  autoSchedule: () =>
    request<AutoScheduleResponse>("/api/user-supplements/auto-schedule", { method: "POST" }),

  getTodayChecklist: () => request<TodayChecklistResponse>("/api/dosage-records/today"),

  recordDosage: (userSupplementId: number, dosageScheduleId: number, isTaken: boolean) =>
    request("/api/dosage-records", {
      method: "POST",
      body: JSON.stringify({
        userSupplementId,
        dosageScheduleId,
        takenAt: new Date().toISOString(),
        isTaken,
      }),
    }),
};

export interface SupplementSearchResult {
  reportNo: string;
  productName: string;
  manufacturer: string;
  shape: string;
  primaryFunction: string;
  caution: string;
  rawMaterial: string;
}

export interface UserSupplement {
  id: number;
  supplementId: number;
  productName: string;
  isActive: boolean;
}

export interface NutritionSummary {
  ageGroup: string;
  gender: string;
  nutrients: {
    name: string;
    unit: string;
    intake: number;
    recommended: number | null;
    upperLimit: number | null;
    percentage: number;
  }[];
}

export interface AnalysisResponse {
  interactions: {
    ingredient1: string;
    ingredient2: string;
    status: "SYNERGY" | "CAUTION" | "AVOID";
    description: string;
  }[];
  summary: string;
  overallRisk: "LOW" | "MEDIUM" | "HIGH";
}

export interface RecommendationResponse {
  symptom: string;
  symptomLabel: string;
  items: {
    ingredientName: string;
    reason: string;
    alreadySufficient: boolean;
  }[];
}

export interface AutoScheduleResponse {
  items: {
    userSupplementId: number;
    productName: string;
    bucketLabel: string;
    scheduledTime: string;
  }[];
  caution: string | null;
}

export interface TodayChecklistResponse {
  items: {
    dosageScheduleId: number;
    userSupplementId: number;
    productName: string;
    scheduledTime: string;
    bucketLabel: string;
    isTaken: boolean;
  }[];
  caution: string | null;
}

export const SYMPTOMS: { code: string; label: string; emoji: string }[] = [
  { code: "EYE_STRAIN", label: "눈이 침침해요", emoji: "👁️" },
  { code: "JOINT_PAIN", label: "뼈 마디가 쑤셔요", emoji: "🦴" },
  { code: "SLEEP", label: "잠을 깊게 못 자요", emoji: "😴" },
  { code: "MEMORY", label: "기억력이 떨어져요", emoji: "🧠" },
  { code: "FATIGUE", label: "피로하고 기운이 없어요", emoji: "⚡" },
  { code: "BP_SUGAR", label: "혈압/혈당이 걱정돼요", emoji: "❤️" },
];
