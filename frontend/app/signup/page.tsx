"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api } from "@/lib/api";

export default function SignupPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [birthYear, setBirthYear] = useState("");
  const [gender, setGender] = useState<"MALE" | "FEMALE" | "">("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await api.signup(
        email, password, name,
        birthYear ? parseInt(birthYear) : undefined,
        gender || undefined
      );
      await api.login(email, password);
      router.push("/dashboard");
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "회원가입에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-blue-50 px-6">
      <div className="max-w-lg w-full bg-white rounded-3xl shadow-lg p-10">
        <h1 className="text-4xl font-bold text-blue-800 mb-2 text-center">회원가입</h1>
        <p className="text-lg text-gray-500 text-center mb-8">정보를 입력해 주세요</p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          <div>
            <label className="block text-xl font-semibold text-gray-700 mb-2">이름</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="홍길동"
              required
              className="w-full border-2 border-gray-300 rounded-xl px-5 py-4 text-xl focus:border-blue-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-xl font-semibold text-gray-700 mb-2">이메일</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="example@email.com"
              required
              className="w-full border-2 border-gray-300 rounded-xl px-5 py-4 text-xl focus:border-blue-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-xl font-semibold text-gray-700 mb-2">비밀번호</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호 입력 (8자 이상)"
              required
              minLength={8}
              className="w-full border-2 border-gray-300 rounded-xl px-5 py-4 text-xl focus:border-blue-500 focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xl font-semibold text-gray-700 mb-2">출생연도 (선택)</label>
            <input
              type="number"
              value={birthYear}
              onChange={(e) => setBirthYear(e.target.value)}
              placeholder="예: 1960"
              min={1930}
              max={2005}
              className="w-full border-2 border-gray-300 rounded-xl px-5 py-4 text-xl focus:border-blue-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-xl font-semibold text-gray-700 mb-2">성별 (선택)</label>
            <div className="flex gap-4">
              {(["MALE", "FEMALE"] as const).map((g) => (
                <button
                  key={g}
                  type="button"
                  onClick={() => setGender(g)}
                  className={`flex-1 py-4 text-xl font-bold rounded-xl border-2 transition ${
                    gender === g
                      ? "bg-blue-600 text-white border-blue-600"
                      : "bg-white text-gray-600 border-gray-300 hover:border-blue-400"
                  }`}
                >
                  {g === "MALE" ? "남성" : "여성"}
                </button>
              ))}
            </div>
          </div>

          {error && (
            <div className="bg-red-50 border-2 border-red-300 text-red-700 text-lg px-5 py-4 rounded-xl">
              ⚠️ {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white text-xl font-bold py-5 rounded-2xl hover:bg-blue-700 transition disabled:opacity-50 mt-2"
          >
            {loading ? "가입 중..." : "회원가입"}
          </button>
        </form>

        <p className="text-center text-lg text-gray-500 mt-6">
          이미 계정이 있으신가요?{" "}
          <Link href="/login" className="text-blue-600 font-semibold hover:underline">
            로그인
          </Link>
        </p>
      </div>
    </main>
  );
}
