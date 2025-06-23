
INSERT INTO areas (area_name, created_at, created_by) VALUES
('Toàn quốc', NOW(), 1),
('Bắc', NOW(), 1),
('Trung', NOW(), 1),
('Nam', NOW(), 1);


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_gif, vocab_area_description, created_at, created_by)
SELECT v.id, 1, CONCAT(v.vocab, '.mp4'), '' AS vocab_area_description, NOW(), 1 FROM vocab v WHERE v.vocab IN ('A','Ă','Â','B','C','D','Đ','E','Ê','G','H','I','K','L');


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_gif, vocab_area_description, created_at, created_by)
SELECT v.id, 1, CONCAT(v.vocab, '.mp4'), '' AS vocab_area_description, NOW(), 1 FROM vocab v WHERE v.vocab IN ('M','N','Ô','Ơ','P','Q','R','S','T','U','V','X','Y','Dấu sắc','Dấu huyền','Dấu hỏi','Dấu ngã','Dấu nặng');


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_gif, vocab_area_description, created_at, created_by)
SELECT v.id, 1, CONCAT(v.vocab, '.mp4'), '' AS vocab_area_description, NOW(), 1 FROM vocab v WHERE v.vocab IN ('0','1','2','3','4','5','6','7','8','9');


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_gif, vocab_area_description, created_at, created_by)
SELECT v.id, 1, CONCAT(v.vocab, '.mp4'), '' AS vocab_area_description, NOW(), 1 FROM vocab v WHERE v.vocab IN ('Bé trai','Bé gái','Cao(người)','Thấp(người)','Gầy','Béo','Khỏe mạnh','Yếu','Mệt','Vui','Buồn','No','Đói','Tức giận');


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_gif, vocab_area_description, created_at, created_by)
SELECT v.id, 1, CONCAT(v.vocab, '.mp4'), '' AS vocab_area_description, NOW(), 1 FROM vocab v WHERE v.vocab IN ('Đầu','Tóc','Mắt','Mũi','Miệng','Má','Tai','Cổ','Vai','Bụng','Tay','Chân');


INSERT INTO vocab_area (vocab_id, area_id, vocab_area_gif, vocab_area_description, created_at, created_by)
SELECT v.id, 1, CONCAT(v.vocab, '.mp4'), '' AS vocab_area_description, NOW(), 1 FROM vocab v WHERE v.vocab IN ('Đi','Đứng','Ngồi','Nằm','Chạy','Nhảy','Ngủ','Ăn','Uống','Đi vệ sinh','Chải đầu','Mặc quần áo','Gội đầu','Rửa tay','Rửa chân','Rửa mặt','Đánh răng');

INSERT INTO vocab (vocab, created_at, created_by)
VALUES

('A', NOW(), 1),('Ă', NOW(), 1),('Â', NOW(), 1),('B', NOW(), 1),('C', NOW(), 1),('D', NOW(), 1),('Đ', NOW(), 1),('E', NOW(), 1),('Ê', NOW(), 1),('G', NOW(), 1),('H', NOW(), 1),('I', NOW(), 1),('K', NOW(), 1),('L', NOW(), 1),

('M', NOW(), 1),('N', NOW(), 1),('Ô', NOW(), 1),('Ơ', NOW(), 1),('P', NOW(), 1),('Q', NOW(), 1),('R', NOW(), 1),('S', NOW(), 1),('T', NOW(), 1),('U', NOW(), 1),('V', NOW(), 1),('X', NOW(), 1),('Y', NOW(), 1),('Dấu sắc', NOW(), 1),('Dấu huyền', NOW(), 1),('Dấu hỏi', NOW(), 1),('Dấu ngã', NOW(), 1),('Dấu nặng', NOW(), 1),

('0', NOW(), 1),('1', NOW(), 1),('2', NOW(), 1),('3', NOW(), 1),('4', NOW(), 1),('5', NOW(), 1),('6', NOW(), 1),('7', NOW(), 1),('8', NOW(), 1),('9', NOW(), 1),

('Bé trai', NOW(), 1),('Bé gái', NOW(), 1),('Cao(người)', NOW(), 1),('Thấp(người)', NOW(), 1),('Gầy', NOW(), 1),('Béo', NOW(), 1),('Khỏe mạnh', NOW(), 1),('Yếu', NOW(), 1),('Mệt', NOW(), 1),('Vui', NOW(), 1),('Buồn', NOW(), 1),('No', NOW(), 1),('Đói', NOW(), 1),('Tức giận', NOW(), 1),

('Đầu', NOW(), 1),('Tóc', NOW(), 1),('Mắt', NOW(), 1),('Mũi', NOW(), 1),('Miệng', NOW(), 1),('Má', NOW(), 1),('Tai', NOW(), 1),('Cổ', NOW(), 1),('Vai', NOW(), 1),('Bụng', NOW(), 1),('Tay', NOW(), 1),('Chân', NOW(), 1),

('Đi', NOW(), 1),('Đứng', NOW(), 1),('Ngồi', NOW(), 1),('Nằm', NOW(), 1),('Chạy', NOW(), 1),('Nhảy', NOW(), 1),('Ngủ', NOW(), 1),('Ăn', NOW(), 1),('Uống', NOW(), 1),('Đi vệ sinh', NOW(), 1),('Chải đầu', NOW(), 1),('Mặc quần áo', NOW(), 1),('Gội đầu', NOW(), 1),('Rửa tay', NOW(), 1),('Rửa chân', NOW(), 1),('Rửa mặt', NOW(), 1),('Đánh răng', NOW(), 1); 


-- Bảng chữ cái 1
UPDATE vocab SET sub_topic_id = 1 WHERE vocab IN ('A','Ă','Â','B','C','D','Đ','E','Ê','G','H','I','K','L');

-- Bảng chữ cái 2
UPDATE vocab SET sub_topic_id = 2 WHERE vocab IN ('M','N','Ô','Ơ','P','Q','R','S','T','U','V','X','Y','Dấu sắc','Dấu huyền','Dấu hỏi','Dấu ngã','Dấu nặng');

-- Số đếm
UPDATE vocab SET sub_topic_id = 3 WHERE vocab IN ('0','1','2','3','4','5','6','7','8','9');

-- Con người và đặc điểm
UPDATE vocab SET sub_topic_id = 4 WHERE vocab IN ('Bé trai','Bé gái','Cao(người)','Thấp(người)','Gầy','Béo','Khỏe mạnh','Yếu','Mệt','Vui','Buồn','No','Đói','Tức giận');

-- Cơ thể người
UPDATE vocab SET sub_topic_id = 5 WHERE vocab IN ('Đầu','Tóc','Mắt','Mũi','Miệng','Má','Tai','Cổ','Vai','Bụng','Tay','Chân');

-- Hoạt động hàng ngày
UPDATE vocab SET sub_topic_id = 6 WHERE vocab IN ('Đi','Đứng','Ngồi','Nằm','Chạy','Nhảy','Ngủ','Ăn','Uống','Đi vệ sinh','Chải đầu','Mặc quần áo','Gội đầu','Rửa tay','Rửa chân','Rửa mặt','Đánh răng');


  
UPDATE vocab_area va
JOIN vocab v ON va.vocab_id = v.id
SET va.vocab_area_gif = CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_1_Bảng chữ cái 1/', v.vocab, '.mp4')
WHERE va.area_id = 1 AND v.sub_topic_id = 1;

UPDATE vocab_area va
JOIN vocab v ON va.vocab_id = v.id
SET va.vocab_area_gif = CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_2_Bảng chữ cái 2/', v.vocab, '.mp4')
WHERE va.area_id = 1 AND v.sub_topic_id = 2;

UPDATE vocab_area va
JOIN vocab v ON va.vocab_id = v.id
SET va.vocab_area_gif = CONCAT('Topic_1_Bảng chữ cái và số đếm/Subtopic_3_Số đếm/', v.vocab, '.mp4')
WHERE va.area_id = 1 AND v.sub_topic_id = 3;

UPDATE vocab_area va
JOIN vocab v ON va.vocab_id = v.id
SET va.vocab_area_gif = CONCAT('Topic_2_Bản thân/Subtopic_1_Con người và đặc điểm/', v.vocab, '.mp4')
WHERE va.area_id = 1 AND v.sub_topic_id = 4;

UPDATE vocab_area va
JOIN vocab v ON va.vocab_id = v.id
SET va.vocab_area_gif = CONCAT('Topic_2_Bản thân/Subtopic_2_Cơ thể người/', v.vocab, '.mp4')
WHERE va.area_id = 1 AND v.sub_topic_id = 5;

UPDATE vocab_area va
JOIN vocab v ON va.vocab_id = v.id
SET va.vocab_area_gif = CONCAT('Topic_2_Bản thân/Subtopic_3_Hoạt động hàng ngày/', v.vocab, '.mp4')
WHERE va.area_id = 1 AND v.sub_topic_id = 6;