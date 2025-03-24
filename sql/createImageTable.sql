create table images(
    id BIGINT auto_increment PRIMARY KEY,
    path VARCHAR(255) not null,
    article_id BIGINT,
    FOREIGN KEY (article_id) REFERENCES articles(id)
);