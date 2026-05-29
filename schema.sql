-- ============================================================================
-- 校园二手交易平台 — 数据库建表脚本
-- 目标数据库: campus_trading (MySQL 8.0+, utf8mb4)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS campus_trading
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_trading;

-- ============================================================================
-- 管理员表
-- ============================================================================
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL COMMENT '用户名',
    name VARCHAR(32) NOT NULL COMMENT '姓名',
    password VARCHAR(64) NOT NULL DEFAULT '123456' COMMENT '密码',
    phone VARCHAR(11) DEFAULT NULL COMMENT '手机号',
    sex VARCHAR(2) DEFAULT NULL COMMENT '性别',
    id_number VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    create_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    create_user BIGINT DEFAULT NULL,
    update_user BIGINT DEFAULT NULL,
    UNIQUE KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ============================================================================
-- 分类表
-- ============================================================================
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type TINYINT DEFAULT NULL COMMENT '分类类型: 1商品分类 2捆绑包分类',
    name VARCHAR(32) NOT NULL COMMENT '分类名称',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    create_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    create_user BIGINT DEFAULT NULL,
    update_user BIGINT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- ============================================================================
-- 商品表（核心表）
-- ============================================================================
CREATE TABLE IF NOT EXISTS item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单价',
    images VARCHAR(500) DEFAULT NULL COMMENT '图片（多图，逗号分隔）',
    item_description VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    sale_status TINYINT NOT NULL DEFAULT 0 COMMENT '销售状态: 0下架 1上架',
    priority INT DEFAULT 0 COMMENT '优先级（越大越靠前）',
    condition_level TINYINT DEFAULT 1 COMMENT '新旧程度: 1全新 2九成新 3八成新 4七成新',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    seller_id BIGINT DEFAULT NULL COMMENT '卖家ID',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    create_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    create_user BIGINT DEFAULT NULL,
    update_user BIGINT DEFAULT NULL,
    INDEX idx_category_id (category_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_sale_status (sale_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================================================
-- 捆绑包表（组合出售）
-- ============================================================================
CREATE TABLE IF NOT EXISTS bundle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(64) NOT NULL COMMENT '捆绑包名称',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    create_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    create_user BIGINT DEFAULT NULL,
    update_user BIGINT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='捆绑包表';

-- ============================================================================
-- 捆绑包-商品关联表
-- ============================================================================
CREATE TABLE IF NOT EXISTS bundle_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bundle_id BIGINT NOT NULL COMMENT '捆绑包ID',
    item_id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(64) DEFAULT NULL COMMENT '商品名称',
    price DECIMAL(10,2) DEFAULT NULL COMMENT '商品单价',
    copies INT NOT NULL DEFAULT 1 COMMENT '份数',
    INDEX idx_bundle_id (bundle_id),
    INDEX idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='捆绑包商品关联表';

-- ============================================================================
-- 交易订单表
-- 状态: 1待付款 2待发货 3已发货 4已完成 5已取消 6退款中 7已退款
-- ============================================================================
CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_no VARCHAR(50) NOT NULL COMMENT '交易单号',
    trade_status TINYINT NOT NULL DEFAULT 1 COMMENT '交易状态: 1待付款 2待发货 3已发货 4已完成 5已取消 6退款中 7已退款',
    buyer_id BIGINT NOT NULL COMMENT '买家ID',
    seller_id BIGINT DEFAULT NULL COMMENT '卖家ID',
    item_id BIGINT DEFAULT NULL COMMENT '商品ID',
    delivery_address_id BIGINT DEFAULT NULL COMMENT '收货地址ID',
    quantity INT DEFAULT 1 COMMENT '数量',
    trade_time DATETIME DEFAULT NULL COMMENT '交易时间',
    payment_time DATETIME DEFAULT NULL COMMENT '支付时间',
    payment_method TINYINT DEFAULT NULL COMMENT '支付方式: 1微信 2支付宝',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
    shipping_fee DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
    trade_remark VARCHAR(500) DEFAULT NULL COMMENT '交易备注',
    tracking_number VARCHAR(100) DEFAULT NULL COMMENT '物流单号',
    confirm_time DATETIME DEFAULT NULL COMMENT '确认收货时间',
    phone VARCHAR(11) DEFAULT NULL COMMENT '联系电话',
    address VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    user_name VARCHAR(32) DEFAULT NULL COMMENT '用户姓名',
    consignee VARCHAR(32) DEFAULT NULL COMMENT '收货人',
    cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    rejection_reason VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    INDEX idx_trade_no (trade_no),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_trade_status (trade_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- ============================================================================
-- 订单详情表
-- ============================================================================
CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) DEFAULT NULL COMMENT '商品名称',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    item_id BIGINT DEFAULT NULL COMMENT '商品ID',
    bundle_id BIGINT DEFAULT NULL COMMENT '捆绑包ID',
    number INT DEFAULT NULL COMMENT '数量',
    amount DECIMAL(10,2) DEFAULT NULL COMMENT '金额',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- ============================================================================
-- 购物车表
-- ============================================================================
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) DEFAULT NULL COMMENT '商品名称',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    item_id BIGINT DEFAULT NULL COMMENT '商品ID',
    bundle_id BIGINT DEFAULT NULL COMMENT '捆绑包ID',
    number INT DEFAULT NULL COMMENT '数量',
    amount DECIMAL(10,2) DEFAULT NULL COMMENT '金额',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ============================================================================
-- 用户表
-- ============================================================================
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    openid VARCHAR(45) DEFAULT NULL COMMENT '微信openid',
    name VARCHAR(32) DEFAULT NULL COMMENT '昵称',
    phone VARCHAR(11) DEFAULT NULL COMMENT '手机号',
    sex VARCHAR(2) DEFAULT NULL COMMENT '性别',
    id_number VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像',
    create_time DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================================
-- 地址簿表
-- ============================================================================
CREATE TABLE IF NOT EXISTS address_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    consignee VARCHAR(50) DEFAULT NULL COMMENT '收货人',
    phone VARCHAR(11) DEFAULT NULL COMMENT '联系电话',
    sex VARCHAR(2) DEFAULT NULL COMMENT '性别',
    province_code VARCHAR(12) DEFAULT NULL COMMENT '省级编码',
    province_name VARCHAR(32) DEFAULT NULL COMMENT '省',
    city_code VARCHAR(12) DEFAULT NULL COMMENT '市级编码',
    city_name VARCHAR(32) DEFAULT NULL COMMENT '市',
    district_code VARCHAR(12) DEFAULT NULL COMMENT '区级编码',
    district_name VARCHAR(32) DEFAULT NULL COMMENT '区',
    detail VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
    label VARCHAR(100) DEFAULT NULL COMMENT '标签',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认: 1是 0否',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址簿表';

-- ============================================================================
-- 收藏表
-- ============================================================================
CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    item_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME NOT NULL COMMENT '收藏时间',
    UNIQUE KEY uk_user_item (user_id, item_id),
    INDEX idx_user_id (user_id),
    INDEX idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ============================================================================
-- 评价表
--     type: 1 买家评卖家, 2 卖家评买家
-- ============================================================================
CREATE TABLE IF NOT EXISTS evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    reviewer_id BIGINT NOT NULL COMMENT '评价人ID',
    reviewee_id BIGINT NOT NULL COMMENT '被评价人ID',
    rating TINYINT NOT NULL DEFAULT 5 COMMENT '评分: 1-5',
    content VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    create_time DATETIME NOT NULL COMMENT '评价时间',
    type TINYINT DEFAULT 1 COMMENT '评价类型: 1买家评卖家 2卖家评买家',
    INDEX idx_reviewee_id (reviewee_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ============================================================================
-- 聊天消息表
-- ============================================================================
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    item_id BIGINT DEFAULT NULL COMMENT '关联商品ID',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type TINYINT DEFAULT 1 COMMENT '消息类型: 1文字 2图片',
    send_time DATETIME NOT NULL COMMENT '发送时间',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    INDEX idx_sender_receiver (sender_id, receiver_id),
    INDEX idx_item_id (item_id),
    INDEX idx_send_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- ============================================================================
-- AI 聊天历史表
-- ============================================================================
CREATE TABLE IF NOT EXISTS chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    message_type VARCHAR(16) NOT NULL COMMENT '消息类型: USER/ASSISTANT/SYSTEM',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天历史表';

-- ============================================================================
-- 建表完成
-- ============================================================================
