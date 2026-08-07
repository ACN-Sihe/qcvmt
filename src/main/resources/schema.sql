CREATE TABLE IF NOT EXISTS t_user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    keycloak_id VARCHAR(36) UNIQUE,
    qcid VARCHAR(20),
    name VARCHAR(20),
    role VARCHAR(10),
    parent VARCHAR(20),
    createtime DATETIME,
    UNIQUE KEY uk_username (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_vessel (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vesselid VARCHAR(10),
    deck_hold VARCHAR(10),
    bay VARCHAR(10),
    row_start VARCHAR(10),
    row_end VARCHAR(10),
    tier_start VARCHAR(10),
    tier_end VARCHAR(10),
    version INT DEFAULT 0,
    UNIQUE KEY uk_vessel (vesselid, deck_hold, bay)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_col_set (
    id INT AUTO_INCREMENT PRIMARY KEY,
    boxcase VARCHAR(20),
    color VARCHAR(20),
    version INT DEFAULT 0,
    UNIQUE KEY uk_boxcase (boxcase)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_vessel_col (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vesselid VARCHAR(10),
    deck_hold VARCHAR(10),
    bay VARCHAR(10),
    row_start VARCHAR(10),
    row_end VARCHAR(10),
    tier_start VARCHAR(10),
    tier_end VARCHAR(10),
    version INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_vessel_refuel (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vesselid VARCHAR(10),
    is_refuel VARCHAR(10),
    version INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_cell_matrix (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(10),
    `row` VARCHAR(10),
    tier VARCHAR(10),
    tier_start VARCHAR(10),
    tier_end VARCHAR(10),
    active VARCHAR(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_showlog (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userid INT,
    username VARCHAR(20),
    qcid VARCHAR(20),
    login_time DATETIME,
    operation VARCHAR(20),
    INDEX idx_userid_logintime (userid, login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_operation_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userid INT,
    username VARCHAR(20),
    function_name VARCHAR(50),
    action_type VARCHAR(20),
    old_values TEXT,
    new_values TEXT,
    timestamp DATETIME,
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
