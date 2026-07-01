import Link from "next/link";

export default function Home() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center bg-blue-50 px-6">
      <div className="max-w-lg w-full text-center">
        <h1 className="text-5xl font-bold text-blue-800 mb-4">💊 필케어</h1>
        <p className="text-2xl text-gray-700 mb-2">영양제 상호작용 분석 서비스</p>
        <p className="text-xl text-gray-500 mb-12">
          복용 중인 영양제를 등록하면 <br />
          AI가 안전한지 알려드립니다
        </p>

        <div className="flex flex-col gap-4">
          <Link
            href="/login"
            className="block w-full bg-blue-600 text-white text-xl font-bold py-5 rounded-2xl text-center hover:bg-blue-700 transition"
          >
            로그인
          </Link>
          <Link
            href="/signup"
            className="block w-full bg-white text-blue-600 text-xl font-bold py-5 rounded-2xl text-center border-2 border-blue-600 hover:bg-blue-50 transition"
          >
            회원가입
          </Link>
        </div>
      </div>
    </main>
  );
}
