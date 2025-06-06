
DROP DATABASE IF EXISTS foodhub_management;

CREATE DATABASE foodhub_management
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE foodhub_management;

-- Bảng role
CREATE TABLE role (
    name VARCHAR(50) PRIMARY KEY,
    description VARCHAR(100)
);

-- Bảng permission
CREATE TABLE permission (
    name VARCHAR(50) PRIMARY KEY,
    description VARCHAR(100)
);

-- Bảng role_permission
CREATE TABLE role_permission (
    role_name VARCHAR(50) NOT NULL,
    permission_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_name, permission_name),
    FOREIGN KEY (role_name) REFERENCES role(name) ON DELETE CASCADE,
    FOREIGN KEY (permission_name) REFERENCES permission(name) ON DELETE CASCADE,
    INDEX idx_role_permission_role_name (role_name)
);

-- Bảng user
CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    address VARCHAR(255),
    phone VARCHAR(20),
    registration_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_auth_user BOOLEAN NOT NULL DEFAULT FALSE,
    oauth_provider VARCHAR(50),
    last_login DATETIME,
    FOREIGN KEY (role_name) REFERENCES role(name) ON DELETE RESTRICT,
    INDEX idx_user_role_name (role_name),
    INDEX idx_user_status (status),
    INDEX idx_user_registration_date (registration_date),
    INDEX idx_user_last_login (last_login)
);

-- Bảng category
CREATE TABLE category (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    INDEX idx_category_name (name)
);

-- Bảng menu_item
CREATE TABLE menu_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    status ENUM('AVAILABLE', 'UNAVAILABLE') DEFAULT 'AVAILABLE',
    INDEX idx_menu_item_name (name),
    INDEX idx_menu_item_status (status)
);

-- Bảng menu_item_category
CREATE TABLE menu_item_category (
    menu_item_id INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (menu_item_id, category_id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_item(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    INDEX idx_menu_item_category_menu_item_id (menu_item_id),
    INDEX idx_menu_item_category_category_id (category_id)
);

-- Bảng restaurant_table
CREATE TABLE restaurant_table (
    id INT PRIMARY KEY AUTO_INCREMENT,
    table_number VARCHAR(50) NOT NULL UNIQUE,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    status ENUM('AVAILABLE', 'OCCUPIED') NOT NULL DEFAULT 'AVAILABLE',
    INDEX idx_table_table_number (table_number),
    INDEX idx_table_status (status)
);

-- Bảng restaurant_order
CREATE TABLE restaurant_order (
    id INT PRIMARY KEY AUTO_INCREMENT,
    table_id INT NOT NULL,
    user_id INT NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    note TEXT,
    order_type ENUM('DINE_IN', 'TAKEAWAY', 'DELIVERY') NOT NULL DEFAULT 'DINE_IN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES restaurant_table(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE RESTRICT,
    INDEX idx_order_table_id (table_id),
    INDEX idx_order_user_id (user_id),
    INDEX idx_order_status (status),
    INDEX idx_order_created_at (created_at)
);

-- Bảng order_item
CREATE TABLE order_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'READY', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (order_id) REFERENCES restaurant_order(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_item(id) ON DELETE RESTRICT,
    INDEX idx_order_item_order_id (order_id),
    INDEX idx_order_item_menu_item_id (menu_item_id),
    INDEX idx_order_item_status (status)
);

-- Bảng chat_message
CREATE TABLE chat_message (
    id INT PRIMARY KEY AUTO_INCREMENT,
    table_id INT NOT NULL,
    sender VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES restaurant_table(id) ON DELETE CASCADE,
    INDEX idx_chat_message_table_id (table_id),
    INDEX idx_chat_message_timestamp (timestamp)
);

-- Bảng payment
CREATE TABLE payment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'VNPAY') NOT NULL,
    status ENUM('PENDING', 'UNPAID', 'PAID', 'CANCELLED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES restaurant_order(id) ON DELETE CASCADE,
    INDEX idx_payment_order_id (order_id),
    INDEX idx_payment_status (status),
    INDEX idx_payment_created_at (created_at)
);

-- Bảng invalidate_token
CREATE TABLE invalidate_token (
    token VARCHAR(512) PRIMARY KEY,
    user_id INT NOT NULL,
    expiry_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_invalidate_token_expiry (expiry_time)
);

-- Bảng work_schedule
CREATE TABLE work_schedule (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    work_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    shift_type ENUM('MORNING', 'AFTERNOON', 'EVENING', 'FULL_DAY') DEFAULT 'FULL_DAY',
    note TEXT,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_schedule_user_id (user_id),
    INDEX idx_schedule_work_date (work_date),
    INDEX idx_schedule_shift_type (shift_type)
);

-- Chèn dữ liệu vào bảng role
INSERT INTO role (name, description) VALUES 
('ADMIN', 'System administrator'),
('CASHIER', 'Cashier staff'),
('WAITER', 'Service staff'),
('CHEF', 'Chef'),
('CUSTOMER', 'Customer');

-- Chèn dữ liệu vào bảng permission
INSERT INTO permission (name, description) VALUES
('USER_MANAGEMENT', 'Manage users and their roles'),
('MENU_MANAGEMENT', 'Manage menu items and categories'),
('ORDER_MANAGEMENT', 'Manage orders and order items'),
('BOOKING_MANAGEMENT', 'Manage table bookings'),
('PAYMENT_MANAGEMENT', 'Process payments and refunds'),
('BRANCH_MANAGEMENT', 'Manage branch operations'),
('TABLE_MANAGEMENT', 'Manage restaurant tables'),
('CHAT_MANAGEMENT', 'Manage chat messages'),
('REPORT_VIEW', 'View reports and analytics'),
('SYSTEM_CONFIG', 'Configure system settings');

-- Chèn dữ liệu vào bảng role_permission
INSERT INTO role_permission (role_name, permission_name) VALUES
-- ADMIN permissions
('ADMIN', 'USER_MANAGEMENT'),
('ADMIN', 'MENU_MANAGEMENT'),
('ADMIN', 'ORDER_MANAGEMENT'),
('ADMIN', 'BOOKING_MANAGEMENT'),
('ADMIN', 'PAYMENT_MANAGEMENT'),
('ADMIN', 'BRANCH_MANAGEMENT'),
('ADMIN', 'TABLE_MANAGEMENT'),
('ADMIN', 'CHAT_MANAGEMENT'),
('ADMIN', 'REPORT_VIEW'),
('ADMIN', 'SYSTEM_CONFIG'),
-- CASHIER permissions
('CASHIER', 'ORDER_MANAGEMENT'),
('CASHIER', 'PAYMENT_MANAGEMENT'),
('CASHIER', 'CHAT_MANAGEMENT'),
-- WAITER permissions
('WAITER', 'ORDER_MANAGEMENT'),
('WAITER', 'BOOKING_MANAGEMENT'),
('WAITER', 'TABLE_MANAGEMENT'),
('WAITER', 'CHAT_MANAGEMENT'),
-- CHEF permissions
('CHEF', 'ORDER_MANAGEMENT'),
('CHEF', 'MENU_MANAGEMENT');

-- Chèn dữ liệu vào bảng user
INSERT INTO user (username, email, password, role_name, status, address, phone, registration_date, is_auth_user, oauth_provider, last_login) VALUES
-- CUSTOMER (Khách hàng)
('nguyenvana', 'nguyenvana@gmail.com', 'password123', 'CUSTOMER', 'ACTIVE', '123 Đường Láng, Hà Nội', '0901234567', CURRENT_TIMESTAMP, FALSE, NULL, NULL),
('tranba', 'tranba@gmail.com', 'password456', 'CUSTOMER', 'ACTIVE', '45 Nguyễn Huệ, TP.HCM', '0912345678', CURRENT_TIMESTAMP, FALSE, NULL, NULL),
('lethiminh', 'lethiminh@gmail.com', 'password789', 'CUSTOMER', 'INACTIVE', '78 Lê Lợi, Đà Nẵng', '0923456789', CURRENT_TIMESTAMP, FALSE, NULL, NULL),
('phamthic', 'phamthic@gmail.com', 'password101', 'CUSTOMER', 'ACTIVE', '12 Trần Phú, Nha Trang', '0934567890', CURRENT_TIMESTAMP, TRUE, 'google', NULL),
-- WAITER (Nhân viên phục vụ - Staff)
('hoangvand', 'hoangvand@foodhub.com', 'staff123', 'WAITER', 'ACTIVE', '56 Phạm Văn Đồng, Hà Nội', '0945678901', CURRENT_TIMESTAMP, FALSE, NULL, NULL),
('nguyenthihoa', 'nguyenthihoa@foodhub.com', 'staff456', 'WAITER', 'ACTIVE', '89 Nguyễn Trãi, TP.HCM', '0956789012', CURRENT_TIMESTAMP, FALSE, NULL, NULL),
('tranthanh', 'tranthanh@foodhub.com', 'staff789', 'WAITER', 'ACTIVE', '23 Lê Đại Hành, Huế', '0967890123', CURRENT_TIMESTAMP, FALSE, NULL, NULL),
('levanbinh', 'levanbinh@foodhub.com', 'staff101', 'WAITER', 'INACTIVE', '67 Trần Hưng Đạo, Đà Lạt', '0978901234', CURRENT_TIMESTAMP, FALSE, NULL, NULL),
-- ADMIN (Quản trị viên)
('admin1', 'admin1@foodhub.com', 'admin123', 'ADMIN', 'ACTIVE', '101 Tôn Đức Thắng, Hà Nội', '0989012345', CURRENT_TIMESTAMP, FALSE, NULL, NULL),
('admin2', 'admin2@foodhub.com', 'admin456', 'ADMIN', 'ACTIVE', '202 Nguyễn Văn Cừ, TP.HCM', '0990123456', CURRENT_TIMESTAMP, TRUE, 'google', NULL);

-- Chèn dữ liệu vào bảng category
INSERT INTO category (id, name, description) VALUES
(1, 'Lẩu nước', 'Các loại nước lẩu như lẩu Thái, lẩu kim chi...'),
(2, 'Nguyên liệu nhúng lẩu', 'Thịt, hải sản, rau, mì... để nhúng vào lẩu'),
(3, 'Món nướng thịt', 'Ba chỉ bò, thịt gà, thịt heo nướng'),
(4, 'Món nướng hải sản', 'Tôm, mực, bạch tuộc nướng'),
(5, 'Món nướng rau củ', 'Nấm, bắp, đậu bắp, rau củ nướng'),
(6, 'Món khai vị', 'Salad, khoai tây chiên, món ăn nhẹ đầu bữa'),
(7, 'Món ăn kèm', 'Bún, mì, cơm thêm'),
(8, 'Nước chấm', 'Các loại nước chấm và sốt đặc biệt'),
(9, 'Đồ uống không cồn', 'Trà, nước ép, nước suối'),
(10, 'Đồ uống có cồn', 'Bia, rượu, cocktail');

-- Chèn dữ liệu vào bảng menu_item
INSERT INTO menu_item (name, description, image_url, price, status) VALUES
('Lẩu Thái Hải Sản', 'Lẩu cay chua kiểu Thái với tôm, mực, ngao', NULL, 299000, 'AVAILABLE'),
('Lẩu Kim Chi Bò Mỹ', 'Lẩu Hàn Quốc với kim chi và bò Mỹ lát mỏng', NULL, 289000, 'AVAILABLE'),
('Lẩu Nấm Chay', 'Lẩu thanh đạm với các loại nấm và rau củ', NULL, 199000, 'AVAILABLE'),
('Thịt Bò Mỹ Slices', 'Thịt bò Mỹ thái lát mỏng, tươi ngon để nhúng lẩu', NULL, 99000, 'AVAILABLE'),
('Tôm Tươi', 'Tôm tươi dùng để nhúng lẩu', NULL, 79000, 'AVAILABLE'),
('Nấm Kim Châm', 'Nấm kim châm tươi, nguyên liệu nhúng lẩu', NULL, 39000, 'AVAILABLE'),
('Ba Chỉ Bò Mỹ Nướng', 'Ba chỉ bò Mỹ tẩm sốt đặc biệt, nướng trên than hồng', NULL, 129000, 'AVAILABLE'),
('Gà Xiên Nướng Mật Ong', 'Thịt gà xiên nướng thơm vị mật ong', NULL, 89000, 'AVAILABLE'),
('Sườn Heo Nướng BBQ', 'Sườn heo nướng sốt BBQ đậm đà', NULL, 139000, 'AVAILABLE'),
('Tôm Sú Nướng Bơ Tỏi', 'Tôm sú tươi ướp bơ tỏi, khách tự nướng trên bếp ga mini', NULL, 98000, 'AVAILABLE'),
('Mực Ống Nướng Bơ Cay', 'Mực ống tươi ướp sốt bơ cay nhẹ, thích hợp cho nướng tại bàn', NULL, 92000, 'AVAILABLE'),
('Bạch Tuộc Nướng Bơ Sả', 'Bạch tuộc nhỏ tẩm ướp sả và bơ, nướng thơm lừng', NULL, 99000, 'AVAILABLE'),
('Cá Trứng Nướng Bơ', 'Cá trứng tươi nướng vàng đều trên chảo bơ nóng', NULL, 75000, 'AVAILABLE'),
('Hàu Sữa Nướng Bơ Tỏi', 'Hàu sữa béo ngậy nướng bơ tỏi tại bàn', NULL, 89000, 'AVAILABLE'),
('Nấm Đùi Gà Nướng Bơ', 'Nấm đùi gà thái lát, nướng bơ thơm ngon', NULL, 48000, 'AVAILABLE'),
('Khoai Lang Nướng Bơ', 'Khoai lang thái mỏng, nướng bơ béo bùi', NULL, 39000, 'AVAILABLE'),
('Măng Tây Nướng Bơ Tỏi', 'Măng tây giòn, nướng với bơ và tỏi', NULL, 52000, 'AVAILABLE'),
('Ớt Chuông Nướng', 'Ớt chuông các màu nướng thơm, ăn kèm các món nướng', NULL, 35000, 'AVAILABLE'),
('Cà Tím Nướng Mỡ Hành', 'Cà tím mềm nướng, rưới mỡ hành hấp dẫn', NULL, 42000, 'AVAILABLE'),
('Bánh Xèo Mini', 'Bánh xèo nhỏ giòn rụm, nhân thịt tôm, ăn khai vị', NULL, 55000, 'AVAILABLE'),
('Khoai Tây Chiên', 'Khoai tây chiên giòn, ăn vặt hoặc khai vị', NULL, 45000, 'AVAILABLE'),
('Nem Chua Rán', 'Nem chua rán nóng giòn, ăn kèm tương ớt', NULL, 49000, 'AVAILABLE'),
('Súp Ngô Gà', 'Súp ngô non và thịt gà nhẹ nhàng', NULL, 42000, 'AVAILABLE'),
('Gỏi Cuốn Tôm Thịt', 'Gỏi cuốn thanh mát, chấm mắm nêm', NULL, 58000, 'AVAILABLE'),
('Bún Tươi', 'Bún tươi mềm, dùng kèm món lẩu hoặc nướng', NULL, 15000, 'AVAILABLE'),
('Mì Gói Hàn Quốc', 'Mì Hàn cay, dùng kèm lẩu Thái hoặc Kim Chi', NULL, 18000, 'AVAILABLE'),
('Bánh Tráng Mè', 'Bánh tráng nướng giòn, ăn kèm đồ nướng', NULL, 12000, 'AVAILABLE'),
('Rau Sống Ăn Kèm', 'Rau sống các loại: xà lách, tía tô, dưa leo,...', NULL, 20000, 'AVAILABLE'),
('Kim Chi Cải Thảo', 'Kim chi lên men chuẩn vị Hàn, ăn cùng đồ nướng', NULL, 22000, 'AVAILABLE'),
('Sốt Mè Rang Nhật', 'Sốt mè rang béo thơm, ăn kèm lẩu hoặc nướng', NULL, 8000, 'AVAILABLE'),
('Sốt Hải Sản Thái', 'Sốt xanh chua cay kiểu Thái, hợp với hải sản', NULL, 8000, 'AVAILABLE'),
('Sốt Chanh Ớt', 'Sốt chanh tươi, ớt, tỏi dùng kèm hải sản nướng', NULL, 5000, 'AVAILABLE'),
('Sốt Tare Nhật Bản', 'Sốt ngọt đậm đà, dùng với thịt nướng', NULL, 9000, 'AVAILABLE'),
('Muối Tiêu Chanh', 'Muối tiêu chanh truyền thống, dễ ăn', NULL, 4000, 'AVAILABLE'),
('Trà Đào Cam Sả', 'Trà đào kết hợp cam và sả, thơm mát', NULL, 35000, 'AVAILABLE'),
('Nước Suối', 'Nước suối tinh khiết đóng chai', NULL, 10000, 'AVAILABLE'),
('Pepsi Lon', 'Pepsi lạnh, dùng kèm món ăn', NULL, 15000, 'AVAILABLE'),
('Trà Ô Long Không Đường', 'Trà ô long đóng chai, vị thanh mát', NULL, 18000, 'AVAILABLE'),
('Sữa Bắp Tươi', 'Sữa bắp nấu từ bắp tươi nguyên chất', NULL, 25000, 'AVAILABLE'),
('Bia Tiger Lon', 'Bia Tiger mát lạnh, phù hợp đồ nướng', NULL, 18000, 'AVAILABLE'),
('Bia Sapporo Chai', 'Bia Sapporo Nhật chai lớn', NULL, 28000, 'AVAILABLE'),
('Rượu Soju Hàn Quốc', 'Soju nhiều hương vị: nho, đào, táo', NULL, 50000, 'AVAILABLE'),
('Cocktail Trái Cây', 'Cocktail nhẹ vị trái cây, dễ uống', NULL, 60000, 'AVAILABLE'),
('Strongbow Táo', 'Cider có cồn nhẹ, hương táo', NULL, 35000, 'AVAILABLE');

-- Chèn dữ liệu vào bảng menu_item_category
INSERT INTO menu_item_category (menu_item_id, category_id) VALUES
-- Lẩu nước (category_id = 1)
(1, 1), (2, 1), (3, 1),
-- Nguyên liệu nhúng lẩu (category_id = 2)
(4, 2), (5, 2), (6, 2),
-- Món nướng thịt (category_id = 3)
(7, 3), (8, 3), (9, 3),
-- Món nướng hải sản (category_id = 4)
(10, 4), (11, 4), (12, 4), (13, 4), (14, 4),
-- Món nướng rau củ (category_id = 5)
(15, 5), (16, 5), (17, 5), (18, 5), (19, 5),
-- Món khai vị (category_id = 6)
(20, 6), (21, 6), (22, 6), (23, 6), (24, 6),
-- Món ăn kèm (category_id = 7)
(25, 7), (26, 7), (27, 7), (28, 7), (29, 7),
-- Nước chấm (category_id = 8)
(30, 8), (31, 8), (32, 8), (33, 8), (34, 8),
-- Đồ uống không cồn (category_id = 9)
(35, 9), (36, 9), (37, 9), (38, 9), (39, 9),
-- Đồ uống có cồn (category_id = 10)
(40, 10), (41, 10), (42, 10), (43, 10), (44, 10);