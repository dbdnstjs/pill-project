"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { api, AnalysisResponse } from "@/lib/api";
import BottomNav from "@/components/BottomNav";

const STATUS_LABEL = {
  SYNERGY: { text: "시너지", color: "bg-green-100 text-green-800 border-green-300" },
  CAUTION: { text: "주의", color: "bg-yellow-100 text-yellow-800 border-yellow-300" },
  AVOID: { text: "복용 금지", color: "bg-red-100 text-red-800 border-red-300" },
};

const RISK_LABEL = {
  LOW: { text: "낮음 ✅", color: "text-green-700 bg-green-50 border-green-300" },
  MEDIUM: { text: "보통 ⚠️", color: "text-yellow-700 bg-yellow-50 border-yellow-300" },
  HIGH: { text: "높음 🚨", color: "text-red-700 bg-red-50 border-red-300" },
};

export default function AnalysisPage() {
  const router = useRouter();
  const [result, setResult] = useState<AnalysisResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [scheduling, setScheduling] = useState(false);

  async function handleAnalyze() {
    setError("");
    setLoading(true);
    try {
      const data = await api.analyze();
      setResult(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "분석 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  }

  async function handleAutoSchedule() {
    setScheduling(true);
    try {
      await api.autoSchedule();
      router.push("/dashboard");
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "시간표 설정에 실패했습니다.");
    } finally {
      setScheduling(false);
    }
  }

  const hasRisk = result?.interactions.some(
    (i) => i.status === "CAUTION" || i.status === "AVOID"
  );

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
          <h1 className="text-4xl font-bold text-blue-800">AI 상호작용 분석</h1>
        </div>

        {!result && !loading && (
          <div className="bg-white rounded-3xl p-10 text-center shadow">
            <p className="text-2xl text-gray-600 mb-4">💊</p>
            <p className="text-xl text-gray-600 mb-8 leading-relaxed">
              등록된 영양제들의 상호작용을 <br />
              AI가 분석해드립니다
            </p>
            <button
              onClick={handleAnalyze}
              className="w-full bg-blue-600 text-white text-xl font-bold py-5 rounded-2xl hover:bg-blue-700 transition"
            >
              분석 시작하기
            </button>
          </div>
        )}

        {loading && (
          <div className="bg-white rounded-3xl p-10 text-center shadow">
            <div className="text-5xl mb-4 animate-pulse">🔍</div>
            <p className="text-2xl text-gray-600">AI가 분석하고 있습니다...</p>
            <p className="text-lg text-gray-400 mt-2">잠시만 기다려주세요</p>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border-2 border-red-300 text-red-700 text-lg px-5 py-4 rounded-xl mb-6">
            ⚠️ {error}
          </div>
        )}

        {result && (
          <div className="flex flex-col gap-6">
            <div
              className={`bg-white rounded-3xl p-8 shadow border-2 ${RISK_LABEL[result.overallRisk].color}`}
            >
              <p className="text-lg font-semibold text-gray-500 mb-1">전체 위험도</p>
              <p className={`text-3xl font-bold ${RISK_LABEL[result.overallRisk].color.split(" ")[0]}`}>
                {RISK_LABEL[result.overallRisk].text}
              </p>
            </div>

            <div className="bg-white rounded-3xl p-8 shadow">
              <p className="text-lg font-semibold text-gray-500 mb-3">종합 평가</p>
              <p className="text-xl text-gray-800 leading-relaxed">{result.summary}</p>
            </div>

            {result.interactions.length > 0 && (
              <div className="flex flex-col gap-4">
                <h2 className="text-2xl font-bold text-gray-700">상세 상호작용</h2>
                {result.interactions.map((item, i) => {
                  const label = STATUS_LABEL[item.status];
                  return (
                    <div key={i} className="bg-white rounded-2xl p-6 shadow">
                      <div className="flex items-center gap-3 mb-3">
                        <span className="text-lg font-bold text-gray-800">{item.ingredient1}</span>
                        <span className="text-gray-400">↔</span>
                        <span className="text-lg font-bold text-gray-800">{item.ingredient2}</span>
                        <span
                          className={`ml-auto text-base font-bold px-4 py-1 rounded-full border-2 ${label.color}`}
                        >
                          {label.text}
                        </span>
                      </div>
                      <p className="text-lg text-gray-600 leading-relaxed">{item.description}</p>
                    </div>
                  );
                })}
              </div>
            )}

            {hasRisk && (
              <button
                onClick={handleAutoSchedule}
                disabled={scheduling}
                className="w-full bg-yellow-400 text-yellow-900 text-xl font-bold py-5 rounded-2xl hover:bg-yellow-500 transition disabled:opacity-50"
              >
                {scheduling ? "시간표 설정 중..." : "✨ AI가 최적 시간표로 바꿔드릴게요 →"}
              </button>
            )}

            <button
              onClick={() => { setResult(null); }}
              className="w-full bg-white text-blue-600 text-xl font-bold py-5 rounded-2xl border-2 border-blue-600 hover:bg-blue-50 transition"
            >
              다시 분석하기
            </button>
            <button
              onClick={() => router.push("/dashboard")}
              className="w-full bg-blue-600 text-white text-xl font-bold py-5 rounded-2xl hover:bg-blue-700 transition"
            >
              홈으로 돌아가기
            </button>
          </div>
        )}
      </div>
      <BottomNav />
    </main>
  );
}
