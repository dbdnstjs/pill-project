-- =====================================================
-- PILL 프로젝트 DB 스키마 v2 (PostgreSQL)
-- =====================================================

-- 1. 사용자
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(50),
    birth_year      INT,
    gender          VARCHAR(10),
    age_group       VARCHAR(20),
    height_cm       DECIMAL(5,1),
    weight_kg       DECIMAL(5,1),
    alarm_enabled   BOOLEAN DEFAULT true,
    created_at      TIMESTAMP DEFAULT now(),
    updated_at      TIMESTAMP DEFAULT now()
);

-- 2. 성분
CREATE TABLE ingredients (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    name_en     VARCHAR(100),
    category    VARCHAR(50),
    unit        VARCHAR(20),
    description TEXT
);

-- 3. 영양소 기준 (KDRI 2025)
CREATE TABLE nutrient_limits (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ingredient_id       UUID REFERENCES ingredients(id),
    gender              VARCHAR(10) NOT NULL,
    age_group           VARCHAR(20) NOT NULL,
    recommended_amount  DECIMAL(10,2),
    upper_limit         DECIMAL(10,2),
    unit                VARCHAR(20),
    source              VARCHAR(50) DEFAULT 'KDRI_2025'
);

-- 4. 영양제 제품 (식품안전처 API)
CREATE TABLE supplements (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_no        VARCHAR(50) UNIQUE,
    name             VARCHAR(200) NOT NULL,
    company          VARCHAR(200),
    shape            VARCHAR(50),
    dosage_method    TEXT,
    primary_function TEXT,
    caution          TEXT,
    raw_material     TEXT,
    image_url        VARCHAR(500),
    raw_api_data     JSONB,
    created_at       TIMESTAMP DEFAULT now()
);

-- 5. 영양제-성분 연결 (N:M)
CREATE TABLE supplement_ingredients (
    supplement_id   UUID REFERENCES supplements(id) ON DELETE CASCADE,
    ingredient_id   UUID REFERENCES ingredients(id),
    amount_per_dose DECIMAL(10,3),
    unit            VARCHAR(20),
    PRIMARY KEY (supplement_id, ingredient_id)
);

-- 6. 상호작용 규칙
CREATE TABLE interactions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ingredient_a_id   UUID REFERENCES ingredients(id),
    ingredient_b_id   UUID REFERENCES ingredients(id),
    status            VARCHAR(10) NOT NULL,
    title             VARCHAR(30) NOT NULL,
    description       TEXT NOT NULL,
    recommendation    TEXT,
    source            VARCHAR(100),
    is_ai_generated   BOOLEAN DEFAULT false,
    created_at        TIMESTAMP DEFAULT now()
);

CREATE UNIQUE INDEX unique_interaction_pair
    ON interactions (
        LEAST(ingredient_a_id::text, ingredient_b_id::text),
        GREATEST(ingredient_a_id::text, ingredient_b_id::text)
    );

-- 7. 사용자 영양제 등록
CREATE TABLE user_supplements (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID REFERENCES users(id) ON DELETE CASCADE,
    supplement_id UUID REFERENCES supplements(id),
    is_active     BOOLEAN DEFAULT true,
    start_date    DATE,
    end_date      DATE,
    memo          TEXT,
    created_at    TIMESTAMP DEFAULT now()
);

-- 8. 복용 스케줄
CREATE TABLE dosage_schedules (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_supplement_id UUID REFERENCES user_supplements(id) ON DELETE CASCADE,
    timing             VARCHAR(30),
    timing_label       VARCHAR(50),
    alarm_time         TIME,
    days_of_week       INT[],
    is_active          BOOLEAN DEFAULT true
);

-- 9. 복용 기록
CREATE TABLE dosage_records (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID REFERENCES users(id) ON DELETE CASCADE,
    user_supplement_id UUID REFERENCES user_supplements(id),
    taken_at           TIMESTAMP NOT NULL,
    scheduled_at       TIMESTAMP,
    status             VARCHAR(20),
    note               TEXT
);

-- 10. 처방약
CREATE TABLE medications (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID REFERENCES users(id) ON DELETE CASCADE,
    name           VARCHAR(200) NOT NULL,
    ingredient     VARCHAR(200),
    prescribed_for VARCHAR(100),
    ocr_raw_text   TEXT,
    is_active      BOOLEAN DEFAULT true,
    created_at     TIMESTAMP DEFAULT now()
);

-- 11. 증상 태그
CREATE TABLE symptoms (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name     VARCHAR(50) UNIQUE,
    icon     VARCHAR(10),
    category VARCHAR(30)
);

-- 12. 사용자-증상 연결
CREATE TABLE user_symptoms (
    user_id    UUID REFERENCES users(id) ON DELETE CASCADE,
    symptom_id UUID REFERENCES symptoms(id),
    created_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (user_id, symptom_id)
);

-- 인덱스
CREATE INDEX idx_supplements_name       ON supplements(name);
CREATE INDEX idx_supplements_raw_data   ON supplements USING gin(raw_api_data);
CREATE INDEX idx_user_supplements_user  ON user_supplements(user_id);
CREATE INDEX idx_dosage_records_user    ON dosage_records(user_id);
CREATE INDEX idx_dosage_records_taken   ON dosage_records(taken_at);
CREATE INDEX idx_interactions_status    ON interactions(status);
CREATE INDEX idx_nutrient_limits_lookup ON nutrient_limits(ingredient_id, gender, age_group);