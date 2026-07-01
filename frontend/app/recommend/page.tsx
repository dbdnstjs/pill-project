"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { api, RecommendationResponse, SYMPTOMS } from "@/lib/api";
import BottomNav from "@/components/BottomNav";

export default function RecommendPage() {
  const router = useRouter();
  const [selected, setSelected] = useState<string | null>(null);
  const [result, setResult] = useState<RecommendationResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSelect(code: string) {
    setSelected(code);
    setError("");
    setLoading(true);
    try {
      const data = await api.getRecommendations(code);
      setResult(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "추천을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen bg-blue-50 px-6 py-10 pb-28">
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center gap-4 mb-8">
          <button
            onClick={() => router.push("/supplements")}
            className="text-2xl text-blue-600 hover:text-blue-800 transition"
          >
            ←
          </button>
          <div>
            <h1 className="text-4xl font-bold text-blue-800">영양소 추천</h1>
            <p className="text-lg text-gray-500 mt-1">어디가 불편하신가요?</p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 mb-8">
          {SYMPTOMS.map((s) => (
            <button
              key={s.code}
              onClick={() => handleSelect(s.code)}
              className={`rounded-2xl p-6 text-center shadow transition ${
                selected === s.code
                  ? "bg-blue-600 text-white"
                  : "bg-white text-gray-800 hover:bg-blue-50"
              }`}
            >
              <div className="text-3xl mb-2">{s.emoji}</div>
              <div className="text-lg font-bold leading-snug">{s.label}</div>
            </button>
          ))}
        </div>

        {loading && (
          <div className="bg-white rounded-3xl p-10 text-center shadow">
            <p className="text-2xl text-gray-500">추천을 찾고 있어요...</p>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border-2 border-red-300 text-red-700 text-lg px-5 py-4 rounded-xl mb-6">
            ⚠️ {error}
          </div>
        )}

        {result && !loading && (
          <div className="bg-white rounded-3xl p-8 shadow">
            <p className="text-lg font-semibold text-gray-500 mb-1">AI 맞춤 추천</p>
            <p className="text-2xl font-bold text-gray-800 mb-5">{result.symptomLabel}</p>
            <div className="flex flex-col gap-4">
              {result.items.map((item, i) => (
                <div key={i} className="border-b border-gray-100 pb-4 last:border-0 last:pb-0">
                  <span
                    className={`inline-block text-base font-bold px-4 py-1 rounded-full border-2 mb-2 ${
                      item.alreadySufficient
                        ? "bg-gray-100 text-gray-500 border-gray-300"
                        : "bg-green-100 text-green-800 border-green-300"
                    }`}
                  >
                    {item.alreadySufficient ? `✓ ${item.ingredientName} (충분히 섭취 중)` : item.ingredientName}
                  </span>
                  <p className="text-base text-gray-600 leading-relaxed">{item.reason}</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
      <BottomNav />
    </main>
  );
}
