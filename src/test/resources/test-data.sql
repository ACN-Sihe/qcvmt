INSERT INTO t_user (keycloak_id, qcid, name, role, parent, createtime)
VALUES ('test-uuid-1', 'QC83', 'testuser', 'qcvmt-user', 'admin', NOW());

INSERT INTO t_vessel (vesselid, deck_hold, bay, row_start, row_end, tier_start, tier_end)
VALUES ('V123456', 'H', '17H', '1', '19', '82', '90');

INSERT INTO t_col_set (boxcase, color)
VALUES ('EMPTY', 'white');
