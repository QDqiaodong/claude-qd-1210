SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- ---------------------------------------------------------------------
-- 银饰锻造坊 · 4 模块业务库
-- 库名 silver_smith；application.yml 里 ddl-auto: none，表结构全部由本文件在 MySQL 镜像构建期导入。
-- 注意：mysql 容器是 UTC，时间戳一律写 NOW()，日期用 DATE_SUB(CURDATE(), INTERVAL N DAY)。
-- 两处 schema（backend/src/main/resources/schema.sql 与 mysql/schema.sql）内容必须完全一致。

CREATE DATABASE IF NOT EXISTS silver_smith
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE silver_smith;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS forge_order;
DROP TABLE IF EXISTS custom_order;
DROP TABLE IF EXISTS mold;
DROP TABLE IF EXISTS silver_stock;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------- 1. 银料 silver_stock ----------------
CREATE TABLE silver_stock (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    alloy        VARCHAR(16) COMMENT '合金：999 / 925 / 苗银',
    weight       DECIMAL(10,2) COMMENT '重量（克），必须大于 0',
    source       VARCHAR(16) COMMENT '来源：采购 / 回收',
    status       VARCHAR(16) COMMENT '在用 / 熔炼中 / 已成材 / 报废',
    purity_check TINYINT(1) COMMENT '纯度已检：1=已检 0=未检（未检不可被锻造引用）',
    created_at   DATETIME COMMENT '入库时间',
    updated_at   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 工具模具 mold（须在 forge_order 之前建立，供外键引用） ----------------
CREATE TABLE mold (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(64) COMMENT '模具名称',
    type         VARCHAR(16) COMMENT '类型：錾子 / 砧 / 花丝模 / 抛光轮',
    wear         INT COMMENT '磨损度（%），0-100',
    last_maintain DATE COMMENT '上次维护日期',
    status       VARCHAR(16) COMMENT '良好 / 待修 / 报废',
    created_at   DATETIME COMMENT '创建时间',
    updated_at   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 2. 锻造工单 forge_order（依赖银料 + 模具） ----------------
CREATE TABLE forge_order (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    product      VARCHAR(16) COMMENT '产品：手镯 / 戒指 / 项链 / 耳环 / 吊坠',
    silver_id    BIGINT COMMENT '银料 id（须纯度已检且非报废/熔炼中）',
    mold_id      BIGINT COMMENT '模具 id（须非报废/待修）',
    plan_weight  DECIMAL(10,2) COMMENT '计划重量（克）',
    actual_weight DECIMAL(10,2) COMMENT '实际重量（克），完成前必填',
    status       VARCHAR(16) COMMENT '开料 / 锻打 / 抛光 / 质检 / 完成 / 返工',
    craft        VARCHAR(16) COMMENT '工艺：錾刻 / 花丝 / 烧蓝',
    created_at   DATETIME COMMENT '创建时间',
    updated_at   DATETIME COMMENT '更新时间',
    CONSTRAINT fk_fo_silver FOREIGN KEY (silver_id) REFERENCES silver_stock (id),
    CONSTRAINT fk_fo_mold FOREIGN KEY (mold_id) REFERENCES mold (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 3. 客户定制 custom_order ----------------
CREATE TABLE custom_order (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer     VARCHAR(64) COMMENT '客户姓名',
    design       VARCHAR(128) COMMENT '定制图样',
    deposit      DECIMAL(10,2) COMMENT '定金（≤总价，退订不退）',
    total        DECIMAL(10,2) COMMENT '总价',
    status       VARCHAR(16) COMMENT '待确认 / 制作中 / 待取 / 已交付 / 退订',
    due_date     DATE COMMENT '约定交付日期',
    created_at   DATETIME COMMENT '创建时间',
    updated_at   DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------------------
-- 种子数据（含异常态，用于真实触发业务拒绝分支）
--   异常态 1：S3 状态为「报废」→ 不可被引用、不可恢复
--   异常态 2：S4 纯度未检（purity_check=0）→ 不可被锻造工单引用
--   异常态 3：S2 状态「熔炼中」→ 不可被锻造工单引用
--   异常态 4：M3 磨损度 85>80 且曾为「良好」→ 按规则自动置「待修」
--   异常态 5：M5 状态「报废」→ 不可被锻造工单引用
--   异常态 6：F4 引用熔炼中银料 + 待修模具且为「返工」；F6 引用未检纯度银料 → 复核新建时会被拦截
--   异常态 7：C5 已「退订」（定金不退，记已没收）；C4 已「已交付」终态
-- ---------------------------------------------------------------------

INSERT IGNORE INTO silver_stock (id, alloy, weight, source, status, purity_check, created_at, updated_at) VALUES
(1, '999',  50.00, '采购', '在用',   1, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY)),
(2, '925',  30.00, '采购', '熔炼中', 1, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 20 DAY)),
(3, '苗银', 20.00, '回收', '报废',   1, DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 40 DAY)),
(4, '999',  25.00, '采购', '在用',   0, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY)),
(5, '925',  40.00, '回收', '已成材', 1, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
(6, '999',  60.00, '采购', '在用',   1, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  DATE_SUB(CURDATE(), INTERVAL 5 DAY));

INSERT IGNORE INTO mold (id, name, type, wear, last_maintain, status, created_at, updated_at) VALUES
(1, '圆头錾子',   '錾子',   10,  DATE_SUB(CURDATE(), INTERVAL 30 DAY), '良好', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY)),
(2, '平砧',       '砧',     45,  DATE_SUB(CURDATE(), INTERVAL 60 DAY), '良好', DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_SUB(CURDATE(), INTERVAL 60 DAY)),
(3, '牡丹花丝模', '花丝模', 85,  DATE_SUB(CURDATE(), INTERVAL 90 DAY), '待修', DATE_SUB(CURDATE(), INTERVAL 90 DAY), DATE_SUB(CURDATE(), INTERVAL 90 DAY)),
(4, '羊毛抛光轮', '抛光轮', 20,  DATE_SUB(CURDATE(), INTERVAL 15 DAY), '良好', DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY)),
(5, '旧錾子',     '錾子',   100, DATE_SUB(CURDATE(), INTERVAL 120 DAY), '报废', DATE_SUB(CURDATE(), INTERVAL 120 DAY), DATE_SUB(CURDATE(), INTERVAL 120 DAY));

INSERT IGNORE INTO forge_order (id, product, silver_id, mold_id, plan_weight, actual_weight, status, craft, created_at, updated_at) VALUES
(1, '手镯', 1, 1, 45.00, NULL,   '开料', '錾刻', DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
(2, '戒指', 5, 2, 10.00, 9.50,   '质检', '花丝', DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
(3, '项链', 1, 1, 30.00, 28.00,  '完成', '烧蓝', DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY)),
(4, '耳环', 2, 3, 5.00,  NULL,   '返工', '錾刻', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(5, '吊坠', 6, 4, 15.00, NULL,   '锻打', '花丝', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(6, '手镯', 4, 1, 20.00, NULL,   '开料', '錾刻', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY));

INSERT IGNORE INTO custom_order (id, customer, design, deposit, total, status, due_date, created_at, updated_at) VALUES
(1, '王女士', '龙凤镯图样', 500.00, 3000.00, '待确认', DATE_SUB(CURDATE(), INTERVAL -7 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(2, '李先生', '莲花戒指',   300.00, 1200.00, '制作中', DATE_SUB(CURDATE(), INTERVAL -5 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
(3, '张同学', '星月项链',   200.00, 800.00,  '待取',   DATE_SUB(CURDATE(), INTERVAL -3 DAY), DATE_SUB(CURDATE(), INTERVAL 8 DAY), DATE_SUB(CURDATE(), INTERVAL 8 DAY)),
(4, '赵阿姨', '福字吊坠',   400.00, 1500.00, '已交付', DATE_SUB(CURDATE(), INTERVAL -1 DAY), DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 20 DAY)),
(5, '陈先生', '云纹耳环',   100.00, 600.00,  '退订',   DATE_SUB(CURDATE(), INTERVAL 2 DAY),  DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_SUB(CURDATE(), INTERVAL 4 DAY));
