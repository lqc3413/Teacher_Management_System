ALTER TABLE sys_paper_record
  ADD COLUMN paper_type VARCHAR(50) COMMENT '论文类型' AFTER submission_id,
  ADD COLUMN author_type VARCHAR(20) COMMENT '作者类型' AFTER paper_name,
  ADD COLUMN other_authors VARCHAR(500) COMMENT '其他作者' AFTER author_type,
  CHANGE COLUMN publish_type index_category VARCHAR(50) COMMENT '收录类别',
  DROP COLUMN `rank`,
  DROP COLUMN `is_first_author`;
