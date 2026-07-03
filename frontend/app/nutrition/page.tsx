"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, NutritionSummary } from "@/lib/api";
import BottomNav from "@/components/BottomNav";

const AGE_LABEL: Record<string, string> = {
  "50-64": "50~64세",
  "65-74": "65~74세",
  "75+": "75세 이상",
};

const UNITS = ["mg", "μg", "g", "IU"];

function BarColor(pct: number): string {
  if (pct >= 150) return "bg-red-500";
  if (pct >= 100) return "bg-orange-400";
  if (pct >= 60) return "bg-green-500";
  return "bg-blue-400";
}

function StatusText(pct: number): { text: string; color: string } {
  if (pct >= 150) return { text: "과다 섭취", color: "text-red-600" };
  if (pct >= 100) return { text: "권장량 충족", color: "text-orange-500" };
  if (pct >= 60) return { text: "양호", color: "text-green-600" };
  return { text: "부족", color: "text-blue-500" };
}

export default function NutritionPage() {
  const router = useRouter();
  const [data, setData] = useState<NutritionSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [editTarget, setEditTarget] = useState<string | null>(null);
  const [editAmount, setEditAmount] = useState("");
  const [editUnit, setEditUnit] = useState("mg");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api
      .getNutritionSummary()
      .then(setData)
      .catch((err) =>
        setError(err instanceof Error ? err.message : "데이터를 불러올 수 없습니다.")
      )
      .finally(() => setLoading(false));
  }, []);

  function openEdit(name: string, unit: string | null) {
    setEditTarget(name);
    setEditAmount("");
    setEditUnit(unit || "mg");
  }

  async function handleSave() {
    if (!editTarget || !editAmount) return;
    const amount = parseFloat(editAmount);
    if (isNaN(amount) || amount <= 0) return;
    setSaving(true);
    try {
      await api.updateIngredientAmount(editTarget, amount, editUnit);
      const refreshed = await api.getNutritionSummary();
      setData(refreshed);
      setEditTarget(null);
    } catch (err) {
      alert(err instanceof Error ? err.message : "저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-blue-50">
        <p className="text-2xl text-gray-500">불러오는 중...</p>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-blue-50 px-6 py-10 pb-28">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center gap-4 mb-8">
          <button
            onClick={() => router.push("/dashboard")}
            className="text-2xl text-blue-600 hover:text-blue-800 transition"
          >
            ←
          </button>
          <h1 className="text-4xl font-bold text-blue-800">영양소 섭취 현황</h1>
        </div>

        {error && (
          <div className="bg-red-50 border-2 border-red-300 text-red-700 text-lg px-5 py-4 rounded-xl mb-6">
            ⚠️ {error}
          </div>
        )}

        {data && (
          <>
            <div className="bg-white rounded-2xl px-6 py-4 shadow mb-6 flex gap-6">
              <span className="text-lg text-gray-600">
                연령대: <strong>{AGE_LABEL[data.ageGroup] ?? data.ageGroup}</strong>
              </span>
              <span className="text-lg text-gray-600">
                성별: <strong>{data.gender === "MALE" ? "남성" : "여성"}</strong>
              </span>
            </div>

            {data.nutrients.length === 0 ? (
              <div className="bg-white rounded-3xl p-10 text-center shadow">
                <p className="text-xl text-gray-500 mb-2">분석할 영양소 정보가 없습니다.</p>
                <p className="text-lg text-gray-400">
                  등록된 영양제에서 성분을 추출하지 못했습니다.
                </p>
              </div>
            ) : (
              <div className="flex flex-col gap-5">
                {data.nutrients.map((n) => {
                  const barColor = BarColor(n.percentage);
                  const status = StatusText(n.percentage);
                  const barWidth = n.hasAmount ? Math.min(n.percentage, 100) : 0;
                  return (
                    <div key={n.name} className="bg-white rounded-2xl p-6 shadow">
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-xl font-bold text-gray-800">{n.name}</span>
                        {n.hasAmount ? (
                          <span className={`text-lg font-semibold ${status.color}`}>
                            {status.text}
                          </span>
                        ) : (
                          <span className="text-base text-gray-400">함량 미입력</span>
                        )}
                      </div>

                      <div className="w-full bg-gray-100 rounded-full h-5 mb-3">
                        <div
                          className={`h-5 rounded-full transition-all ${n.hasAmount ? barColor : "bg-gray-300"}`}
                          style={{ width: `${barWidth}%` }}
                        />
                      </div>

                      <div className="flex justify-between items-center text-base text-gray-500">
                        <span>
                          현재:{" "}
                          {n.hasAmount ? (
                            <strong className="text-gray-800">
                              {n.intake.toFixed(1)} {n.unit}
                            </strong>
                          ) : (
                            <button
                              onClick={() => openEdit(n.name, n.unit)}
                              className="font-semibold text-blue-500 underline underline-offset-2"
                            >
                              미입력 — 직접 입력하기
                            </button>
                          )}
                        </span>
                        {n.hasAmount && n.recommended && (
                          <span>
                            1일 권장:{" "}
                            <strong>
                              {n.recommended} {n.unit}
                            </strong>{" "}
                            ({n.percentage}%)
                          </span>
                        )}
                        {n.hasAmount && n.upperLimit && (
                          <span className="text-red-400">
                            상한: {n.upperLimit} {n.unit}
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            <p className="text-center text-base text-gray-400 mt-8">
              * KDRI 2025 기반 · 라벨 표시량 기준 추정치이며, 함량 미표기 성분은 직접 입력해 주세요
            </p>
          </>
        )}
      </div>

      {/* 입력 모달 */}
      {editTarget && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-6">
          <div className="bg-white rounded-3xl p-8 w-full max-w-sm shadow-xl">
            <h2 className="text-2xl font-bold text-gray-800 mb-1">{editTarget}</h2>
            <p className="text-lg text-gray-500 mb-6">
              제품 라벨의 1일 섭취량을 입력해 주세요
            </p>

            <div className="flex gap-3 mb-6">
              <input
                type="number"
                value={editAmount}
                onChange={(e) => setEditAmount(e.target.value)}
                placeholder="예: 500"
                className="flex-1 border-2 border-gray-300 rounded-xl px-4 py-4 text-xl focus:border-blue-500 focus:outline-none"
                min="0"
                step="any"
                autoFocus
              />
              <select
                value={editUnit}
                onChange={(e) => setEditUnit(e.target.value)}
                className="border-2 border-gray-300 rounded-xl px-3 py-4 text-xl focus:border-blue-500 focus:outline-none"
              >
                {UNITS.map((u) => (
                  <option key={u} value={u}>{u}</option>
                ))}
              </select>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setEditTarget(null)}
                className="flex-1 border-2 border-gray-300 text-gray-600 text-xl font-bold py-4 rounded-xl hover:bg-gray-50 transition"
              >
                취소
              </button>
              <button
                onClick={handleSave}
                disabled={saving || !editAmount}
                className="flex-1 bg-blue-600 text-white text-xl font-bold py-4 rounded-xl hover:bg-blue-700 disabled:opacity-50 transition"
              >
                {saving ? "저장 중..." : "저장"}
              </button>
            </div>
          </div>
        </div>
      )}

      <BottomNav />
    </main>
  );
}
