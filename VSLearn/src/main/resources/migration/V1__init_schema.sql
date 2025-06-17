DROP DATABASE IF EXISTS VSLearn;
CREATE DATABASE VSLearn;
USE VSLearn;

CREATE TABLE users (
                       id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       user_name VARCHAR(255) NOT NULL,
                       user_password VARCHAR(255) NOT NULL,
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
                             create_by INT UNSIGNED NOT NULL,
                             total_point DOUBLE NOT NULL,
                             created_at DATETIME NOT NULL,
                             updated_at DATETIME,
                             updated_by INT UNSIGNED,
                             FOREIGN KEY (topic_id) REFERENCES topic(id) ON DELETE CASCADE,
                             FOREIGN KEY (create_by) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE transactions (
                              id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              pricing_id INT UNSIGNED NOT NULL,
                              start_date DATETIME NOT NULL,
                              end_date DATETIME NOT NULL,
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


CREATE TABLE user_feedback (
                               id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               feedback_content TEXT,
                               rating INT UNSIGNED NOT NULL,
                               created_by INT UNSIGNED NOT NULL,
                               created_at DATETIME NOT NULL,
                               FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
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