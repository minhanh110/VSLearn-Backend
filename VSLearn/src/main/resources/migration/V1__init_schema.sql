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
    (2, 'Cơ thể người', 'approve', NOW(), 1),
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


INSERT INTO transactions (pricing_id, start_date, end_date, code, created_by, created_at) VALUES
    (2, NOW() - INTERVAL 30 DAY, NOW() + INTERVAL 60 DAY, 'TXN_TEST_001', 2, NOW());


INSERT INTO transactions (pricing_id, start_date, end_date, code, created_by, created_at) VALUES
    (1, NOW() - INTERVAL 60 DAY, NOW() - INTERVAL 30 DAY, 'TXN_TEST_002', 3, NOW() - INTERVAL 60 DAY);


-- Vùng miền

INSERT INTO areas (area_name, created_at, created_by) VALUES
                                                          ('Toàn quốc', NOW(), 1),
                                                          ('Bắc', NOW(), 1),
                                                          ('Trung', NOW(), 1),
                                                          ('Nam', NOW(), 1);


INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('A', 1, NOW(), 1), ('Ă', 1, NOW(), 1), ('Â', 1, NOW(), 1), ('B', 1, NOW(), 1), ('C', 1, NOW(), 1), ('D', 1, NOW(), 1), ('Đ', 1, NOW(), 1), ('E', 1, NOW(), 1), ('Ê', 1, NOW(), 1), ('G', 1, NOW(), 1), ('H', 1, NOW(), 1), ('I', 1, NOW(), 1), ('K', 1, NOW(), 1), ('L', 1, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('M', 2, NOW(), 1), ('N', 2, NOW(), 1), ('Ô', 2, NOW(), 1), ('Ơ', 2, NOW(), 1), ('P', 2, NOW(), 1), ('Q', 2, NOW(), 1), ('R', 2, NOW(), 1), ('S', 2, NOW(), 1), ('T', 2, NOW(), 1), ('U', 2, NOW(), 1), ('V', 2, NOW(), 1), ('X', 2, NOW(), 1), ('Y', 2, NOW(), 1), ('Dấu sắc', 2, NOW(), 1), ('Dấu huyền', 2, NOW(), 1), ('Dấu hỏi', 2, NOW(), 1), ('Dấu ngã', 2, NOW(), 1), ('Dấu nặng', 2, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('0', 3, NOW(), 1), ('1', 3, NOW(), 1), ('2', 3, NOW(), 1), ('3', 3, NOW(), 1), ('4', 3, NOW(), 1), ('5', 3, NOW(), 1), ('6', 3, NOW(), 1), ('7', 3, NOW(), 1), ('8', 3, NOW(), 1), ('9', 3, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Bé trai', 4, NOW(), 1), ('Bé gái', 4, NOW(), 1), ('Cao', 4, NOW(), 1), ('Thấp', 4, NOW(), 1), ('Gầy', 4, NOW(), 1), ('Béo', 4, NOW(), 1), ('Khỏe mạnh', 4, NOW(), 1), ('Yếu', 4, NOW(), 1), ('Mệt', 4, NOW(), 1), ('Vui', 4, NOW(), 1), ('Buồn', 4, NOW(), 1), ('No', 4, NOW(), 1), ('Đói', 4, NOW(), 1), ('Tức giận', 4, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Đầu', 5, NOW(), 1), ('Tóc', 5, NOW(), 1), ('Mắt', 5, NOW(), 1), ('Mũi', 5, NOW(), 1), ('Miệng', 5, NOW(), 1), ('Má', 5, NOW(), 1), ('Tai', 5, NOW(), 1), ('Cổ', 5, NOW(), 1), ('Vai', 5, NOW(), 1), ('Bụng', 5, NOW(), 1), ('Tay', 5, NOW(), 1), ('Chân', 5, NOW(), 1);

INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Đi', 6, NOW(), 1), ('Đứng', 6, NOW(), 1), ('Ngồi', 6, NOW(), 1), ('Nằm', 6, NOW(), 1), ('Chạy', 6, NOW(), 1), ('Nhảy', 6, NOW(), 1), ('Ngủ', 6, NOW(), 1), ('Ăn', 6, NOW(), 1), ('Uống', 6, NOW(), 1), ('Đi vệ sinh', 6, NOW(), 1), ('Chải đầu', 6, NOW(), 1), ('Mặc quần áo', 6, NOW(), 1), ('Gội đầu', 6, NOW(), 1), ('Rửa tay', 6, NOW(), 1), ('Rửa chân', 6, NOW(), 1), ('Rửa mặt', 6, NOW(), 1), ('Đánh răng', 6, NOW(), 1);

-- Thêm từ vựng cho "Giao tiếp và từ để hỏi" (sub_topic_id = 7)
INSERT INTO vocab (vocab, sub_topic_id, created_at, created_by) VALUES
                                                                    ('Chào', 7, NOW(), 1), ('Tên là gì', 7, NOW(), 1), ('Bao nhiêu', 7, NOW(), 1), ('Ở đâu', 7, NOW(), 1), ('Thế nào', 7, NOW(), 1), ('Xin lỗi', 7, NOW(), 1), ('Xin phép', 7, NOW(), 1), ('Cảm ơn', 7, NOW(), 1), ('Tôi', 7, NOW(), 1), ('Bạn', 7, NOW(), 1), ('Bao nhiêu tuổi', 7, NOW(), 1);

-- Thêm vocab_area cho các từ vựng mới (vùng miền Toàn quốc)
INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, vocab_area_description, created_at, created_by)
SELECT v.id, 1, CONCAT('Topic_2_Bản thân/Subtopic_4_Giao tiếp và từ để hỏi/', v.vocab, '.mp4'), '', NOW(), 1
FROM vocab v
         LEFT JOIN vocab_area va ON va.vocab_id = v.id AND va.area_id = 1
WHERE va.id IS NULL AND v.sub_topic_id = 7;

-- Cập nhật vocab_area cho tất cả các từ vựng thuộc subtopic "Giao tiếp và từ để hỏi"
UPDATE vocab_area va
    JOIN vocab v ON va.vocab_id = v.id
    SET va.vocab_area_video = CONCAT('Topic_2_Bản thân/Subtopic_4_Giao tiếp và từ để hỏi/', v.vocab, '.mp4')
WHERE va.area_id = 1 AND v.sub_topic_id = 7;


-- SET SQL_SAFE_UPDATES = 0;

-- SET SQL_SAFE_UPDATES = 1;
UPDATE vocab
SET sub_topic_id = CASE
                       WHEN vocab IN ('A','Ă','Â','B','C','D','Đ','E','Ê','G','H','I','K','L') THEN 1
                       WHEN vocab IN ('M','N','Ô','Ơ','P','Q','R','S','T','U','V','X','Y','Dấu sắc','Dấu huyền','Dấu hỏi','Dấu ngã','Dấu nặng') THEN 2
                       WHEN vocab IN ('0','1','2','3','4','5','6','7','8','9') THEN 3
                       WHEN vocab IN ('Bé trai','Bé gái','Cao','Thấp','Gầy','Béo','Khỏe mạnh','Yếu','Mệt','Vui','Buồn','No','Đói','Tức giận') THEN 4
                       WHEN vocab IN ('Đầu','Tóc','Mắt','Mũi','Miệng','Má','Tai','Cổ','Vai','Bụng','Tay','Chân') THEN 5
                       WHEN vocab IN ('Đi','Đứng','Ngồi','Nằm','Chạy','Nhảy','Ngủ','Ăn','Uống','Đi vệ sinh','Chải đầu','Mặc quần áo','Gội đầu','Rửa tay','Rửa chân','Rửa mặt','Đánh răng') THEN 6
                       ELSE sub_topic_id
    END
WHERE vocab IN (
                'A','Ă','Â','B','C','D','Đ','E','Ê','G','H','I','K','L',
                'M','N','Ô','Ơ','P','Q','R','S','T','U','V','X','Y',
                'Dấu sắc','Dấu huyền','Dấu hỏi','Dấu ngã','Dấu nặng',
                '0','1','2','3','4','5','6','7','8','9',
                'Bé trai','Bé gái','Cao','Thấp','Gầy','Béo','Khỏe mạnh','Yếu','Mệt','Vui','Buồn','No','Đói','Tức giận',
                'Đầu','Tóc','Mắt','Mũi','Miệng','Má','Tai','Cổ','Vai','Bụng','Tay','Chân',
                'Đi','Đứng','Ngồi','Nằm','Chạy','Nhảy','Ngủ','Ăn','Uống','Đi vệ sinh','Chải đầu','Mặc quần áo','Gội đầu','Rửa tay','Rửa chân','Rửa mặt','Đánh răng'
    );


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_video, vocab_area_description, created_at, created_by)
SELECT v.id, 1, CONCAT(v.vocab, '.mp4'), '', NOW(), 1
FROM vocab v
         LEFT JOIN vocab_area va ON va.vocab_id = v.id AND va.area_id = 1
WHERE va.id IS NULL AND v.sub_topic_id IS NOT NULL;


UPDATE vocab_area va
    JOIN vocab v ON va.vocab_id = v.id
    SET va.vocab_area_video = CASE
        WHEN v.sub_topic_id = 1 THEN CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_1_Bảng chữ cái 1/', v.vocab, '.mp4')
        WHEN v.sub_topic_id = 2 THEN CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_2_Bảng chữ cái 2/', v.vocab, '.mp4')
        WHEN v.sub_topic_id = 3 THEN CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_3_Số đếm/', v.vocab, '.mp4')
        WHEN v.sub_topic_id = 4 THEN CONCAT('Topic_2_Bản thân/Subtopic_1_Con người và đặc điểm/', v.vocab, '.mp4')
        WHEN v.sub_topic_id = 5 THEN CONCAT('Topic_2_Bản thân/Subtopic_2_Cơ thể người/', v.vocab, '.mp4')
        WHEN v.sub_topic_id = 6 THEN CONCAT('Topic_2_Bản thân/Subtopic_3_Hoạt động hàng ngày/', v.vocab, '.mp4')
        WHEN v.sub_topic_id = 7 THEN CONCAT('Topic_2_Bản thân/Subtopic_4_Giao tiếp và từ để hỏi/', v.vocab, '.mp4')
        ELSE va.vocab_area_video
END
WHERE va.area_id = 1 AND v.sub_topic_id IS NOT NULL;

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
WHERE v.vocab = 'Bao nhiêu tuổi' AND v.sub_topic_id = 7;

-- Xóa chữ "Bạn" khỏi sentence id = 2
DELETE sv FROM sentence_vocab sv
JOIN vocab v ON sv.vocab_id = v.id
WHERE sv.sentence_id = 2 AND v.vocab = 'Bạn' AND v.sub_topic_id = 7;
