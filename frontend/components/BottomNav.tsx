"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const TABS = [
  { href: "/dashboard", label: "홈", emoji: "🏠" },
  { href: "/supplements", label: "영양제", emoji: "💊" },
  { href: "/analysis", label: "궁합", emoji: "🔍" },
  { href: "/profile", label: "내정보", emoji: "👤" },
];

export default function BottomNav() {
  const pathname = usePathname();

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t-2 border-gray-100 shadow-[0_-2px_10px_rgba(0,0,0,0.05)] z-50">
      <div className="max-w-2xl mx-auto grid grid-cols-4">
        {TABS.map((tab) => {
          const active = pathname === tab.href;
          return (
            <Link
              key={tab.href}
              href={tab.href}
              className={`flex flex-col items-center gap-1 py-3 text-sm font-bold transition ${
                active ? "text-blue-600" : "text-gray-400"
              }`}
            >
              <span className="text-2xl">{tab.emoji}</span>
              {tab.label}
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
