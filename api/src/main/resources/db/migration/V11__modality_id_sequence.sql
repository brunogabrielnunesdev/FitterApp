CREATE SEQUENCE modalities_id_seq AS SMALLINT START WITH 1;
SELECT setval('modalities_id_seq', COALESCE((SELECT MAX(id) FROM modalities), 0) + 1, false);
