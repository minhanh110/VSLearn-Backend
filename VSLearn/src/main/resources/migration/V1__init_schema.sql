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


CREATE TABLE test_question (
                               id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               topic_id INT UNSIGNED NOT NULL,
                               question_content VARCHAR(255) NULL,
                               question_answer INT UNSIGNED NOT NULL,
                               question_type VARCHAR(255) NOT NULL COMMENT 'MC/TF/Fill',
                               created_at DATETIME NOT NULL,
                               created_by INT UNSIGNED NOT NULL,
                               updated_at DATETIME,
                               updated_by INT UNSIGNED,
                               deleted_at DATETIME,
                               deleted_by INT UNSIGNED,
                               FOREIGN KEY (topic_id) REFERENCES topic(id) ON DELETE CASCADE,
                               FOREIGN KEY (question_answer) REFERENCES vocab(id) ON DELETE CASCADE
);


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





CREATE TABLE option_answers (
                                id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                test_question_id INT UNSIGNED NOT NULL,
    -- users_id INT UNSIGNED NOT NULL,
    -- answer_vocab_id INT UNSIGNED NOT NULL,
                                type_content VARCHAR(255),
                                is_correct TINYINT(1) NOT NULL,
    -- users_answers_point DOUBLE NOT NULL,
                                created_at DATETIME NOT NULL,
                                created_by INT UNSIGNED NOT NULL,
                                updated_at DATETIME,
                                updated_by INT UNSIGNED,
                                deleted_at DATETIME,
                                deleted_by INT UNSIGNED,
                                FOREIGN KEY (test_question_id) REFERENCES test_question(id) ON DELETE CASCADE
);

CREATE TABLE user_answers (
                              id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              users_id INT UNSIGNED NOT NULL,
                              option_answers_id INT UNSIGNED NOT NULL,
                              created_at DATETIME NOT NULL,
                              created_by INT UNSIGNED NOT NULL,
                              updated_at DATETIME,
                              updated_by INT UNSIGNED,
                              deleted_at DATETIME,
                              deleted_by INT UNSIGNED,
                              FOREIGN KEY (users_id) REFERENCES users(id) ON DELETE CASCADE,
                              FOREIGN KEY (option_answers_id) REFERENCES option_answers(id) ON DELETE CASCADE
);



CREATE TABLE vocab_area (
                            id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                            vocab_id INT UNSIGNED NOT NULL,
                            area_id INT UNSIGNED NOT NULL,
                            vocab_area_gif VARCHAR(255) NOT NULL,
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
                          sentence_gif VARCHAR(255),
                          sentence_description TEXT,
                          sentence_type VARCHAR(255),
                          sentence_sub_topic_id INT UNSIGNED NOT NULL,
                          created_at DATETIME,
                          created_by INT UNSIGNED,
                          updated_at DATETIME,
                          updated_by INT UNSIGNED,
                          deleted_at DATETIME,
                          deleted_by INT UNSIGNED,
                          FOREIGN KEY (sentence_sub_topic_id) REFERENCES sub_topic(id) ON DELETE CASCADE
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
    (2, 'Cơ thể người', 'approve', NOW(), 1),
    (2, 'Hoạt động hàng ngày', 'approve', NOW(), 1);
    

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
    
    
    
    

INSERT INTO pricing (pricing_type, price, duration_days, discount_percent, description, created_at, created_by) VALUES
('1_MONTH', 100000, 30, 0.0, 'Gói học 1 tháng', NOW(), 1),
('3_MONTHS', 250000, 90, 0.1, 'Gói học 3 tháng', NOW(), 1),
('6_MONTHS', 500000, 180, 0.2, 'Gói học 6 tháng', NOW(), 1);


INSERT INTO transactions (pricing_id, start_date, end_date, code, created_by, created_at) VALUES
(2, NOW() - INTERVAL 30 DAY, NOW() + INTERVAL 60 DAY, 'TXN_TEST_001', 2, NOW());


INSERT INTO transactions (pricing_id, start_date, end_date, code, created_by, created_at) VALUES
(1, NOW() - INTERVAL 60 DAY, NOW() - INTERVAL 30 DAY, 'TXN_TEST_002', 3, NOW() - INTERVAL 60 DAY); 

