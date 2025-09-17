DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS comment_likes;
DROP TABLE IF EXISTS item_views;
DROP TABLE IF EXISTS favorite_records; -- 如果存在就清除掉，每次运行都把数据清空
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

CREATE TABLE users -- 用户的table
(
    id INT PRIMARY KEY AUTO_INCREMENT, -- 用户id，类似于学号，是自动增加的，primary key是指数据库主件，唯一的标识，不能重复，不能修改
    username VARCHAR(50) NOT NULL UNIQUE, -- varchar类似string，50是最大长度
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password VARCHAR(100) NOT NULL,
    enabled  TINYINT      NOT NULL DEFAULT 1 -- 为了满足Spring Boot，指用户有没有被激活，默认已经被激活
);

CREATE TABLE authorities -- 也是为了满足Spring Boot，设置权限的
(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username  VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE items -- item entity就是对应这个的
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    twitch_id VARCHAR(255) UNIQUE NOT NULL,
    title TEXT,
    url VARCHAR(255),
    thumbnail_url VARCHAR(255),
    broadcaster_name VARCHAR(255),
    game_id VARCHAR(255),
    type VARCHAR(255) -- video, stream, clip
);

CREATE TABLE favorite_records -- 记录用户的收藏、点赞信息
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 时间戳，默认值是当前时间
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    -- FOREIGN KEY： 强制声明了关系，起保护作用，在不成立的时候会报错（如用户/item不存在）。
    -- ON DELETE CASCADE（级联）：如用户/item被删掉，favorite_records也会被删掉，保持数据的干净
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    UNIQUE KEY unique_item_and_user_combo (item_id, user_id)
    -- UNIQUE KEY：item_id和user_id的组合是unique的，不能重复存储，就是点赞过后不能再点赞了
);

CREATE TABLE comments -- 评论表
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    twitch_id VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_comments_twitch_id_created_at (twitch_id, created_at DESC)
);

CREATE TABLE comment_likes -- 评论点赞表
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    comment_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_comment_user (comment_id, user_id),
    INDEX idx_comment_likes_comment_id (comment_id)
);

CREATE TABLE item_views -- 浏览记录
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    twitch_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_item_views_user_twitch_time (user_id, twitch_id, created_at)
);
