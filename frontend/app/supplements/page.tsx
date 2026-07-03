"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api, SupplementSearchResult, SupplementIngredientInfo } from "@/lib/api";
import BottomNav from "@/components/BottomNav";

interface AmountModal {
  supplementId: number;
  nutrients: SupplementIngredientInfo[];
  inputs: Record<string, { amount: string; unit: string }>;
}

export default function SupplementsPage() {
  const router = useRouter();
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<SupplementSearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [registering, setRegistering] = useState<string | null>(null);
  const [done, setDone] = useState<Set<string>>(new Set());
  const [error, setError] = useState("");
  const [amountModal, setAmountModal] = useState<AmountModal | null>(null);
  const [saving, setSaving] = useState(false);

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    if (!keyword.trim()) return;
    setError("");
    setSearching(true);
    try {
      const data = await api.searchSupplements(keyword.trim());
      setResults(data);
      if (data.length === 0) setError("검색 결과가 없습니다. 다른 키워드로 검색해보세요.");
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "검색 중 오류가 발생했습니다.");
    } finally {
      setSearching(false);
    }
  }

  async function handleRegister(item: SupplementSearchResult) {
    setRegistering(item.reportNo);
    try {
      const saved = await api.saveSupplementToDB(item);
      await api.registerUserSupplement(saved.id);
      setDone((prev) => new Set(prev).add(item.reportNo));

      // 파싱된 성분 중 함량 미입력 항목 확인
      const ingredients = await api.getSupplementIngredients(saved.id);
      const missing = ingredients.filter((i) => i.amount === null);
      if (missing.length > 0) {
        const inputs: Record<string, { amount: string; unit: string }> = {};
        missing.forEach((i) => { inputs[i.name] = { amount: "", unit: "mg" }; });
        setAmountModal({ supplementId: saved.id, nutrients: missing, inputs });
      }
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "등록에 실패했습니다.");
    } finally {
      setRegistering(null);
    }
  }

  async function handleSaveAmounts() {
    if (!amountModal) return;
    setSaving(true);
    try {
      for (const [name, { amount, unit }] of Object.entries(amountModal.inputs)) {
        if (amount.trim() && !isNaN(Number(amount))) {
          await api.updateIngredientAmount(amountModal.supplementId, name, Number(amount), unit);
        }
      }
      setAmountModal(null);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
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
          <h1 className="text-4xl font-bold text-blue-800">영양제 검색</h1>
        </div>

        <Link
          href="/recommend"
          className="block w-full bg-white rounded-2xl p-5 shadow mb-6 hover:bg-blue-50 transition"
        >
          <p className="text-lg font-bold text-blue-700">🩺 어디가 불편하신가요?</p>
          <p className="text-base text-gray-500 mt-1">증상에 맞는 영양소를 추천받아보세요 →</p>
        </Link>

        <form onSubmit={handleSearch} className="flex gap-3 mb-6">
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="예: 비타민C, 칼슘, 오메가3"
            className="flex-1 border-2 border-gray-300 rounded-xl px-5 py-4 text-xl focus:border-blue-500 focus:outline-none"
          />
          <button
            type="submit"
            disabled={searching}
            className="bg-blue-600 text-white text-xl font-bold px-8 py-4 rounded-xl hover:bg-blue-700 transition disabled:opacity-50"
          >
            {searching ? "검색중" : "검색"}
          </button>
        </form>

        {error && (
          <div className="bg-yellow-50 border-2 border-yellow-300 text-yellow-800 text-lg px-5 py-4 rounded-xl mb-6">
            ⚠️ {error}
          </div>
        )}

        <div className="flex flex-col gap-4">
          {results.map((item) => (
            <div key={item.reportNo} className="bg-white rounded-2xl p-6 shadow">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1">
                  <p className="text-xl font-bold text-gray-800 mb-1">{item.productName}</p>
                  <p className="text-lg text-gray-500 mb-2">{item.manufacturer}</p>
                  {item.primaryFunction && (
                    <p className="text-base text-gray-600 leading-relaxed">
                      {item.primaryFunction.length > 80
                        ? item.primaryFunction.slice(0, 80) + "..."
                        : item.primaryFunction}
                    </p>
                  )}
                </div>
                <button
                  onClick={() => handleRegister(item)}
                  disabled={registering === item.reportNo || done.has(item.reportNo)}
                  className={`flex-shrink-0 text-lg font-bold px-6 py-3 rounded-xl transition ${
                    done.has(item.reportNo)
                      ? "bg-green-100 text-green-700 border-2 border-green-300"
                      : "bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
                  }`}
                >
                  {done.has(item.reportNo)
                    ? "✓ 등록됨"
                    : registering === item.reportNo
                      ? "등록중..."
                      : "등록"}
                </button>
              </div>
            </div>
          ))}
        </div>

        {done.size > 0 && (
          <div className="mt-8">
            <button
              onClick={() => router.push("/dashboard")}
              className="w-full bg-blue-600 text-white text-xl font-bold py-5 rounded-2xl hover:bg-blue-700 transition"
            >
              내 영양제 보기 →
            </button>
          </div>
        )}
      </div>
      <BottomNav />

      {/* 함량 입력 모달 */}
      {amountModal && (
        <div className="fixed inset-0 bg-black/50 flex items-end justify-center z-50 px-4 pb-6">
          <div className="bg-white rounded-3xl w-full max-w-lg p-7 shadow-2xl">
            <h2 className="text-2xl font-bold text-gray-800 mb-2">성분 함량 입력하기</h2>
            <p className="text-base text-gray-500 mb-6">
              제품 라벨을 확인하고 아래 성분의 함량을 입력해주세요.
              <br />모르는 경우 비워두고 건너뛸 수 있습니다.
            </p>

            <div className="flex flex-col gap-4 mb-6 max-h-64 overflow-y-auto">
              {amountModal.nutrients.map((n) => (
                <div key={n.name}>
                  <label className="text-lg font-semibold text-gray-700 mb-2 block">
                    {n.name}
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="number"
                      min="0"
                      step="any"
                      placeholder="숫자 입력"
                      value={amountModal.inputs[n.name]?.amount ?? ""}
                      onChange={(e) =>
                        setAmountModal((prev) =>
                          prev
                            ? {
                                ...prev,
                                inputs: {
                                  ...prev.inputs,
                                  [n.name]: { ...prev.inputs[n.name], amount: e.target.value },
                                },
                              }
                            : prev
                        )
                      }
                      className="flex-1 border-2 border-gray-300 rounded-xl px-4 py-3 text-xl focus:border-blue-500 focus:outline-none"
                    />
                    <select
                      value={amountModal.inputs[n.name]?.unit ?? "mg"}
                      onChange={(e) =>
                        setAmountModal((prev) =>
                          prev
                            ? {
                                ...prev,
                                inputs: {
                                  ...prev.inputs,
                                  [n.name]: { ...prev.inputs[n.name], unit: e.target.value },
                                },
                              }
                            : prev
                        )
                      }
                      className="border-2 border-gray-300 rounded-xl px-3 py-3 text-xl focus:border-blue-500 focus:outline-none"
                    >
                      <option>mg</option>
                      <option>μg</option>
                      <option>g</option>
                      <option>IU</option>
                    </select>
                  </div>
                </div>
              ))}
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setAmountModal(null)}
                className="flex-1 py-4 rounded-2xl border-2 border-gray-300 text-xl font-bold text-gray-600 hover:bg-gray-50 transition"
              >
                건너뛰기
              </button>
              <button
                onClick={handleSaveAmounts}
                disabled={saving}
                className="flex-1 py-4 rounded-2xl bg-blue-600 text-white text-xl font-bold hover:bg-blue-700 transition disabled:opacity-50"
              >
                {saving ? "저장중..." : "저장"}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
