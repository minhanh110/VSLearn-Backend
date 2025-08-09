DROP DATABASE IF EXISTS VSLearn;
CREATE DATABASE VSLearn;
USE VSLearn;

CREATE TABLE users (
                       id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       user_name VARCHAR(255) NOT NULL,
                       user_password VARCHAR(255),
                       user_email VARCHAR(255) NOT NULL,
                       phone_number VARCHAR(12),
                       user_avatar VARCHAR(255),
                       user_role VARCHAR(255) NOT NULL,
                       provider VARCHAR(20) DEFAULT 'LOCAL',
                       is_active TINYINT(1) ,
                       modify_time DATETIME,
                       active_code VARCHAR(10),
                       created_at DATETIME NOT NULL,
                       created_by INT UNSIGNED,
                       updated_at DATETIME,
                       updated_by INT UNSIGNED,
                       deleted_at DATETIME,
                       deleted_by INT UNSIGNED,
                       UNIQUE KEY user_name (user_name),
                       UNIQUE KEY user_email (user_email),
                       UNIQUE KEY phone_number (phone_number)
);



CREATE TABLE topic (
                       id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       topic_name VARCHAR(255) NOT NULL,
                       is_free TINYINT(1) NOT NULL,
                       status VARCHAR(255) NOT NULL COMMENT 'approve/reject/pending',
                       sort_order INT UNSIGNED NOT NULL DEFAULT 0,
                       created_at DATETIME NOT NULL,
                       created_by INT UNSIGNED NOT NULL,
                       updated_at DATETIME,
                       updated_by INT UNSIGNED,
                       deleted_at DATETIME,
                       deleted_by INT UNSIGNED
);



CREATE TABLE sub_topic (
                           id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           topic_id INT UNSIGNED NOT NULL,
                           sub_topic_name VARCHAR(255) NOT NULL,
                           status VARCHAR(255) NOT NULL,
                           sort_order INT UNSIGNED NOT NULL DEFAULT 0,
                           created_at DATETIME NOT NULL,
                           created_by INT UNSIGNED NOT NULL,
                           updated_at DATETIME,
                           updated_by INT UNSIGNED,
                           deleted_at DATETIME,
                           deleted_by INT UNSIGNED,
                           FOREIGN KEY (topic_id) REFERENCES topic(id) ON DELETE CASCADE

);


CREATE TABLE areas (
                       id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       area_name VARCHAR(255) NOT NULL,
                       created_at DATETIME NOT NULL,
                       created_by INT UNSIGNED NOT NULL,
                       updated_at DATETIME,
                       updated_by INT UNSIGNED,
                       deleted_at DATETIME,
                       deleted_by INT UNSIGNED
);


CREATE TABLE pricing (
                         id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                         pricing_type VARCHAR(255) NOT NULL,
                         price INT UNSIGNED NOT NULL,
                         duration_days INT UNSIGNED NOT NULL,
                         discount_percent DOUBLE UNSIGNED NOT NULL,
                         description VARCHAR(255),
                         created_at DATETIME NOT NULL,
                         created_by INT UNSIGNED NOT NULL,
                         updated_at DATETIME,
                         updated_by INT UNSIGNED,
                         deleted_at DATETIME,
                         deleted_by INT UNSIGNED
);
ALTER TABLE pricing ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;


CREATE TABLE progress (
                          id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          sub_topic_id INT UNSIGNED NOT NULL,
                          created_by INT UNSIGNED NOT NULL,
                          is_complete TINYINT(1) NOT NULL,
                          created_at DATETIME NOT NULL,
                          FOREIGN KEY (sub_topic_id) REFERENCES sub_topic(id) ON DELETE CASCADE,
                          FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE vocab (
                       id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       vocab VARCHAR(255) NOT NULL,
                       created_at DATETIME NOT NULL,
                       created_by INT UNSIGNED NOT NULL,
                       updated_at DATETIME,
                       updated_by INT UNSIGNED,
                       deleted_at DATETIME,
                       deleted_by INT UNSIGNED,
                       sub_topic_id INT UNSIGNED,
                       FOREIGN KEY (sub_topic_id) REFERENCES sub_topic(id) ON DELETE CASCADE
);
ALTER TABLE vocab
    ADD COLUMN meaning TEXT NULL AFTER vocab;
ALTER TABLE vocab
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'pending' AFTER vocab;


CREATE TABLE topic_point (
                             id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             topic_id INT UNSIGNED NOT NULL,
                             feedback_content TEXT,
                             rating INT UNSIGNED,
                             total_point DOUBLE NOT NULL,
                             created_by INT UNSIGNED NOT NULL,
                             created_at DATETIME NOT NULL,
                             updated_at DATETIME,
                             updated_by INT UNSIGNED,
                             FOREIGN KEY (topic_id) REFERENCES topic(id) ON DELETE CASCADE,
                             FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);



CREATE TABLE transactions (
                              id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              pricing_id INT UNSIGNED NOT NULL,
                              start_date DATETIME NOT NULL,
                              end_date DATETIME NOT NULL,
                              code VARCHAR(255) UNIQUE NOT NULL,
                              created_by INT UNSIGNED NOT NULL,
                              created_at DATETIME NOT NULL,
                              FOREIGN KEY (pricing_id) REFERENCES pricing(id) ON DELETE CASCADE,
                              FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);
ALTER TABLE transactions
    ADD COLUMN amount DOUBLE NULL AFTER created_at;

ALTER TABLE transactions
    ADD COLUMN payment_status VARCHAR(20) NULL AFTER amount;

ALTER TABLE transactions
    ADD COLUMN description TEXT NULL AFTER payment_status;








CREATE TABLE vocab_area (
                            id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            vocab_id INT UNSIGNED NOT NULL,
                            area_id INT UNSIGNED NOT NULL,
                            vocab_area_video VARCHAR(500) NOT NULL,
                            vocab_area_description TEXT,
                            created_at DATETIME NOT NULL,
                            created_by INT UNSIGNED NOT NULL,
                            updated_at DATETIME,
                            updated_by INT UNSIGNED,
                            deleted_at DATETIME,
                            deleted_by INT UNSIGNED,
                            FOREIGN KEY (vocab_id) REFERENCES vocab(id) ON DELETE CASCADE,
                            FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE CASCADE
);


CREATE TABLE sentence (
                          id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          sentence_video VARCHAR(255),
                          sentence_description TEXT,
                          sentence_type VARCHAR(255),
                          sentence_topic_id INT UNSIGNED NOT NULL,
                          created_at DATETIME,
                          created_by INT UNSIGNED,
                          updated_at DATETIME,
                          updated_by INT UNSIGNED,
                          deleted_at DATETIME,
                          deleted_by INT UNSIGNED,
                          FOREIGN KEY (sentence_topic_id) REFERENCES topic(id) ON DELETE CASCADE
);


CREATE TABLE sentence_vocab (
                                id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                sentence_id INT UNSIGNED NOT NULL,
                                vocab_id INT UNSIGNED NOT NULL,
                                created_at DATETIME,
                                created_by INT UNSIGNED,
                                updated_at DATETIME,
                                updated_by INT UNSIGNED,
                                deleted_at DATETIME,
                                deleted_by INT UNSIGNED,
                                FOREIGN KEY (sentence_id) REFERENCES sentence(id) ON DELETE CASCADE,
                                FOREIGN KEY (vocab_id) REFERENCES vocab(id) ON DELETE CASCADE
);

CREATE TABLE word (
                      id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      word VARCHAR(255) NOT NULL,
                      created_at DATETIME,
                      created_by INT UNSIGNED,
                      updated_at DATETIME,
                      updated_by INT UNSIGNED,
                      deleted_at DATETIME,
                      deleted_by INT UNSIGNED
);

CREATE TABLE notification (
                              id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              content TEXT NOT NULL,
                              from_user_id INT UNSIGNED NOT NULL,
                              to_user_id INT UNSIGNED NOT NULL,
                              is_send BOOLEAN NOT NULL DEFAULT FALSE,
                              created_at DATETIME NOT NULL,
                              created_by INT UNSIGNED NOT NULL,
                              updated_at DATETIME,
                              updated_by INT UNSIGNED,
                              deleted_at DATETIME,
                              deleted_by INT UNSIGNED,
                              FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
                              FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE notify DROP FOREIGN KEY notify_ibfk_3;

ALTER TABLE notify DROP FOREIGN KEY notify_ibfk_4;

ALTER TABLE notify DROP FOREIGN KEY notify_ibfk_5;

-- các topic và subtopic
INSERT INTO topic (topic_name, is_free, status, created_at, created_by)
VALUES
    ('Bảng chữ cái và số đếm', 1, 'approve', NOW(), 1),
    ('Bản thân', 1, 'approve', NOW(), 1);



INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (1, 'Bảng chữ cái 1', 'approve', NOW(), 1),
    (1, 'Bảng chữ cái 2', 'approve', NOW(), 1),
    (1, 'Số đếm', 'approve', NOW(), 1);

INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (2, 'Con người và đặc điểm', 'approve', NOW(), 1),
    (2, 'Cơ thể con người', 'approve', NOW(), 1),
    (2, 'Hoạt động hàng ngày', 'approve', NOW(), 1),
    (2, 'Giao tiếp và từ để hỏi', 'approve', NOW(), 1);


INSERT INTO topic (topic_name, is_free, status, created_at, created_by)
VALUES
    ('Gia đình', 1, 'approve', NOW(), 1),
    ('Công việc', 1, 'approve', NOW(), 1),
    ('Hiện tượng tự nhiên', 1, 'approve', NOW(), 1),
    ('Thực vật', 1, 'approve', NOW(), 1),
    ('Động vật', 1, 'approve', NOW(), 1),
    ('Trường học', 1, 'approve', NOW(), 1),
    ('Giao thông', 1, 'approve', NOW(), 1),
    ('Quê hương đất nước', 1, 'approve', NOW(), 1);


INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (3, 'Các thành viên trong gia đình', 'approve', NOW(), 1),
    (3, 'Họ hàng', 'approve', NOW(), 1),
    (3, 'Đồ dùng trong gia đình', 'approve', NOW(), 1);


INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (4, 'Công việc', 'approve', NOW(), 1),
    (4, 'Dụng cụ lao động', 'approve', NOW(), 1);


INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (5, 'Thời gian', 'approve', NOW(), 1),
    (5, 'Ngày trong tuần', 'approve', NOW(), 1),
    (5, 'Thời tiết', 'approve', NOW(), 1),
    (5, 'Địa hình', 'approve', NOW(), 1);


INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (6, 'Trái cây', 'approve', NOW(), 1),
    (6, 'Hoa', 'approve', NOW(), 1),
    (6, 'Rau và cây', 'approve', NOW(), 1);


INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (7, 'Thú nuôi và trang trại', 'approve', NOW(), 1),
    (7, 'Thú rừng và động vật dưới nước', 'approve', NOW(), 1),
    (7, 'Chim và côn trùng', 'approve', NOW(), 1);


INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (8, 'Trường học và người trong trường', 'approve', NOW(), 1),
    (8, 'Đồ dùng học tập', 'approve', NOW(), 1),
    (8, 'Thủ công và nghệ thuật', 'approve', NOW(), 1),
    (8, 'Môn học và hoạt động học tập', 'approve', NOW(), 1);



INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (9, 'Phương tiện giao thông và tốc độ', 'approve', NOW(), 1),
    (9, 'Địa điểm giao thông và tín hiệu giao thông', 'approve', NOW(), 1);


INSERT INTO sub_topic (topic_id, sub_topic_name, status, created_at, created_by)
VALUES
    (10, 'Địa điểm', 'approve', NOW(), 1),
    (10, 'Ngày lễ và Tết', 'approve', NOW(), 1);





-- gói học

INSERT INTO pricing (pricing_type, price, duration_days, discount_percent, description, created_at, created_by) VALUES
                                                                                                                    ('1_MONTH', 100000, 30, 0.0, 'Gói học 1 tháng', NOW(), 1),
                                                                                                                    ('3_MONTHS', 250000, 90, 0.1, 'Gói học 3 tháng', NOW(), 1),
                                                                                                                    ('6_MONTHS', 500000, 180, 0.2, 'Gói học 6 tháng', NOW(), 1);




-- Vùng miền

INSERT INTO areas (area_name, created_at, created_by) VALUES
                                                          ('Toàn quốc', NOW(), 1),
                                                          ('Bắc', NOW(), 1),
                                                          ('Trung', NOW(), 1),
                                                          ('Nam', NOW(), 1);


INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('A', 1, NOW(), 1), ('Ă', 1, NOW(), 1), ('Â', 1, NOW(), 1), ('B', 1, NOW(), 1), ('C', 1, NOW(), 1), ('D', 1, NOW(), 1), ('Đ', 1, NOW(), 1), ('E', 1, NOW(), 1), ('Ê', 1, NOW(), 1), ('G', 1, NOW(), 1), ('H', 1, NOW(), 1), ('I', 1, NOW(), 1), ('K', 1, NOW(), 1), ('L', 1, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('M', 2, NOW(), 1), ('N', 2, NOW(), 1),('O', 2, NOW(), 1), ('Ô', 2, NOW(), 1), ('Ơ', 2, NOW(), 1), ('P', 2, NOW(), 1), ('Q', 2, NOW(), 1), ('R', 2, NOW(), 1), ('S', 2, NOW(), 1), ('T', 2, NOW(), 1), ('U', 2, NOW(), 1), ('V', 2, NOW(), 1), ('X', 2, NOW(), 1), ('Y', 2, NOW(), 1), ('Dấu sắc', 2, NOW(), 1), ('Dấu huyền', 2, NOW(), 1), ('Dấu hỏi', 2, NOW(), 1), ('Dấu ngã', 2, NOW(), 1), ('Dấu nặng', 2, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('0', 3, NOW(), 1), ('1', 3, NOW(), 1), ('2', 3, NOW(), 1), ('3', 3, NOW(), 1), ('4', 3, NOW(), 1), ('5', 3, NOW(), 1), ('6', 3, NOW(), 1), ('7', 3, NOW(), 1), ('8', 3, NOW(), 1), ('9', 3, NOW(), 1), ('10', 3, NOW(), 1), ('11', 3, NOW(), 1), ('12', 3, NOW(), 1), ('100', 3, NOW(), 1), ('1000', 3, NOW(), 1), ('5000', 3, NOW(), 1), ('10000', 3, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Bé trai', 4, NOW(), 1), ('Bé gái', 4, NOW(), 1), ('Cao', 4, NOW(), 1), ('Thấp', 4, NOW(), 1), ('Gầy', 4, NOW(), 1), ('Béo', 4, NOW(), 1), ('Khỏe mạnh', 4, NOW(), 1), ('Yếu', 4, NOW(), 1), ('Mệt', 4, NOW(), 1), ('Vui', 4, NOW(), 1), ('Buồn', 4, NOW(), 1), ('No', 4, NOW(), 1), ('Đói', 4, NOW(), 1), ('Tức giận', 4, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Đầu', 5, NOW(), 1), ('Tóc', 5, NOW(), 1), ('Mắt', 5, NOW(), 1), ('Mũi', 5, NOW(), 1), ('Miệng', 5, NOW(), 1), ('Má', 5, NOW(), 1), ('Tai', 5, NOW(), 1), ('Cổ', 5, NOW(), 1), ('Vai', 5, NOW(), 1), ('Bụng', 5, NOW(), 1), ('Tay', 5, NOW(), 1), ('Chân', 5, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Đi', 6, NOW(), 1), ('Đứng', 6, NOW(), 1), ('Ngồi', 6, NOW(), 1), ('Nằm', 6, NOW(), 1), ('Chạy', 6, NOW(), 1), ('Nhảy', 6, NOW(), 1), ('Ngủ', 6, NOW(), 1), ('Ăn', 6, NOW(), 1), ('Uống', 6, NOW(), 1), ('Đi vệ sinh', 6, NOW(), 1), ('Chải đầu', 6, NOW(), 1), ('Mặc quần áo', 6, NOW(), 1), ('Gội đầu', 6, NOW(), 1), ('Rửa tay', 6, NOW(), 1), ('Rửa chân', 6, NOW(), 1), ('Rửa mặt', 6, NOW(), 1), ('Đánh răng', 6, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Bạn', 7, NOW(), 1),
                                                                    ('Bao giờ', 7, NOW(), 1),
                                                                    ('Bao nhiêu', 7, NOW(), 1),
                                                                    ('Bao nhiêu tuổi', 7, NOW(), 1),
                                                                    ('Cảm ơn', 7, NOW(), 1),
                                                                    ('Như thế nào', 7, NOW(), 1),
                                                                    ('Ở đâu', 7, NOW(), 1),
                                                                    ('Tên là gì', 7, NOW(), 1),
                                                                    ('Tôi', 7, NOW(), 1),
                                                                    ('Xin chào', 7, NOW(), 1),
                                                                    ('Xin lỗi', 7, NOW(), 1),
                                                                    ('Xin phép', 7, NOW(), 1);


INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Anh trai', 8, NOW(), 1), ('Ba', 8, NOW(), 1), ('Bố', 8, NOW(), 1), ('Cha mẹ', 8, NOW(), 1), ('Chị', 8, NOW(), 1), ('Chồng', 8, NOW(), 1), ('Con cái', 8, NOW(), 1), ('Con gái', 8, NOW(), 1), ('Con trai', 8, NOW(), 1), ('Em gái', 8, NOW(), 1), ('Em trai', 8, NOW(), 1), ('Em út', 8, NOW(), 1), ('Gia đình', 8, NOW(), 1), ('Mẹ', 8, NOW(), 1), ('Ông', 8, NOW(), 1), ('Ông bà', 8, NOW(), 1), ('Vợ', 8, NOW(), 1), ('Vợ chồng', 8, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Anh rể', 9, NOW(), 1), ('Bác gái', 9, NOW(), 1), ('Bác trai', 9, NOW(), 1), ('Cậu', 9, NOW(), 1), ('Cháu', 9, NOW(), 1), ('Cháu ngoại', 9, NOW(), 1), ('Cháu nội', 9, NOW(), 1), ('Chị dâu', 9, NOW(), 1), ('Chú', 9, NOW(), 1), ('Dì', 9, NOW(), 1), ('Em dâu', 9, NOW(), 1), ('Họ hàng', 9, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Ấm đun', 10, NOW(), 1), ('Bát', 10, NOW(), 1), ('Cái chăn', 10, NOW(), 1), ('Cái chảo', 10, NOW(), 1), ('Cái chậu', 10, NOW(), 1), ('Cái chiếu', 10, NOW(), 1), ('Cái gối', 10, NOW(), 1), ('Chổi quét', 10, NOW(), 1), ('Con dao', 10, NOW(), 1), ('Đèn điện', 10, NOW(), 1), ('Đĩa', 10, NOW(), 1), ('Điện thoại', 10, NOW(), 1), ('Đôi đũa', 10, NOW(), 1), ('Đồng hồ', 10, NOW(), 1), ('Máy tính', 10, NOW(), 1), ('Nồi', 10, NOW(), 1), ('Nồi cơm điện', 10, NOW(), 1), ('Phích nước', 10, NOW(), 1), ('Quạt trần', 10, NOW(), 1), ('Thìa', 10, NOW(), 1), ('Tủ lạnh', 10, NOW(), 1), ('TV', 10, NOW(), 1), ('Xà phòng', 10, NOW(), 1), ('Xô', 10, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Bác sĩ', 11, NOW(), 1),
                                                                    ('Bộ đội', 11, NOW(), 1),
                                                                    ('Cô giáo', 11, NOW(), 1),
                                                                    ('Con người', 11, NOW(), 1),
                                                                    ('Công an', 11, NOW(), 1),
                                                                    ('Công nhân', 11, NOW(), 1),
                                                                    ('Công việc', 11, NOW(), 1),
                                                                    ('Dạy học', 11, NOW(), 1),
                                                                    ('Người bán hàng', 11, NOW(), 1),
                                                                    ('Nông dân', 11, NOW(), 1),
                                                                    ('Thợ thêu', 11, NOW(), 1),
                                                                    ('Thợ xây', 11, NOW(), 1),
                                                                    ('Y tá', 11, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Cây lúa', 12, NOW(), 1),
                                                                    ('Chỉ khâu', 12, NOW(), 1),
                                                                    ('Cối sáo', 12, NOW(), 1),
                                                                    ('Cưa', 12, NOW(), 1),
                                                                    ('Gạch', 12, NOW(), 1),
                                                                    ('Kim khâu', 12, NOW(), 1),
                                                                    ('Máy cày', 12, NOW(), 1),
                                                                    ('Máy khâu', 12, NOW(), 1),
                                                                    ('Tiệm', 12, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Buổi chiều', 13, NOW(), 1),
                                                                    ('Buổi đêm', 13, NOW(), 1),
                                                                    ('Buổi sáng', 13, NOW(), 1),
                                                                    ('Buổi tối', 13, NOW(), 1),
                                                                    ('Buổi trưa', 13, NOW(), 1),
                                                                    ('Mùa đông', 13, NOW(), 1),
                                                                    ('Mùa hè', 13, NOW(), 1),
                                                                    ('Mùa thu', 13, NOW(), 1),
                                                                    ('Mùa xuân', 13, NOW(), 1),
                                                                    ('Năm', 13, NOW(), 1),
                                                                    ('Ngày', 13, NOW(), 1),
                                                                    ('Tháng', 13, NOW(), 1),
                                                                    ('Tuần', 13, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Chủ nhật', 14, NOW(), 1),
                                                                    ('Hôm nay', 14, NOW(), 1),
                                                                    ('Hôm qua', 14, NOW(), 1),
                                                                    ('Ngày kia', 14, NOW(), 1),
                                                                    ('Ngày mai', 14, NOW(), 1),
                                                                    ('Thứ ba', 14, NOW(), 1),
                                                                    ('Thứ bảy', 14, NOW(), 1),
                                                                    ('Thứ hai', 14, NOW(), 1),
                                                                    ('Thứ năm', 14, NOW(), 1),
                                                                    ('Thứ sáu', 14, NOW(), 1),
                                                                    ('Thứ tư', 14, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Ẩm ướt', 15, NOW(), 1),
                                                                    ('Bầu trời', 15, NOW(), 1),
                                                                    ('Gió', 15, NOW(), 1),
                                                                    ('Giông bão', 15, NOW(), 1),
                                                                    ('Khô', 15, NOW(), 1),
                                                                    ('Lạnh', 15, NOW(), 1),
                                                                    ('Lũ', 15, NOW(), 1),
                                                                    ('Mặt trăng', 15, NOW(), 1),
                                                                    ('Mặt trời', 15, NOW(), 1),
                                                                    ('Mưa', 15, NOW(), 1),
                                                                    ('Nắng', 15, NOW(), 1),
                                                                    ('Nóng', 15, NOW(), 1),
                                                                    ('Sấm chớp', 15, NOW(), 1);


INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Ao hồ', 16, NOW(), 1),
                                                                    ('Biển', 16, NOW(), 1),
                                                                    ('Con sông', 16, NOW(), 1),
                                                                    ('Đèo', 16, NOW(), 1),
                                                                    ('Khu rừng', 16, NOW(), 1),
                                                                    ('Núi cao', 16, NOW(), 1),
                                                                    ('Suối', 16, NOW(), 1),
                                                                    ('Thác nước', 16, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Cam', 17, NOW(), 1),
                                                                    ('Chanh', 17, NOW(), 1),
                                                                    ('Chuối', 17, NOW(), 1),
                                                                    ('Dưa hấu', 17, NOW(), 1),
                                                                    ('Khóm dừa', 17, NOW(), 1),
                                                                    ('Mít', 17, NOW(), 1),
                                                                    ('Na', 17, NOW(), 1),
                                                                    ('Nho', 17, NOW(), 1),
                                                                    ('Ổi', 17, NOW(), 1),
                                                                    ('Quýt', 17, NOW(), 1),
                                                                    ('Táo', 17, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Hoa cúc', 18, NOW(), 1),
                                                                    ('Hoa đào', 18, NOW(), 1),
                                                                    ('Hoa hồng', 18, NOW(), 1),
                                                                    ('Hoa hướng dương', 18, NOW(), 1),
                                                                    ('Hoa mai', 18, NOW(), 1),
                                                                    ('Hoa nhài', 18, NOW(), 1),
                                                                    ('Hoa phượng đỏ', 18, NOW(), 1),
                                                                    ('Hoa sen', 18, NOW(), 1),
                                                                    ('Hoa sữa', 18, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Cà chua', 19, NOW(), 1),
                                                                    ('Cành cây', 19, NOW(), 1),
                                                                    ('Gốc cây', 19, NOW(), 1),
                                                                    ('Khoai lang', 19, NOW(), 1),
                                                                    ('Khoai tây', 19, NOW(), 1),
                                                                    ('Lá cây', 19, NOW(), 1),
                                                                    ('Ớt', 19, NOW(), 1),
                                                                    ('Rau bắp cải', 19, NOW(), 1),
                                                                    ('Rau muống', 19, NOW(), 1),
                                                                    ('Rễ cây', 19, NOW(), 1),
                                                                    ('Súp lơ', 19, NOW(), 1),
                                                                    ('Thân cây', 19, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Chó', 20, NOW(), 1),
                                                                    ('Con bò', 20, NOW(), 1),
                                                                    ('Cừu', 20, NOW(), 1),
                                                                    ('Dê', 20, NOW(), 1),
                                                                    ('Gà', 20, NOW(), 1),
                                                                    ('Gà mái', 20, NOW(), 1),
                                                                    ('Gà trống', 20, NOW(), 1),
                                                                    ('Heo', 20, NOW(), 1),
                                                                    ('Mèo', 20, NOW(), 1),
                                                                    ('Ngựa', 20, NOW(), 1),
                                                                    ('Thỏ con', 20, NOW(), 1),
                                                                    ('Trâu', 20, NOW(), 1),
                                                                    ('Vịt', 20, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Cá', 21, NOW(), 1),
                                                                    ('Cá heo', 21, NOW(), 1),
                                                                    ('Cóc', 21, NOW(), 1),
                                                                    ('Cua', 21, NOW(), 1),
                                                                    ('Ếch', 21, NOW(), 1),
                                                                    ('Gấu', 21, NOW(), 1),
                                                                    ('Hổ', 21, NOW(), 1),
                                                                    ('Hươu', 21, NOW(), 1),
                                                                    ('Mực', 21, NOW(), 1),
                                                                    ('Nai', 21, NOW(), 1),
                                                                    ('Ốc', 21, NOW(), 1),
                                                                    ('Rắn', 21, NOW(), 1),
                                                                    ('Sóc', 21, NOW(), 1),
                                                                    ('Sói', 21, NOW(), 1),
                                                                    ('Sư tử', 21, NOW(), 1),
                                                                    ('Tôm', 21, NOW(), 1),
                                                                    ('Tran', 21, NOW(), 1),
                                                                    ('Voi', 21, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Bướm', 22, NOW(), 1),
                                                                    ('Chim', 22, NOW(), 1),
                                                                    ('Chim bồ câu', 22, NOW(), 1),
                                                                    ('Con cò', 22, NOW(), 1),
                                                                    ('Công', 22, NOW(), 1),
                                                                    ('Cú mèo', 22, NOW(), 1),
                                                                    ('Dế', 22, NOW(), 1),
                                                                    ('Gian', 22, NOW(), 1),
                                                                    ('Muỗi', 22, NOW(), 1),
                                                                    ('Ngỗng', 22, NOW(), 1),
                                                                    ('Ong', 22, NOW(), 1),
                                                                    ('Ruồi', 22, NOW(), 1),
                                                                    ('Vẹt', 22, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Đại học', 23, NOW(), 1),
                                                                    ('Đi học', 23, NOW(), 1),
                                                                    ('Hiệu trưởng', 23, NOW(), 1),
                                                                    ('Lớp học', 23, NOW(), 1),
                                                                    ('Mầm non', 23, NOW(), 1),
                                                                    ('Nhà trường', 23, NOW(), 1),
                                                                    ('Phổ thông cơ sở cấp hai', 23, NOW(), 1),
                                                                    ('Thời khóa biểu', 23, NOW(), 1),
                                                                    ('Tiểu học', 23, NOW(), 1),
                                                                    ('Trung học phổ thông cấp ba', 23, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Bàn học', 24, NOW(), 1),
                                                                    ('Bảng đen', 24, NOW(), 1),
                                                                    ('Bút bi', 24, NOW(), 1),
                                                                    ('Bút chì', 24, NOW(), 1),
                                                                    ('Bút mực', 24, NOW(), 1),
                                                                    ('Cặp sách', 24, NOW(), 1),
                                                                    ('Cục tẩy', 24, NOW(), 1),
                                                                    ('Ghế', 24, NOW(), 1),
                                                                    ('Gọt bút chì', 24, NOW(), 1),
                                                                    ('Hộp bút', 24, NOW(), 1),
                                                                    ('Lọ mực', 24, NOW(), 1),
                                                                    ('Quyển vở', 24, NOW(), 1),
                                                                    ('Sách', 24, NOW(), 1),
                                                                    ('Thước', 24, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Ba lô', 25, NOW(), 1),
                                                                    ('Cái kéo', 25, NOW(), 1),
                                                                    ('Giấy màu', 25, NOW(), 1),
                                                                    ('Hạt hồ', 25, NOW(), 1),
                                                                    ('Họa đàn', 25, NOW(), 1),
                                                                    ('Kể chuyện', 25, NOW(), 1),
                                                                    ('Múa', 25, NOW(), 1),
                                                                    ('Nhào nặn', 25, NOW(), 1),
                                                                    ('Phấn viết bảng', 25, NOW(), 1),
                                                                    ('Tô màu', 25, NOW(), 1),
                                                                    ('Vẽ tranh', 25, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Chăm chỉ', 26, NOW(), 1),
                                                                    ('Cố gắng', 26, NOW(), 1),
                                                                    ('Dạy học', 26, NOW(), 1),
                                                                    ('Đọc sách', 26, NOW(), 1),
                                                                    ('Đúng', 26, NOW(), 1),
                                                                    ('Ghi nhớ', 26, NOW(), 1),
                                                                    ('Hiểu bài', 26, NOW(), 1),
                                                                    ('Học giỏi', 26, NOW(), 1),
                                                                    ('Học kém', 26, NOW(), 1),
                                                                    ('Học tập', 26, NOW(), 1),
                                                                    ('Không hiểu', 26, NOW(), 1),
                                                                    ('Làm bài tập', 26, NOW(), 1),
                                                                    ('Lười học', 26, NOW(), 1),
                                                                    ('Môn địa lý', 26, NOW(), 1),
                                                                    ('Môn lịch sử', 26, NOW(), 1),
                                                                    ('Môn tiếng Anh', 26, NOW(), 1),
                                                                    ('Môn tiếng Việt', 26, NOW(), 1),
                                                                    ('Môn toán', 26, NOW(), 1),
                                                                    ('Muốn', 26, NOW(), 1),
                                                                    ('Quên bài', 26, NOW(), 1),
                                                                    ('Sai', 26, NOW(), 1),
                                                                    ('Suy nghĩ', 26, NOW(), 1),
                                                                    ('Tập viết', 26, NOW(), 1),
                                                                    ('Trả lời', 26, NOW(), 1),
                                                                    ('Về nhà', 26, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Chậm chạp', 27, NOW(), 1),
                                                                    ('Đất', 27, NOW(), 1),
                                                                    ('Giao thông', 27, NOW(), 1),
                                                                    ('Máy bay', 27, NOW(), 1),
                                                                    ('Nhanh', 27, NOW(), 1),
                                                                    ('Ô tô', 27, NOW(), 1),
                                                                    ('Tàu hỏa', 27, NOW(), 1),
                                                                    ('Tàu thuyền', 27, NOW(), 1),
                                                                    ('Xe buýt', 27, NOW(), 1),
                                                                    ('Xe đạp', 27, NOW(), 1),
                                                                    ('Xe máy', 27, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Đèn đỏ', 28, NOW(), 1),
                                                                    ('Đèn vàng', 28, NOW(), 1),
                                                                    ('Đèn xanh', 28, NOW(), 1),
                                                                    ('Ngã ba', 28, NOW(), 1),
                                                                    ('Ngã tư', 28, NOW(), 1),
                                                                    ('Nhà ga', 28, NOW(), 1),
                                                                    ('Sân bay', 28, NOW(), 1),
                                                                    ('Trạm xe buýt', 28, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Chùa', 29, NOW(), 1),
                                                                    ('Chùa một cột', 29, NOW(), 1),
                                                                    ('Con đường', 29, NOW(), 1),
                                                                    ('Công viên', 29, NOW(), 1),
                                                                    ('Khu vườn', 29, NOW(), 1),
                                                                    ('Lăng Bác', 29, NOW(), 1),
                                                                    ('Làng xóm', 29, NOW(), 1),
                                                                    ('Nhà sàn', 29, NOW(), 1),
                                                                    ('Nhà táng', 29, NOW(), 1),
                                                                    ('Nhà thờ', 29, NOW(), 1),
                                                                    ('Nông thôn', 29, NOW(), 1),
                                                                    ('Thành phố', 29, NOW(), 1),
                                                                    ('Văn miếu', 29, NOW(), 1),
                                                                    ('Việt Nam', 29, NOW(), 1),
                                                                    ('Vịnh Hạ Long', 29, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Bác Hồ', 30, NOW(), 1),
                                                                    ('Bánh nướng', 30, NOW(), 1),
                                                                    ('Pháo hoa', 30, NOW(), 1),
                                                                    ('Tết Âm', 30, NOW(), 1),
                                                                    ('Tết Dương', 30, NOW(), 1),
                                                                    ('Trung Thu', 30, NOW(), 1);

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_1_Bảng chữ cái 1/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'A' THEN 'a_1.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ă' THEN 'a_2.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Â' THEN 'a_3.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'B' THEN 'b.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'C' THEN 'c.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'D' THEN 'd_1.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đ' THEN 'd_2.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'E' THEN 'e_1.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ê' THEN 'e_2.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'G' THEN 'g.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'H' THEN 'h.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'I' THEN 'i.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'K' THEN 'k.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'L' THEN 'l.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 1;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_2_Bảng chữ cái 2/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'M' THEN 'm.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'N' THEN 'n.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'O' THEN 'o_1.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ô' THEN 'o_2.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ơ' THEN 'o_3.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'P' THEN 'p.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Q' THEN 'q.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'R' THEN 'r.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'S' THEN 's.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'T' THEN 't.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'U' THEN 'u.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'V' THEN 'v.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'X' THEN 'x.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Y' THEN 'y.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dấu sắc' THEN 'dau_sac.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dấu huyền' THEN 'dau_huyen.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dấu hỏi' THEN 'dau_hoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dấu ngã' THEN 'dau_nga.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dấu nặng' THEN 'dau_nang.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 2;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_3_Số đếm/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = '0' THEN '0.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '1' THEN '1.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '2' THEN '2.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '3' THEN '3.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '4' THEN '4.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '5' THEN '5.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '6' THEN '6.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '7' THEN '7.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '8' THEN '8.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '9' THEN '9.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '10' THEN '10.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '11' THEN '11.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '12' THEN '12.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '100' THEN '100.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '1000' THEN '1000.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '5000' THEN '5000.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = '10000' THEN '10000.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 3;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_2_Bản thân/Subtopic_1_Con người và đặc điểm/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bé trai' THEN 'be_trai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bé gái' THEN 'be_gai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cao' THEN 'cao_rao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thấp' THEN 'thap_nguoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gầy' THEN 'gay_go.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Béo' THEN 'beo_phi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Khỏe mạnh' THEN 'khoe_manh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Yếu' THEN 'yeu_ot.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mệt' THEN 'met_moi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Vui' THEN 'vui_ve.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Buồn' THEN 'buon_tham.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'No' THEN 'no_bung.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đói' THEN 'doi_bung.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tức giận' THEN 'tuc_gian.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 4;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_2_Bản thân/Subtopic_2_Cơ thể con người/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tay' THEN 'ban_tay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Vai' THEN 'bo_vai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bụng' THEN 'bung_da.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cổ' THEN 'cai_co.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Miệng' THEN 'cai_mieng.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mũi' THEN 'cai_mui.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Má' THEN 'cap_ma.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đầu' THEN 'dau_nguoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chân' THEN 'doi_chan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mắt' THEN 'doi_mat.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tai' THEN 'doi_tai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tóc' THEN 'mai_toc.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 5;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_2_Bản thân/Subtopic_3_Hoạt động hàng ngày/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ăn' THEN 'an_com.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chải đầu' THEN 'chai_toc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chạy' THEN 'chay_di.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đánh răng' THEN 'danh_rang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngủ' THEN 'di_ngu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đi' THEN 'di_ra_ngoai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đi vệ sinh' THEN 'di_ve_sinh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đứng' THEN 'dung_len.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gội đầu' THEN 'goi_dau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mặc quần áo' THEN 'mac_quan_ao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nằm' THEN 'nam_xuong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngồi' THEN 'ngoi_xuong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nhảy' THEN 'nhay_len.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Rửa chân' THEN 'rua_chan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Rửa mặt' THEN 'rua_mat.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Rửa tay' THEN 'rua_tay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Uống' THEN 'uong_nuoc.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 6;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_2_Bản thân/Subtopic_4_Giao tiếp/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bạn' THEN 'ban.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bao giờ' THEN 'bao_gio.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bao nhiêu' THEN 'bao_nhieu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bao nhiêu tuổi' THEN 'bao_nhieu_tuoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cảm ơn' THEN 'cam_on.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Như thế nào' THEN 'nhu_the_nao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ở đâu' THEN 'o_dau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tên là gì' THEN 'ten_la_gi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tôi' THEN 'toi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Xin chào' THEN 'xin_chao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Xin lỗi' THEN 'xin_loi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Xin phép' THEN 'xin_phep.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 7;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_3_Gia đình/Subtopic_1_Thành viên gia đình/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Anh trai' THEN 'anh_trai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ba' THEN 'ba.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bố' THEN 'bo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cha mẹ' THEN 'cha_me.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chị' THEN 'chi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chồng' THEN 'chong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con cái' THEN 'con_cai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con gái' THEN 'con_gai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con trai' THEN 'con_trai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Em gái' THEN 'em_gai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Em trai' THEN 'em_trai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Em út' THEN 'em_ut.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gia đình' THEN 'gia_dinh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mẹ' THEN 'me.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ông' THEN 'ong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ông bà' THEN 'ong_ba.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Vợ' THEN 'vo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Vợ chồng' THEN 'vo_chong.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 8;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_3_Gia đình/Subtopic_2_Họ hàng/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Anh rể' THEN 'anh_re.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bác gái' THEN 'bac_gai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bác trai' THEN 'bac_trai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cậu' THEN 'cau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cháu' THEN 'chau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cháu ngoại' THEN 'chau_ngoai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cháu nội' THEN 'chau_noi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chị dâu' THEN 'chi_dau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chú' THEN 'chu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dì' THEN 'di.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Em dâu' THEN 'em_dau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Họ hàng' THEN 'ho_hang.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 9;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_3_Gia đình/Subtopic_3_Đồ dùng gia dụng/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ấm đun' THEN 'am_dun.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bát' THEN 'bat.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cái chăn' THEN 'cai_chan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cái chảo' THEN 'cai_chao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cái chậu' THEN 'cai_chau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cái chiếu' THEN 'cai_chieu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cái gối' THEN 'cai_goi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chổi quét' THEN 'choi_quet.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con dao' THEN 'con_dao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đèn điện' THEN 'den_dien.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đĩa' THEN 'dia.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Điện thoại' THEN 'dien_thoai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đôi đũa' THEN 'doi_dua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đồng hồ' THEN 'dong_ho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Máy tính' THEN 'may_tinh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nồi' THEN 'noi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nồi cơm điện' THEN 'noi_com_dien.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Phích nước' THEN 'phich_nuoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Quạt trần' THEN 'quat_tran.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thìa' THEN 'thia.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tủ lạnh' THEN 'tu_lanh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'TV' THEN 'tv.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Xà phòng' THEN 'xa_phong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Xô' THEN 'xo.mp4'
                  ELSE NULL
                  END
       ),
       NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 10;


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_4_Nghề nghiệp/Subtopic_1_Công việc/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bác sĩ' THEN 'bac_si.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bộ đội' THEN 'bo_doi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cô giáo' THEN 'co_giao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con người' THEN 'con_nguoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Công an' THEN 'cong_an.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Công nhân' THEN 'cong_nhan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Công việc' THEN 'cong_viec.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dạy học' THEN 'day_hoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Người bán hàng' THEN 'nguoi_ban_hang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nông dân' THEN 'nong_dan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thợ thêu' THEN 'tho_theu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thợ xây' THEN 'tho_xay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Y tá' THEN 'y_ta.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 11;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_4_Nghề nghiệp/Subtopic_2_Công cụ lao động/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cây lúa' THEN 'cay_lua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chỉ khâu' THEN 'chi_khau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cối sáo' THEN 'coi_sao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cưa' THEN 'cua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gạch' THEN 'gach.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Kim khâu' THEN 'kim_khau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Máy cày' THEN 'may_cay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Máy khâu' THEN 'may_khau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tiệm' THEN 'tiem.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 12;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_5_Hiện tượng tự nhiên/Subtopic_1_Thời gian/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Buổi chiều' THEN 'buoi_chieu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Buổi đêm' THEN 'buoi_dem.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Buổi sáng' THEN 'buoi_sang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Buổi tối' THEN 'buoi_toi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Buổi trưa' THEN 'buoi_trua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mùa đông' THEN 'mua_dong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mùa hè' THEN 'mua_he.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mùa thu' THEN 'mua_thu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mùa xuân' THEN 'mua_xuan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Năm' THEN 'nam.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngày' THEN 'ngay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tháng' THEN 'thang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tuần' THEN 'tuan.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 13;


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_5_Hiện tượng tự nhiên/Subtopic_2_Ngày trong tuần/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chủ nhật' THEN 'chu_nhat.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hôm nay' THEN 'hom_nay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hôm qua' THEN 'hom_qua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngày kia' THEN 'ngay_kia.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngày mai' THEN 'ngay_mai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thứ ba' THEN 'thu_ba.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thứ bảy' THEN 'thu_bay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thứ hai' THEN 'thu_hai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thứ năm' THEN 'thu_nam.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thứ sáu' THEN 'thu_sau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thứ tư' THEN 'thu_tu.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 14;


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_5_Hiện tượng tự nhiên/Subtopic_3_Thời tiết/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ẩm ướt' THEN 'am_uot.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bầu trời' THEN 'bau_troi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gió' THEN 'gio.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Giông bão' THEN 'giong_bao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Khô' THEN 'kho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Lạnh' THEN 'lanh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Lũ' THEN 'lu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mặt trăng' THEN 'mat_trang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mặt trời' THEN 'mat_troi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mưa' THEN 'mua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nắng' THEN 'nang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nóng' THEN 'nong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Sấm chớp' THEN 'sam_chop.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 15;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_5_Hiện tượng tự nhiên/Subtopic_4_Địa hình/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ao hồ' THEN 'ao_ho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Biển' THEN 'bien.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con sông' THEN 'con_song.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đèo' THEN 'deo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Khu rừng' THEN 'khu_rung.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Núi cao' THEN 'nui_cao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Suối' THEN 'suoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thác nước' THEN 'thac_nuoc.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 16;

SET SQL_SAFE_UPDATES = 0;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_6_Thực vật/Subtopic_1_Trái cây/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cam' THEN 'cam.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chanh' THEN 'chanh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chuối' THEN 'chuoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dưa hấu' THEN 'dua_hau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Khóm dừa' THEN 'khom_dua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mít' THEN 'mit.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Na' THEN 'na.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nho' THEN 'nho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ổi' THEN 'oi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Quýt' THEN 'quyt.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Táo' THEN 'tao.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 17;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_6_Thực vật/Subtopic_2_Loài hoa/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa cúc' THEN 'hoa_cuc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa đào' THEN 'hoa_dao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa hồng' THEN 'hoa_hong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa hướng dương' THEN 'hoa_huong_duong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa mai' THEN 'hoa_mai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa nhài' THEN 'hoa_nhai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa phượng đỏ' THEN 'hoa_phuong_do.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa sen' THEN 'hoa_sen.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hoa sữa' THEN 'hoa_sua.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 18;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_6_Thực vật/Subtopic_3_Cây và rau/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cà chua' THEN 'ca_chua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cành cây' THEN 'canh_cay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gốc cây' THEN 'goc_cay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Khoai lang' THEN 'khoai_lang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Khoai tây' THEN 'khoai_tay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Lá cây' THEN 'la_cay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ớt' THEN 'ot.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Rau bắp cải' THEN 'rau_bap_cai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Rau muống' THEN 'rau_muong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Rễ cây' THEN 're_cay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Súp lơ' THEN 'su_hao.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thân cây' THEN 'than_cay.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 19;


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_7_Động vật/Subtopic_1_Thú nuôi và trang trại/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chó' THEN 'cho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con bò' THEN 'con_bo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cừu' THEN 'cuu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dê' THEN 'de.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gà' THEN 'ga.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gà mái' THEN 'ga_mai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gà trống' THEN 'ga_trong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Heo' THEN 'heo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mèo' THEN 'meo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngựa' THEN 'ngua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thỏ con' THEN 'tho_con.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Trâu' THEN 'trau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Vịt' THEN 'vit.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 20;


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_7_Động vật/Subtopic_2_Thú rừng và dưới nước/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cá' THEN 'ca.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cá heo' THEN 'ca_heo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cóc' THEN 'coc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cua' THEN 'cua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ếch' THEN 'ech.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gấu' THEN 'gau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hổ' THEN 'ho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hươu' THEN 'huou.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mực' THEN 'muc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nai' THEN 'nai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ốc' THEN 'oc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Rắn' THEN 'ran.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Sóc' THEN 'soc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Sói' THEN 'soi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Sư tử' THEN 'su_tu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tôm' THEN 'tom.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tran' THEN 'tran.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Voi' THEN 'voi.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 21;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_7_Động vật/Subtopic_3_Chim và côn trùng/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bướm' THEN 'buom.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chim' THEN 'chim.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chim bồ câu' THEN 'chim_bo_cau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con cò' THEN 'con_co.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Công' THEN 'cong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cú mèo' THEN 'cu_meo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dế' THEN 'de.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gian' THEN 'gian.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Muỗi' THEN 'muoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngỗng' THEN 'ngong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ong' THEN 'ong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ruồi' THEN 'ruoi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Vẹt' THEN 'vet.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 22;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_8_Trường học/Subtopic_1_Trường học/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đại học' THEN 'dai_hoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đi học' THEN 'di_hoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hiệu trưởng' THEN 'hieu_truong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Lớp học' THEN 'lop_hoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Mầm non' THEN 'mam_non.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nhà trường' THEN 'nha_truong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Phổ thông cơ sở cấp hai' THEN 'pho_thong_co_so_cap_hai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thời khóa biểu' THEN 'thoi_khoa_bieu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tiểu học' THEN 'tieu_hoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Trung học phổ thông cấp ba' THEN 'trung_hoc_pho_thong_cap_ba.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 23;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_8_Trường học/Subtopic_2_Đồ dùng học tập/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bàn học' THEN 'ban_hoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bảng đen' THEN 'bang_den.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bút bi' THEN 'but_bi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bút chì' THEN 'but_chi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bút mực' THEN 'but_muc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cặp sách' THEN 'cap_sach.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cục tẩy' THEN 'cuc_tay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ghế' THEN 'ghe.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Gọt bút chì' THEN 'got_but_chi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hộp bút' THEN 'hop_but.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Lọ mực' THEN 'lo_muc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Quyển vở' THEN 'quyen_vo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Sách' THEN 'sach.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thước' THEN 'thuoc.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 24;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_8_Trường học/Subtopic_3_Thủ công và nghệ thuật/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ba lô' THEN 'ba_lo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cái kéo' THEN 'cai_keo.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Giấy màu' THEN 'giay_mau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hạt hồ' THEN 'hat_ho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Họa đàn' THEN 'ho_dan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Kể chuyện' THEN 'ke_chuyen.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Múa' THEN 'mua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nhào nặn' THEN 'nhao_nan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Phấn viết bảng' THEN 'phan_viet_bang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tô màu' THEN 'to_mau.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Vẽ tranh' THEN 've_tranh.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 25;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_8_Trường học/Subtopic_4_Môn học và hoạt động học tập/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chăm chỉ' THEN 'cham_chi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Cố gắng' THEN 'co_gang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Dạy học' THEN 'day_hoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đọc sách' THEN 'doc_sach.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đúng' THEN 'dung.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ghi nhớ' THEN 'ghi_nho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Hiểu bài' THEN 'hieu_bai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Học giỏi' THEN 'hoc_gioi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Học kém' THEN 'hoc_kem.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Học tập' THEN 'hoc_tap.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Không hiểu' THEN 'khong_hieu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Làm bài tập' THEN 'lam_bai_tap.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Lười học' THEN 'luoi_hoc.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Môn địa lý' THEN 'mon_dia_ly.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Môn lịch sử' THEN 'mon_lich_su.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Môn tiếng Anh' THEN 'mon_tieng_anh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Môn tiếng Việt' THEN 'mon_tieng_viet.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Môn toán' THEN 'mon_toan.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Muốn' THEN 'muon.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Quên bài' THEN 'quen_bai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Sai' THEN 'sai.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Suy nghĩ' THEN 'suy_nghi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tập viết' THEN 'tap_viet.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Trả lời' THEN 'tra_loi.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Về nhà' THEN 've_nha.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 26;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_9_Giao thông/Subtopic_1_Phương tiện và tốc độ/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chậm chạp' THEN 'cham_chap.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đất' THEN 'dat.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Giao thông' THEN 'giao_thong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Máy bay' THEN 'may_bay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nhanh' THEN 'nhanh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ô tô' THEN 'oto.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tàu hỏa' THEN 'tau_hoa.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tàu thuyền' THEN 'tau_thuyen.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Xe buýt' THEN 'xe_buyt.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Xe đạp' THEN 'xe_dap.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Xe máy' THEN 'xe_may.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 27;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_9_Giao thông/Subtopic_2_Địa điểm và tín hiệu/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đèn đỏ' THEN 'den_do.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đèn vàng' THEN 'den_vang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Đèn xanh' THEN 'den_xanh.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngã ba' THEN 'nga_ba.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Ngã tư' THEN 'nga_tu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nhà ga' THEN 'nha_ga.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Sân bay' THEN 'san_bay.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Trạm xe buýt' THEN 'tram_xe_buyt.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 28;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_10_Quê hương đất nước/Subtopic_1_Địa điểm/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chùa' THEN 'chua.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Chùa một cột' THEN 'chua_mot_cot.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Con đường' THEN 'con_duong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Công viên' THEN 'cong_vien.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Khu vườn' THEN 'khu_vuon.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Lăng Bác' THEN 'lang_bac.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Làng xóm' THEN 'lang_xom.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nhà sàn' THEN 'nha_san.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nhà táng' THEN 'nha_tang.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nhà thờ' THEN 'nha_tho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Nông thôn' THEN 'nong_thon.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Thành phố' THEN 'thanh_pho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Văn miếu' THEN 'van_mieu.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Việt Nam' THEN 'viet_nam.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Vịnh Hạ Long' THEN 'vinh_ha_long.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 29;

INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
SELECT v.id, 1,
       CONCAT('Topic_10_Quê hương đất nước/Subtopic_2_Ngày lễ/',
              CASE
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bác Hồ' THEN 'bac_ho.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Bánh nướng' THEN 'banh_nuong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Pháo hoa' THEN 'phao_hoa.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tết Âm' THEN 'tet_am.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Tết Dương' THEN 'tet_duong.mp4'
                  WHEN v.vocab COLLATE utf8mb4_bin = 'Trung Thu' THEN 'trung_thu.mp4'
                  ELSE NULL
                  END
       ), NOW(), 1
FROM vocab v
WHERE v.sub_topic_id = 30;

INSERT INTO word (word, created_at, created_by) VALUES
                                                    ('quá', NOW(), 1),
                                                    ('vậy', NOW(), 1),
                                                    ('ấy', NOW(), 1),
                                                    ('không', NOW(), 1),
                                                    ('mà', NOW(), 1),
                                                    ('quá', NOW(), 1),
                                                    ('phải', NOW(), 1),
                                                    ('nên', NOW(), 1),
                                                    ('đang', NOW(), 1),
                                                    ('đã', NOW(), 1);


INSERT INTO sentence (sentence_video, sentence_description, sentence_type, sentence_topic_id, created_at, created_by) VALUES
    ('Topic_2_sentence_Bản thân/Bạn tên là gì.mp4', 'Bạn tên là gì', 'Câu hỏi', 2, NOW(), 1);


-- Thêm sentence_vocab để liên kết sentence với vocab
INSERT INTO sentence_vocab (sentence_id, vocab_id, created_at, created_by)
SELECT 1, v.id, NOW(), 1
FROM vocab v
WHERE v.vocab = 'Bạn' AND v.sub_topic_id = 7;

INSERT INTO sentence_vocab (sentence_id, vocab_id, created_at, created_by)
SELECT 1, v.id, NOW(), 1
FROM vocab v
WHERE v.vocab = 'Tên là gì' AND v.sub_topic_id = 7;

INSERT INTO sentence (sentence_video, sentence_description, sentence_type, sentence_topic_id, created_at, created_by) VALUES
    ('Topic_2_sentence_Bản thân/Bạn bao nhiêu tuổi.mp4', 'Bạn bao nhiêu tuổi', 'Câu hỏi', 2, NOW(), 1);


-- Thêm sentence_vocab để liên kết sentence với vocab
INSERT INTO sentence_vocab (sentence_id, vocab_id, created_at, created_by)
SELECT 2, v.id, NOW(), 1
FROM vocab v
WHERE v.vocab = 'Bạn' AND v.sub_topic_id = 7;
INSERT INTO sentence_vocab (sentence_id, vocab_id, created_at, created_by)
SELECT 2, v.id, NOW(), 1
FROM vocab v
WHERE v.vocab = 'Bao nhiêu tuổi' AND v.sub_topic_id = 7;




DELETE FROM vocab_area WHERE area_id = 1 AND vocab_id IN (SELECT id FROM vocab WHERE sub_topic_id = 1);

SET SQL_SAFE_UPDATES = 1;

-- INSERT INTO vocab (vocab, meaning, sub_topic_id, created_at, created_by)
-- VALUES
--   ('bổ ích', NULL, NULL, NOW(), 1),
--   ('bồ kết', NULL, NULL, NOW(), 1),
--   ('bờ rào', NULL, NULL, NOW(), 1),
--   ('bổ ngữ', NULL, NULL, NOW(), 1),
--   ('bổ dưỡng', NULL, NULL, NOW(), 1);
--
-- INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, created_at, created_by)
-- SELECT * FROM (
--   SELECT v.id, a.area_id,
--     CASE
--       WHEN v.vocab COLLATE utf8mb4_bin = 'bổ dưỡng' AND a.area_id = 1 THEN '19-36/bo_duong.mp4'
--       WHEN v.vocab COLLATE utf8mb4_bin = 'bổ dưỡng' AND a.area_id = 2 THEN '19-36/bo_duong_mb.mp4'
--       WHEN v.vocab COLLATE utf8mb4_bin = 'bổ dưỡng' AND a.area_id = 3 THEN '19-36/bo_duong_mt.mp4'
--       WHEN v.vocab COLLATE utf8mb4_bin = 'bổ dưỡng' AND a.area_id = 4 THEN '19-36/bo_duong_mn.mp4'
--       WHEN v.vocab COLLATE utf8mb4_bin = 'bổ ích' AND a.area_id = 1 THEN '19-36/bo_ich.mp4'
--       WHEN v.vocab COLLATE utf8mb4_bin = 'bồ kết' AND a.area_id = 1 THEN '19-36/bo_ket.mp4'
--       WHEN v.vocab COLLATE utf8mb4_bin = 'bổ ngữ' AND a.area_id = 1 THEN '19-36/bo_ngu.mp4'
--       WHEN v.vocab COLLATE utf8mb4_bin = 'bờ rào' AND a.area_id = 1 THEN '19-36/bo_rao.mp4'
--       ELSE NULL
--     END AS vocab_area_video,
--     NOW(), 1
--   FROM vocab v
--   CROSS JOIN (SELECT 1 AS area_id UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) a
--   WHERE v.sub_topic_id IS NULL
--     AND v.vocab IN ('bổ dưỡng', 'bổ ích', 'bồ kết', 'bổ ngữ', 'bờ rào')
-- ) t
-- WHERE t.vocab_area_video IS NOT NULL;




-- INSERT INTO transactions (pricing_id, start_date, end_date, code, created_by, created_at)
-- VALUES (
--   3,  -- pricing_id cho gói 6 tháng
--   NOW(),
--   DATE_ADD(NOW(), INTERVAL 180 DAY),
--   'TEST-6M-20240713-17',  -- code giao dịch, bạn có thể đổi cho phù hợp
--   17,  -- id user
--   NOW()
-- );


ALTER TABLE user_answers DROP FOREIGN KEY user_answers_ibfk_1;
ALTER TABLE user_answers DROP FOREIGN KEY user_answers_ibfk_2;
ALTER TABLE option_answers DROP FOREIGN KEY option_answers_ibfk_1;
ALTER TABLE test_question DROP FOREIGN KEY test_question_ibfk_1;
ALTER TABLE test_question DROP FOREIGN KEY test_question_ibfk_2;

-- 2. Drop the unused tables
DROP TABLE IF EXISTS user_answers;
DROP TABLE IF EXISTS option_answers;
DROP TABLE IF EXISTS test_question;

-- 3. Rename notify table to notification
RENAME TABLE notify TO notification;