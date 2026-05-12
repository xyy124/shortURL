CREATE DATABASE IF NOT EXISTS short_url DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE short_url;

CREATE TABLE IF NOT EXISTS url_map (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  short_code  VARCHAR(10)  NOT NULL COMMENT '短链码',
  long_url    VARCHAR(2048) NOT NULL COMMENT '原始长链接',
  user_id     BIGINT       DEFAULT NULL COMMENT '所属用户ID',
  title       VARCHAR(255) DEFAULT NULL COMMENT '链接标题',
  is_custom   TINYINT(1)   DEFAULT 0 COMMENT '是否自定义',
  is_active   TINYINT(1)   DEFAULT 1 COMMENT '是否启用',
  expire_time DATETIME     DEFAULT NULL COMMENT '过期时间',
  views       INT          DEFAULT 0 COMMENT '总访问次数',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_short_code (short_code),
  KEY idx_user_id (user_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  username    VARCHAR(50)  NOT NULL COMMENT '用户名',
  password    VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
  nickname    VARCHAR(50)  DEFAULT NULL COMMENT '显示昵称',
  email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  role        VARCHAR(20)  DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  is_active   TINYINT(1)   DEFAULT 1 COMMENT '是否激活',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS visit_log (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  short_code  VARCHAR(10)  NOT NULL COMMENT '短链码',
  visit_time  DATETIME     NOT NULL COMMENT '访问时间',
  ip          VARCHAR(50)  DEFAULT NULL COMMENT '访问者IP',
  user_agent  VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
  referer     VARCHAR(500) DEFAULT NULL COMMENT '来源页面',
  country     VARCHAR(100) DEFAULT NULL COMMENT '国家',
  city        VARCHAR(100) DEFAULT NULL COMMENT '城市',
  device_type VARCHAR(10)  DEFAULT NULL COMMENT '设备类型: PC/MOBILE/TABLET',
  browser     VARCHAR(50)  DEFAULT NULL COMMENT '浏览器',
  os          VARCHAR(50)  DEFAULT NULL COMMENT '操作系统',
  PRIMARY KEY (id),
  KEY idx_short_code (short_code),
  KEY idx_visit_time (visit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS daily_stats (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  short_code  VARCHAR(10)  NOT NULL COMMENT '短链码',
  stats_date  DATE         NOT NULL COMMENT '统计日期',
  pv          INT          DEFAULT 0 COMMENT '页面访问量',
  uv          INT          DEFAULT 0 COMMENT '独立访客',
  ip_count    INT          DEFAULT 0 COMMENT '独立IP数',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code_date (short_code, stats_date),
  KEY idx_stats_date (stats_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
