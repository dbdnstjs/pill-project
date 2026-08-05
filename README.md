# 💊 Pill Project — 부모님을 위한 영양제 관리 앱

복용 중인 영양제의 성분을 분석해 과다섭취를 방지하고, AI 기반 궁합 분석과 자동 복용 시간표를 제공하는 웹 애플리케이션입니다.

**배포 주소:** https://pill-project-kohl.vercel.app (프론트엔드)

> ⚠️ 백엔드 호스팅(Railway) 무료 크레딧 소진으로 API 서버는 현재 중지된 상태입니다.
> 전체 기능은 아래 [로컬 실행 방법](#로컬-실행-방법)으로 확인하실 수 있습니다.

---

## 빠른 시작

```bash
copy .env.example .env                     # 1. 환경변수 생성 후 값 채우기
docker-compose up -d                       # 2. PostgreSQL 실행
cd platform && ./gradlew bootRun           # 3. 백엔드 (application-local.properties 필요)
cd frontend && npm install && npm run dev  # 4. 프론트엔드
```

브라우저에서 `http://localhost:3000` 접속

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| 영양제 검색 & 등록 | 식품안전나라 공공 API 연동, 검색 후 내 복용 목록에 추가 |
| AI 궁합 분석 | Gemini AI가 성분 간 상호작용을 SYNERGY / CAUTION / AVOID 신호등으로 표시 |
| 자동 복용 시간표 | 성분 특성(공복/식후/저녁)에 따라 최적 시간대 자동 배정, 칼슘+철분 자동 분리 |
| 오늘의 체크리스트 | 오늘 요일에 맞는 복용 스케줄 목록, 체크박스로 복용 기록 |
| 과다섭취 방지 | KDRI 2025 기준 연령대 및 성별별 권장량 대비 % 계산 및 상한선 경고 |
| 증상별 영양소 추천 | 눈 침침함, 관절통, 수면, 기억력, 피로, 혈압혈당 6가지 증상별 추천 |

---

## 기술 스택

```
Frontend   Next.js 16, React 19, TypeScript, Tailwind CSS 4
Backend    Spring Boot 3.5, Java 17, Spring Security, JPA/Hibernate
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

---

## 기술 선택 이유

### Gemini를 Spring Boot에서 직접 호출

초기에는 AI 분석을 별도 Python(FastAPI) 서버로 분리했습니다. AI 생태계 활용을 고려한 선택이었지만, 운영해보니 Python 서버가 단일 장애점(SPOF)이 되어 콜드스타트 시 첫 요청이 8~10초 지연되고 502 오류가 반복됐습니다. 두 서비스를 유지하는 비용 및 운영 부담도 컸습니다.

트레이드오프를 재평가해 Python 서버를 제거하고, Spring Boot에서 Gemini REST API를 직접 호출하는 구조로 전환했습니다.

### Redis 캐싱 미적용

초기에 캐싱용으로 Redis 컨테이너를 준비했지만 제거했습니다.

이 앱은 개인 및 소규모 사용을 목적으로 하고 있어 동시 요청량이 많지 않습니다. 사용자별 데이터를 조회하는 구조라 공유 캐시의 이점도 적었습니다. 캐싱으로 얻는 응답 속도 향상보다 **인프라 비용(배포 시 Redis 애드온)과 코드 복잡도가 더 크다고 판단**해 도입하지 않았습니다.

다만 식품안전나라 API 응답은 캐싱 가치가 있습니다. 동일 키워드 검색이 반복되고 결과가 자주 바뀌지 않으며, 외부 API 장애 시 폴백으로도 활용할 수 있기 때문입니다. 실제로 응답 지연이나 API 장애를 체감하는 시점에 도입할 계획입니다.

### AI와 규칙 기반의 분리

AI를 남용하지 않고 역할을 나눴습니다.

| 구분 | 사용처 | 이유 |
|------|--------|------|
| AI (Gemini) | 궁합 분석 | 성분 조합의 상호작용은 복잡한 판단이 필요 |
| 규칙 기반 | 과다섭취 계산 | KDRI 2025 공식 데이터로 정확성 확보 |
| 규칙 기반 | 증상별 추천 | 검증된 매핑, AI 호출 없이 즉시 응답 |

의학 정보를 다루는 서비스이므로, 검증 가능한 영역은 공식 데이터로 처리해 AI 할루시네이션 위험을 구조적으로 차단했습니다.

---

## 로컬 실행 방법

### 사전 준비
- Java 17+
- Node.js 20+
- Docker Desktop

### 1. 환경변수 파일 생성

```bash
copy .env.example .env
```

`.env` 파일을 열어 값을 채웁니다.

```env
POSTGRES_DB=pilldb
POSTGRES_USER=pilluser
POSTGRES_PASSWORD=pillpass
```

### 2. PostgreSQL 실행

```bash
docker-compose up -d
```

### 3. KDRI 2025 시드 데이터 삽입

```bash
docker cp platform/src/main/resources/data/seed_kdri2025.sql pill-db:/tmp/seed.sql
docker exec -i pill-db psql -U pilluser -d pilldb -f /tmp/seed.sql
```

> 사용자명, DB명은 `.env`에 설정한 값과 일치해야 합니다.
> 이 시드가 없으면 영양제 등록 시 성분 매칭(파싱)이 동작하지 않습니다.

### 4. Spring Boot 백엔드 실행

`platform/src/main/resources/application-local.properties` 파일을 생성합니다.
(`.gitignore` 대상이라 직접 만들어야 합니다.)

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

```bash
cd platform
./gradlew bootRun
```

### 5. 프론트엔드 실행

`frontend/.env.local` 파일을 생성합니다.

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 `http://localhost:3000` 접속

---

## 배포 (Railway + Vercel)

### 배포 순서

```
1. Railway에 PostgreSQL 인스턴스 생성
2. KDRI 2025 시드 데이터 삽입 (Railway 콘솔에서 SQL 실행)
3. Spring Boot 백엔드 배포 (환경변수 설정)
4. Vercel에 Next.js 프론트엔드 배포
5. 백엔드 CORS 설정에 Vercel 도메인 추가
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

> `supplement_ingredients`는 사용자가 영양제를 검색 및 저장할 때 `rawMaterial` 텍스트가 자동 파싱되어 채워집니다. KDRI 시드(`ingredients` 테이블)가 먼저 들어가 있어야 파싱이 정상 동작합니다.

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
dosage_schedules       복용 스케줄 (요일, 시간)
dosage_records         복용 기록
nutrient_limits        KDRI 2025 기준값 (연령대, 성별별 권장량, 상한선)
```

---

## 자동 시간표 배정 규칙

| 시간대 | 시각 | 해당 성분 |
|--------|------|-----------|
| 아침 공복 | 07:00 | 유산균, 콜라겐 |
| 아침 식후 | 08:30 | 칼슘, 비타민 D/A/E/K, 오메가3, 루테인, 코엔자임Q10, 아연, 엽산 등 |
| 저녁 | 21:00 | 철분, 마그네슘 |

> 칼슘과 철분이 동시에 있으면 서로 흡수를 방해하므로 자동으로 분리 배정되며 경고 문구를 표시합니다.

---

## 증상별 추천 매핑

| 증상 | 추천 성분 |
|------|-----------|
| 눈이 침침해요 | 루테인+지아잔틴, 비타민 A |
| 뼈 마디가 쑤셔요 | 칼슘, 비타민 D |
| 잠을 깊게 못 자요 | 마그네슘 |
| 기억력이 떨어져요 | 오메가3 |
| 피로하고 기운이 없어요 | 비타민 B1, B2, B12, 마그네슘 |
| 혈압 및 혈당이 걱정돼요 | 오메가3, 코엔자임Q10 |

> 이미 충분히 섭취 중인 성분(현재 섭취량 ≥ KDRI 권장량)은 "이미 복용 중"으로 표시됩니다.

---

## 알려진 제한 사항

- 식품안전나라 API가 점검 중일 때 영양제 검색 불가 (외부 서비스 의존)
- 공공 API가 함량을 정형 데이터로 제공하지 않아, 원문 파싱은 근본적으로 추정치 — 미상 성분은 수동 입력으로 보완

---

## 향후 개선 계획

- 처방약 충돌 검사 추가 (OCR 필요)
- 약 사진 판별 기능 추가 (이미지 인식 필요)
- 브라우저 푸시 알림 추가 (현재는 앱 내 체크리스트만 제공)
- 테스트 코드 추가 (JUnit + Testcontainers)
- 식품안전나라 API 응답 캐싱 및 장애 폴백 처리
- 성분 파싱 정확도 향상 (실패 케이스 수집 → 정규식 및 별칭 사전 보강)
