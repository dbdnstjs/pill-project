"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import BottomNav from "@/components/BottomNav";

export default function ProfilePage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      router.push("/login");
      return;
    }
    setName(localStorage.getItem("name") ?? "");
    setEmail(localStorage.getItem("email") ?? "");
  }, [router]);

  function handleLogout() {
    api.logout();
    router.push("/");
  }

  return (
    <main className="min-h-screen bg-blue-50 px-6 py-10 pb-28">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-4xl font-bold text-blue-800 mb-8">내 정보</h1>

        <div className="bg-white rounded-3xl p-8 shadow mb-8">
          <div className="flex items-center gap-4 mb-6">
            <span className="text-5xl">👤</span>
            <div>
              <p className="text-2xl font-bold text-gray-800">{name || "사용자"}</p>
              <p className="text-lg text-gray-500">{email}</p>
            </div>
          </div>
        </div>

        <button
          onClick={handleLogout}
          className="w-full bg-white text-red-500 text-xl font-bold py-5 rounded-2xl border-2 border-red-300 hover:bg-red-50 transition"
        >
          로그아웃
        </button>
      </div>
      <BottomNav />
    </main>
  );
}
