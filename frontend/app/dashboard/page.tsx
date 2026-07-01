"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api, UserSupplement, TodayChecklistResponse } from "@/lib/api";
import BottomNav from "@/components/BottomNav";

type ChecklistItem = TodayChecklistResponse["items"][number];

export default function DashboardPage() {
  const router = useRouter();
  const [supplements, setSupplements] = useState<UserSupplement[]>([]);
  const [checklist, setChecklist] = useState<TodayChecklistResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [scheduling, setScheduling] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      router.push("/login");
      return;
    }
    Promise.all([api.getMySupplements(), api.getTodayChecklist()])
      .then(([s, c]) => {
        setSupplements(s);
        setChecklist(c);
      })
      .catch(() => router.push("/login"))
      .finally(() => setLoading(false));
  }, [router]);

  async function handleAutoSchedule() {
    setScheduling(true);
    try {
      await api.autoSchedule();
      const c = await api.getTodayChecklist();
      setChecklist(c);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "시간표 설정에 실패했습니다.");
    } finally {
      setScheduling(false);
    }
  }

  async function handleToggle(item: ChecklistItem) {
    if (!checklist) return;
    const nextTaken = !item.isTaken;
    setChecklist({
      ...checklist,
      items: checklist.items.map((i) =>
        i.dosageScheduleId === item.dosageScheduleId ? { ...i, isTaken: nextTaken } : i
      ),
    });
    try {
      await api.recordDosage(item.userSupplementId, item.dosageScheduleId, nextTaken);
    } catch {
      setChecklist(checklist);
      alert("복용 기록에 실패했습니다.");
    }
  }

  if (loading) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-blue-50">
        <p className="text-2xl text-gray-500">불러오는 중...</p>
      </main>
    );
  }

  const items = checklist?.items ?? [];
  const takenCount = items.filter((i) => i.isTaken).length;

  const grouped = items.reduce<Record<string, ChecklistItem[]>>((acc, item) => {
    (acc[item.bucketLabel] ??= []).push(item);
    return acc;
  }, {});

  return (
    <main className="min-h-screen bg-blue-50 px-6 py-10 pb-28">
      <div className="max-w-2xl mx-auto">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-blue-800 mb-1">오늘의 영양제</h1>
          {items.length > 0 && (
            <p className="text-xl text-gray-500">
              오늘 복용 완료 {takenCount}/{items.length}
            </p>
          )}
        </div>

        {supplements.length === 0 ? (
          <div className="bg-white rounded-3xl p-10 text-center shadow">
            <p className="text-2xl text-gray-500 mb-6">등록된 영양제가 없습니다</p>
            <Link
              href="/supplements"
              className="inline-block bg-blue-600 text-white text-xl font-bold px-10 py-4 rounded-2xl hover:bg-blue-700 transition"
            >
              영양제 검색하기
            </Link>
          </div>
        ) : items.length === 0 ? (
          <div className="bg-white rounded-3xl p-10 text-center shadow">
            <p className="text-2xl text-gray-500 mb-6">아직 복용 시간표가 없어요</p>
            <button
              onClick={handleAutoSchedule}
              disabled={scheduling}
              className="bg-blue-600 text-white text-xl font-bold px-10 py-4 rounded-2xl hover:bg-blue-700 transition disabled:opacity-50"
            >
              {scheduling ? "설정 중..." : "✨ AI 시간표 자동 설정하기"}
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-6">
            {Object.entries(grouped).map(([bucketLabel, bucketItems]) => (
              <div key={bucketLabel}>
                <p className="text-lg font-bold text-gray-500 mb-3">{bucketLabel}</p>
                <div className="flex flex-col gap-3">
                  {bucketItems.map((item) => (
                    <button
                      key={item.dosageScheduleId}
                      onClick={() => handleToggle(item)}
                      className={`flex items-center gap-4 bg-white rounded-2xl px-6 py-5 shadow text-left transition ${
                        item.isTaken ? "opacity-60" : ""
                      }`}
                    >
                      <span className="text-3xl">💊</span>
                      <span
                        className={`flex-1 text-xl font-semibold ${
                          item.isTaken ? "text-gray-400 line-through" : "text-gray-800"
                        }`}
                      >
                        {item.productName}
                      </span>
                      <span
                        className={`w-8 h-8 rounded-full border-2 flex items-center justify-center text-lg ${
                          item.isTaken
                            ? "bg-green-500 border-green-500 text-white"
                            : "border-gray-300 text-transparent"
                        }`}
                      >
                        ✓
                      </span>
                    </button>
                  ))}
                </div>
              </div>
            ))}

            {checklist?.caution && (
              <div className="bg-yellow-50 border-2 border-yellow-300 rounded-2xl p-5">
                <p className="text-lg font-bold text-yellow-800 mb-1">⚠️ 오늘 주의사항</p>
                <p className="text-base text-yellow-800 leading-relaxed">{checklist.caution}</p>
              </div>
            )}
          </div>
        )}

        <div className="flex flex-col gap-4 mt-8">
          <Link
            href="/supplements"
            className="block w-full bg-white text-blue-600 text-xl font-bold py-5 rounded-2xl text-center border-2 border-blue-600 hover:bg-blue-50 transition"
          >
            + 영양제 추가
          </Link>
          <Link
            href="/nutrition"
            className="block w-full bg-green-600 text-white text-xl font-bold py-5 rounded-2xl text-center hover:bg-green-700 transition"
          >
            영양소 섭취 현황 📊
          </Link>
          {supplements.length >= 2 && (
            <Link
              href="/analysis"
              className="block w-full bg-blue-600 text-white text-xl font-bold py-5 rounded-2xl text-center hover:bg-blue-700 transition"
            >
              AI 상호작용 분석하기 🔍
            </Link>
          )}
        </div>
      </div>
      <BottomNav />
    </main>
  );
}
