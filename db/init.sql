drop table if exists search_keyword;
drop table if exists search_keyword_history;
SET character_set_client = utf8mb4 ;

CREATE TABLE search_keyword (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                keyword VARCHAR(255),
                                count INT,
                                PRIMARY KEY (id),
                                INDEX idx_keyword (keyword)  -- search_keyword 테이블의 keyword에 대한 인덱스 추가
) ENGINE=InnoDB;

CREATE TABLE search_keyword_history (
                                        id BIGINT NOT NULL AUTO_INCREMENT,
                                        keyword VARCHAR(255),
                                        created_date_time DATETIME(6),
                                        PRIMARY KEY (id),
                                        INDEX idx_keyword_history (keyword)  -- search_keyword_history 테이블의 keyword에 대한 인덱스 추가
) ENGINE=InnoDB;