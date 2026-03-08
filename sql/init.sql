-- ============================================
-- 火锅店管理系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS hotpot_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE hotpot_db;

SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;

-- ----------------------------
-- 先删除所有表（注意外键依赖顺序）
-- ----------------------------
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS member_recharge;
DROP TABLE IF EXISTS inventory_record;
DROP TABLE IF EXISTS dish_ingredient;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS table_reservation;
DROP TABLE IF EXISTS dining_table;
DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS dish_category;
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS member_level;
DROP TABLE IF EXISTS sys_employee;

-- ----------------------------
-- 1. 员工表
-- ----------------------------
CREATE TABLE sys_employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'CASHIER' COMMENT '角色: ADMIN-店长, CASHIER-收银员, KITCHEN-后厨, INVENTORY-库管',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- ----------------------------
-- 2. 菜品分类表
-- ----------------------------
CREATE TABLE dish_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- ----------------------------
-- 3. 菜品表
-- ----------------------------
CREATE TABLE dish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜品ID',
    name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
    description VARCHAR(500) DEFAULT NULL COMMENT '菜品描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-在售, 2-售罄',
    spicy_level TINYINT NOT NULL DEFAULT 0 COMMENT '辣度: 0-不辣, 1-微辣, 2-中辣, 3-特辣',
    is_recommended TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐: 0-否, 1-是',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- ----------------------------
-- 4. 桌台表
-- ----------------------------
CREATE TABLE dining_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '桌台ID',
    table_number VARCHAR(20) NOT NULL COMMENT '桌号',
    capacity INT NOT NULL DEFAULT 4 COMMENT '可容纳人数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-空闲, 1-占用, 2-预订',
    area VARCHAR(50) DEFAULT NULL COMMENT '区域(大厅/包间/露台等)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_table_number (table_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台表';

-- ----------------------------
-- 5. 桌台预订表
-- ----------------------------
CREATE TABLE table_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预订ID',
    table_id BIGINT NOT NULL COMMENT '桌台ID',
    member_id BIGINT DEFAULT NULL COMMENT '会员ID(关联member.id)',
    customer_name VARCHAR(50) NOT NULL COMMENT '预订人姓名',
    customer_phone VARCHAR(20) NOT NULL COMMENT '预订人电话',
    reservation_time DATETIME NOT NULL COMMENT '预订时间',
    guest_count INT NOT NULL DEFAULT 2 COMMENT '就餐人数',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待到店, 1-已到店, 2-已取消, 3-未到(过期)',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_table_id (table_id),
    KEY idx_member_id (member_id),
    KEY idx_customer_phone (customer_phone),
    KEY idx_reservation_time (reservation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台预订表';

-- ----------------------------
-- 6. 订单表
-- ----------------------------
CREATE TABLE `order` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单编号',
    table_id BIGINT DEFAULT NULL COMMENT '桌台ID',
    member_id BIGINT DEFAULT NULL COMMENT '会员ID',
    order_status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待确认, 1-已确认(制作中), 2-已完成(待结算), 3-已结算, 4-已取消',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    actual_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    guest_count INT NOT NULL DEFAULT 1 COMMENT '就餐人数',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_by BIGINT DEFAULT NULL COMMENT '创建人(员工ID)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_table_id (table_id),
    KEY idx_member_id (member_id),
    KEY idx_order_status (order_status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 7. 订单项表
-- ----------------------------
CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    dish_name VARCHAR(100) NOT NULL COMMENT '菜品名称(冗余)',
    dish_price DECIMAL(10,2) NOT NULL COMMENT '菜品单价(下单时快照)',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注(口味要求等)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待制作, 1-制作中, 2-已完成, 3-已上菜',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_order_id (order_id),
    KEY idx_dish_id (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- ----------------------------
-- 8. 支付记录表
-- ----------------------------
CREATE TABLE payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    payment_no VARCHAR(50) NOT NULL COMMENT '支付单号',
    payment_method TINYINT NOT NULL DEFAULT 0 COMMENT '支付方式: 0-现金, 1-微信, 2-支付宝, 3-银行卡, 4-会员余额',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-待支付, 1-已支付, 2-已退款',
    cashier_id BIGINT DEFAULT NULL COMMENT '收银员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ----------------------------
-- 9. 食材表
-- ----------------------------
CREATE TABLE ingredient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '食材ID',
    name VARCHAR(100) NOT NULL COMMENT '食材名称',
    unit VARCHAR(20) NOT NULL COMMENT '单位(kg/斤/个/瓶等)',
    stock_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前库存量',
    warning_quantity DECIMAL(10,2) NOT NULL DEFAULT 10.00 COMMENT '预警库存量',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类(肉类/蔬菜/调料/酒水等)',
    price DECIMAL(10,2) DEFAULT NULL COMMENT '采购单价',
    supplier VARCHAR(100) DEFAULT NULL COMMENT '供应商',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_category (category),
    KEY idx_stock_warning (stock_quantity, warning_quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食材表';

-- ----------------------------
-- 10. 菜品食材关联表
-- ----------------------------
CREATE TABLE dish_ingredient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    ingredient_id BIGINT NOT NULL COMMENT '食材ID',
    consumption_amount DECIMAL(10,2) NOT NULL COMMENT '每份消耗量',
    UNIQUE KEY uk_dish_ingredient (dish_id, ingredient_id),
    KEY idx_ingredient_id (ingredient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品食材关联表';

-- ----------------------------
-- 11. 出入库记录表
-- ----------------------------
CREATE TABLE inventory_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    ingredient_id BIGINT NOT NULL COMMENT '食材ID',
    type TINYINT NOT NULL COMMENT '类型: 0-入库, 1-出库, 2-盘点, 3-订单消耗',
    quantity DECIMAL(10,2) NOT NULL COMMENT '数量',
    before_quantity DECIMAL(10,2) NOT NULL COMMENT '操作前库存',
    after_quantity DECIMAL(10,2) NOT NULL COMMENT '操作后库存',
    unit_price DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    total_price DECIMAL(10,2) DEFAULT NULL COMMENT '总价',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ingredient_id (ingredient_id),
    KEY idx_type (type),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出入库记录表';

-- ----------------------------
-- 12. 会员等级表
-- ----------------------------
CREATE TABLE member_level (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '等级ID',
    name VARCHAR(50) NOT NULL COMMENT '等级名称',
    min_points INT NOT NULL DEFAULT 0 COMMENT '所需最低积分',
    discount DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣(如0.95表示95折)',
    description VARCHAR(255) DEFAULT NULL COMMENT '等级说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- ----------------------------
-- 13. 会员表
-- ----------------------------
CREATE TABLE member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会员ID',
    member_no VARCHAR(50) NOT NULL COMMENT '会员编号',
    username VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password VARCHAR(200) NOT NULL COMMENT '登录密码(BCrypt加密)',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    gender TINYINT DEFAULT NULL COMMENT '性别: 0-女, 1-男',
    birthday DATE DEFAULT NULL COMMENT '生日',
    level_id BIGINT NOT NULL DEFAULT 1 COMMENT '等级ID',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
    points INT NOT NULL DEFAULT 0 COMMENT '积分',
    total_consumption DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_member_no (member_no),
    UNIQUE KEY uk_member_username (username),
    UNIQUE KEY uk_phone (phone),
    KEY idx_level_id (level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- ----------------------------
-- 14. 会员充值记录表
-- ----------------------------
CREATE TABLE member_recharge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '充值记录ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
    gift_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '赠送金额',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '充值后余额',
    payment_method TINYINT NOT NULL DEFAULT 0 COMMENT '支付方式: 0-现金, 1-微信, 2-支付宝, 3-银行卡',
    operator_id BIGINT DEFAULT NULL COMMENT '操作员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_member_id (member_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员充值记录表';

-- ----------------------------
-- 15. 操作日志表
-- ----------------------------
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    module VARCHAR(50) NOT NULL COMMENT '模块',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    detail VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_operator_id (operator_id),
    KEY idx_module (module),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
