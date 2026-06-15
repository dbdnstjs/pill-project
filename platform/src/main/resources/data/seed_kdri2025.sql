-- =====================================================
-- PILL 프로젝트 영양소 기준치 시드 데이터
-- 출처: 한국인 영양소 섭취기준 (KDRI 2025) — 50세 이상
-- 상한값 NULL = 설정된 상한 없음
-- 실행: psql -d pilldb -f seed_kdri2025.sql
-- 멱등성 보장: 여러 번 실행해도 안전
-- =====================================================

BEGIN;

-- =====================================================
-- 1. 영양소(ingredients) 업서트
-- =====================================================
INSERT INTO ingredients (name, description, unit) VALUES
  ('비타민 C',        '피부·면역·항산화',         'mg'),
  ('비타민 D',        '뼈·칼슘흡수·면역',          'μg'),
  ('비타민 A',        '눈건강·면역',               'μgRAE'),
  ('비타민 E',        '항산화·혈관건강',            'mg'),
  ('비타민 K',        '혈액응고·뼈',               'μg'),
  ('비타민 B1',       '에너지대사·신경',            'mg'),
  ('비타민 B2',       '에너지대사',                'mg'),
  ('비타민 B6',       '단백질대사·신경',            'mg'),
  ('비타민 B12',      '빈혈예방·신경',              'μg'),
  ('엽산',            '세포생성·빈혈예방',          'μgDFE'),
  ('나이아신',        '에너지대사·피부',            'mg NE'),
  ('칼슘',            '뼈·치아·근육수축',           'mg'),
  ('마그네슘',        '근육이완·수면·항산화',       'mg'),
  ('철분',            '빈혈예방·산소이동',          'mg'),
  ('아연',            '면역·상처치유·미각',         'mg'),
  ('셀레늄',          '항산화·갑상선',              'μg'),
  ('요오드',          '갑상선호르몬',               'μg'),
  ('구리',            '철흡수·항산화',              'μg'),
  ('인',              '뼈·에너지대사',              'mg'),
  ('오메가3',         '항염증성지방·심혈관',        'mg'),
  ('칼륨',            '혈압조절·심장',              'mg'),
  ('루테인+지아잔틴', '눈건강·황반변성예방',        'mg'),
  ('코엔자임Q10',     '심장·에너지·항산화',         'mg')
ON CONFLICT (name) DO UPDATE SET
  description  = EXCLUDED.description,
  unit = EXCLUDED.unit;

-- =====================================================
-- 2. 기존 nutrient_limits 초기화 (재실행 안전)
-- =====================================================
DELETE FROM nutrient_limits
WHERE ingredient_id IN (
  SELECT id FROM ingredients
  WHERE name IN (
    '비타민 C','비타민 D','비타민 A','비타민 E','비타민 K',
    '비타민 B1','비타민 B2','비타민 B6','비타민 B12','엽산','나이아신',
    '칼슘','마그네슘','철분','아연','셀레늄','요오드','구리','인',
    '오메가3','칼륨','루테인+지아잔틴','코엔자임Q10'
  )
);

-- =====================================================
-- 3. 영양소 기준치 삽입
-- gender: MALE / FEMALE  |  age_group: 50-64 / 65-74 / 75+
-- =====================================================

-- 비타민 C (mg) — 모든 그룹 동일
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender::text, rec, upper_val, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   100.0, 2000.0),
    ('65-74','MALE',   100.0, 2000.0),
    ('75+',  'MALE',   100.0, 2000.0),
    ('50-64','FEMALE', 100.0, 2000.0),
    ('65-74','FEMALE', 100.0, 2000.0),
    ('75+',  'FEMALE', 100.0, 2000.0)
  ) AS t(age_group, gender, rec, upper_val)
WHERE ingredients.name = '비타민 C';

-- 비타민 D (μg)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, upper_val, 'μg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',    10.0, 100.0),
    ('65-74','MALE',    15.0, 100.0),
    ('75+',  'MALE',    15.0, 100.0),
    ('50-64','FEMALE',  10.0, 100.0),
    ('65-74','FEMALE',  15.0, 100.0),
    ('75+',  'FEMALE',  15.0, 100.0)
  ) AS t(age_group, gender, rec, upper_val)
WHERE ingredients.name = '비타민 D';

-- 비타민 A (μgRAE)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, upper_val, 'μgRAE'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   750.0, 3000.0),
    ('65-74','MALE',   700.0, 3000.0),
    ('75+',  'MALE',   700.0, 3000.0),
    ('50-64','FEMALE', 600.0, 3000.0),
    ('65-74','FEMALE', 600.0, 3000.0),
    ('75+',  'FEMALE', 600.0, 3000.0)
  ) AS t(age_group, gender, rec, upper_val)
WHERE ingredients.name = '비타민 A';

-- 비타민 E (mg) — 모든 그룹 동일
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 12.0, 540.0, 'mg'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '비타민 E';

-- 비타민 K (μg) — 상한 없음
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, NULL, 'μg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   75.0),
    ('65-74','MALE',   75.0),
    ('75+',  'MALE',   75.0),
    ('50-64','FEMALE', 65.0),
    ('65-74','FEMALE', 65.0),
    ('75+',  'FEMALE', 65.0)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '비타민 K';

-- 비타민 B1 (mg) — 상한 없음
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, NULL, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   1.2),
    ('65-74','MALE',   1.1),
    ('75+',  'MALE',   1.1),
    ('50-64','FEMALE', 1.1),
    ('65-74','FEMALE', 1.0),
    ('75+',  'FEMALE', 0.8)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '비타민 B1';

-- 비타민 B2 (mg) — 상한 없음
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, NULL, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   1.5),
    ('65-74','MALE',   1.4),
    ('75+',  'MALE',   1.4),
    ('50-64','FEMALE', 1.2),
    ('65-74','FEMALE', 1.1),
    ('75+',  'FEMALE', 1.0)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '비타민 B2';

-- 비타민 B6 (mg)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, 50.0, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   1.5),
    ('65-74','MALE',   1.5),
    ('75+',  'MALE',   1.5),
    ('50-64','FEMALE', 1.4),
    ('65-74','FEMALE', 1.4),
    ('75+',  'FEMALE', 1.4)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '비타민 B6';

-- 비타민 B12 (μg) — 상한 없음, 모든 그룹 동일
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 2.4, NULL, 'μg'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '비타민 B12';

-- 엽산 (μgDFE) — 모든 그룹 동일
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 400.0, 1000.0, 'μgDFE'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '엽산';

-- 나이아신 (mg NE)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, 850.0, 'mg NE'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   14.0),
    ('65-74','MALE',   13.0),
    ('75+',  'MALE',   12.0),
    ('50-64','FEMALE', 13.0),
    ('65-74','FEMALE', 12.0),
    ('75+',  'FEMALE', 11.0)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '나이아신';

-- 칼슘 (mg)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, upper_val, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   800.0, 2500.0),
    ('65-74','MALE',   800.0, 2500.0),
    ('75+',  'MALE',   800.0, 2500.0),
    ('50-64','FEMALE', 750.0, 2000.0),
    ('65-74','FEMALE', 750.0, 2000.0),
    ('75+',  'FEMALE', 750.0, 2000.0)
  ) AS t(age_group, gender, rec, upper_val)
WHERE ingredients.name = '칼슘';

-- 마그네슘 (mg) — 보충제 기준 상한 350mg
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, 350.0, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   380.0),
    ('65-74','MALE',   380.0),
    ('75+',  'MALE',   380.0),
    ('50-64','FEMALE', 280.0),
    ('65-74','FEMALE', 280.0),
    ('75+',  'FEMALE', 280.0)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '마그네슘';

-- 철분 (mg)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, 45.0, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   8.0),
    ('65-74','MALE',   8.0),
    ('75+',  'MALE',   7.0),
    ('50-64','FEMALE', 7.0),
    ('65-74','FEMALE', 6.0),
    ('75+',  'FEMALE', 6.0)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '철분';

-- 아연 (mg)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, 35.0, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   10.0),
    ('65-74','MALE',    9.0),
    ('75+',  'MALE',    9.0),
    ('50-64','FEMALE',  8.0),
    ('65-74','FEMALE',  7.0),
    ('75+',  'FEMALE',  7.0)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '아연';

-- 셀레늄 (μg) — 모든 그룹 동일
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 60.0, 400.0, 'μg'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '셀레늄';

-- 요오드 (μg) — 모든 그룹 동일
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 150.0, 2400.0, 'μg'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '요오드';

-- 구리 (μg)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, 10000.0, 'μg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   850.0),
    ('65-74','MALE',   800.0),
    ('75+',  'MALE',   800.0),
    ('50-64','FEMALE', 650.0),
    ('65-74','FEMALE', 600.0),
    ('75+',  'FEMALE', 600.0)
  ) AS t(age_group, gender, rec)
WHERE ingredients.name = '구리';

-- 인 (mg)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, rec, upper_val, 'mg'
FROM ingredients,
  (VALUES
    ('50-64','MALE',   650.0, 3500.0),
    ('65-74','MALE',   650.0, 3500.0),
    ('75+',  'MALE',   650.0, 3000.0),
    ('50-64','FEMALE', 650.0, 3500.0),
    ('65-74','FEMALE', 650.0, 3500.0),
    ('75+',  'FEMALE', 650.0, 3000.0)
  ) AS t(age_group, gender, rec, upper_val)
WHERE ingredients.name = '인';

-- 오메가3/EPA+DHA (mg) — 모든 그룹 동일, 상한 없음
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 250.0, NULL, 'mg'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '오메가3';

-- 칼륨 (mg) — 모든 그룹 동일, 상한 없음(이뇨제 복용시 주의)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 3500.0, NULL, 'mg'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '칼륨';

-- 루테인+지아잔틴 (mg) — 모든 그룹 동일, 상한 없음(참고치)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 10.0, NULL, 'mg'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '루테인+지아잔틴';

-- 코엔자임Q10 (mg) — 모든 그룹 동일, 상한 없음(참고치)
INSERT INTO nutrient_limits (ingredient_id, age_group, gender, recommended_amount, upper_limit, unit)
SELECT id, age_group, gender, 100.0, NULL, 'mg'
FROM ingredients,
  (VALUES ('50-64','MALE'),('65-74','MALE'),('75+','MALE'),
          ('50-64','FEMALE'),('65-74','FEMALE'),('75+','FEMALE')
  ) AS t(age_group, gender)
WHERE ingredients.name = '코엔자임Q10';

COMMIT;

-- 결과 확인
SELECT i.name, n.gender, n.age_group, n.recommended_amount, n.upper_limit, n.unit
FROM nutrient_limits n
JOIN ingredients i ON i.id = n.ingredient_id
ORDER BY i.name, n.gender, n.age_group;
