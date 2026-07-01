"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api, SupplementSearchResult } from "@/lib/api";
import BottomNav from "@/components/BottomNav";

export default function SupplementsPage() {
  const router = useRouter();
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<SupplementSearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [registering, setRegistering] = useState<string | null>(null);
  const [done, setDone] = useState<Set<string>>(new Set());
  const [error, setError] = useState("");

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
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "등록에 실패했습니다.");
    } finally {
      setRegistering(null);
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
    </main>
  );
}
