-- 테스트용 supplement_ingredients 삽입 (KDRI 시드 실행 후 사용)
INSERT INTO supplement_ingredients (supplement_id, ingredient_id, amount, unit)
SELECT v.supplement_id, i.id, v.amount, i.unit
FROM (VALUES
  (1, '칼슘',      600.0),
  (1, '비타민 D',    5.0),
  (2, '칼슘',      500.0),
  (2, '비타민 D',    5.0),
  (3, '오메가3',  1000.0),
  (4, '비타민 A',   700.0),
  (4, '비타민 B1',    1.2),
  (4, '비타민 B2',    1.4),
  (4, '비타민 B6',    1.5),
  (4, '비타민 B12',   2.4),
  (4, '비타민 C',    75.0),
  (4, '비타민 D',     5.0),
  (4, '비타민 E',    10.0),
  (4, '마그네슘',   150.0),
  (4, '아연',         8.5),
  (4, '엽산',       400.0),
  (4, '나이아신',    15.0)
) AS v(supplement_id, ingredient_name, amount)
JOIN ingredients i ON i.name = v.ingredient_name
ON CONFLICT DO NOTHING;
