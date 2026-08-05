# 💊 Pill Project — 부모님을 위한 영양제 관리 앱

복용 중인 영양제의 성분을 분석해 과다섭취를 방지하고, AI 기반 궁합 분석과 자동 복용 시간표를 제공하는 웹 애플리케이션입니다.

**배포 주소:** https://pill-project-kohl.vercel.app

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| 영양제 검색 & 등록 | 식품안전나라 공공 API 연동, 검색 후 내 복용 목록에 추가 |
| AI 궁합 분석 | Gemini AI가 성분 간 상호작용을 SYNERGY / CAUTION / AVOID 신호등으로 표시 |
| 자동 복용 시간표 | 성분 특성(공복/식후/저녁)에 따라 최적 시간대 자동 배정, 칼슘+철분 자동 분리 |
| 오늘의 체크리스트 | 오늘 요일에 맞는 복용 스케줄 목록, 체크박스로 복용 기록 |
| 과다섭취 방지 | KDRI 2025 기준 연령대·성별별 권장량 대비 % 계산 및 상한선 경고 |
| 증상별 영양소 추천 | 눈 침침함·관절통·수면·기억력·피로·혈압혈당 6가지 증상별 추천 |

---

## 기술 스택

```
Frontend   Next.js 16 · React 19 · TypeScript · Tailwind CSS 4
Backend    Spring Boot 3.5 · Java 17 · Spring Security · JPA/Hibernate
AI         Google Gemini 2.5 Flash API (Spring Boot에서 직접 호출)
Database   PostgreSQL 15 (Docker)
Auth       JWT (HMAC-SHA256, Stateless)
```

---

## 시스템 구조

```
브라우저 (Next.js :3000)
        │ HTTP + JWT
        ▼
Spring Boot 백엔드 (:8080)
        ├── PostgreSQL DB (Docker :5432)
        └── Google Gemini 2.5 Flash API (직접 호출)
```

> 초기에는 AI 분석을 별도 Python(FastAPI) 서버로 분리했으나, 운영 중 단일 장애점(SPOF)·비용 문제로 Spring Boot에서 Gemini REST API를 직접 호출하는 구조로 전환했습니다.

---

## 로컬 실행 방법

### 사전 준비
- Java 17+
- Node.js 20+
- Docker Desktop

### 1. 환경변수 파일 생성

```bash
copy .env.example .env
# .env 열어서 POSTGRES_USER, POSTGRES_PASSWORD 값 채우기
```

### 2. PostgreSQL 실행 (Docker Compose)

```bash
docker-compose up -d
```

> `docker-compose.yml`에 Redis 컨테이너도 정의돼 있지만 현재 코드에서는 사용하지 않습니다.

### 3. KDRI 2025 시드 데이터 삽입

```bash
# Windows
docker cp platform/src/main/resources/data/seed_kdri2025.sql pill-db:/tmp/seed.sql
docker exec -i pill-db psql -U pilluser -d pilldb -f /tmp/seed.sql
```

> 사용자명/DB명은 `.env`에 설정한 값과 일치해야 합니다.

### 4. Spring Boot 백엔드 실행

`platform/src/main/resources/application-local.properties` 파일을 새로 생성합니다 (`.gitignore` 대상이라 직접 만들어야 함).

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pilldb
spring.datasource.username=<.env의 POSTGRES_USER>
spring.datasource.password=<.env의 POSTGRES_PASSWORD>

jwt.secret=your-jwt-secret-key-32chars-or-more
jwt.expiration=86400000

openapi.base-url=http://openapi.foodsafetykorea.go.kr/api
openapi.secret-key=YOUR_FOODSAFETY_API_KEY

gemini.api.key=YOUR_GEMINI_API_KEY
```

```bash
cd platform
./gradlew bootRun
```

### 5. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 `http://localhost:3000` 접속

---

## 환경변수

### Spring Boot (`application-local.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pilldb
spring.datasource.username=pilluser
spring.datasource.password=pillpass

jwt.secret=your-jwt-secret-key-32chars-or-more
jwt.expiration=86400000

openapi.base-url=http://openapi.foodsafetykorea.go.kr/api
openapi.secret-key=YOUR_FOODSAFETY_API_KEY

gemini.api.key=YOUR_GEMINI_API_KEY
```

### Next.js (`.env.local`)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 배포 (Railway + Vercel)

### 배포 순서

```
1. Railway에 PostgreSQL 인스턴스 생성
2. KDRI 2025 시드 데이터 삽입 (Railway 콘솔에서 SQL 실행)
3. Spring Boot 백엔드 배포 (환경변수 설정)
4. Vercel에 Next.js 프론트엔드 배포
```

### Railway — Spring Boot 환경변수

| 변수명 | 설명 |
|--------|------|
| `DATABASE_URL` | Railway PostgreSQL 연결 문자열 (자동 제공) |
| `JWT_SECRET` | 32자 이상 랜덤 문자열 |
| `OPENAPI_SECRET_KEY` | 식품안전나라 API 키 |
| `GEMINI_API_KEY` | Google Gemini API 키 |

### Vercel — 환경변수

| 변수명 | 설명 |
|--------|------|
| `NEXT_PUBLIC_API_URL` | Spring Boot 배포 URL |

### 배포 시 필수 시드 데이터

```bash
# Railway 콘솔 또는 psql 접속 후 실행
# platform/src/main/resources/data/seed_kdri2025.sql 내용 붙여넣기
```

> `supplement_ingredients`는 사용자가 영양제를 검색·저장할 때 `rawMaterial` 텍스트가 자동 파싱되어 채워집니다. KDRI 시드(ingredients 테이블)가 먼저 들어가 있어야 파싱이 정상 동작합니다.

---

## API 엔드포인트

### 인증
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 → JWT 발급 |

### 영양제
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/supplements/search?keyword=` | 식품안전나라 검색 |
| POST | `/api/supplements` | 영양제 DB 저장 |
| GET | `/api/supplements/{id}` | 영양제 상세 조회 |
| GET | `/api/supplements/{id}/ingredients` | 영양제 성분 목록 조회 |
| POST | `/api/user-supplements` | 내 복용 목록에 추가 |
| GET | `/api/user-supplements` | 내 복용 영양제 목록 |
| DELETE | `/api/user-supplements/{id}` | 복용 중단 |
| POST | `/api/user-supplements/auto-schedule` | 자동 시간표 생성 |
| POST | `/api/user-supplements/{id}/schedules` | 복용 스케줄 추가 |
| GET | `/api/user-supplements/{id}/schedules` | 복용 스케줄 조회 |
| DELETE | `/api/user-supplements/{id}/schedules/{scheduleId}` | 복용 스케줄 삭제 |

### 복용 기록
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/dosage-records/today` | 오늘의 체크리스트 |
| GET | `/api/dosage-records` | 복용 기록 전체 조회 |
| POST | `/api/dosage-records` | 복용 기록 저장 |

### 분석
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/analysis` | AI 궁합 분석 |
| GET | `/api/nutrition/summary` | 영양소 섭취 현황 |
| PUT | `/api/nutrition/ingredient-amount` | 성분 섭취량 수정 |
| GET | `/api/recommendations?symptom=` | 증상별 추천 |

---

## DB 스키마 주요 테이블

```
users                  회원 (email, birth_year, gender, age_group)
supplements            영양제 상품 (식품안전나라 원본 데이터)
ingredients            성분 마스터 (칼슘, 비타민D 등 23종)
supplement_ingredients 영양제별 성분 함량 (자동 파싱)
user_supplements       내 복용 영양제 목록
dosage_schedules       복용 스케줄 (요일·시간)
dosage_records         복용 기록
nutrient_limits        KDRI 2025 기준값 (연령대·성별별 권장량·상한선)
```

---

## 자동 시간표 배정 규칙

| 시간대 | 시각 | 해당 성분 |
|--------|------|-----------|
| 아침 공복 | 07:00 | 유산균, 콜라겐 |
| 아침 식후 | 08:30 | 칼슘, 비타민 D/A/E/K, 오메가3, 루테인, 코엔자임Q10, 아연, 엽산 등 |
| 저녁 | 21:00 | 철분, 마그네슘 |

> 칼슘과 철분이 동시에 있으면 자동으로 분리 배정되며 경고 문구를 표시합니다.

---

## 증상별 추천 매핑

| 증상 | 추천 성분 |
|------|-----------|
| 눈이 침침해요 | 루테인+지아잔틴, 비타민 A |
| 뼈 마디가 쑤셔요 | 칼슘, 비타민 D |
| 잠을 깊게 못 자요 | 마그네슘 |
| 기억력이 떨어져요 | 오메가3 |
| 피로하고 기운이 없어요 | 비타민 B1·B2·B12, 마그네슘 |
| 혈압·혈당이 걱정돼요 | 오메가3, 코엔자임Q10 |

> 이미 충분히 섭취 중인 성분(현재 섭취량 ≥ KDRI 권장량)은 "이미 복용 중"으로 표시됩니다.

---

## 알려진 제한 사항

- 식품안전나라 API가 점검 중일 때 영양제 검색 불가 (외부 서비스 의존)
- 공공 API가 함량을 정형 데이터로 제공하지 않아, 원문 파싱은 근본적으로 추정치 — 미상 성분은 수동 입력으로 보완
- 처방약 충돌 검사 미구현 (OCR 필요)
- 약 사진 판별 미구현 (이미지 인식 필요)
- 브라우저 푸시 알림 미구현 (앱 내 체크리스트만 제공)
