package com.laioffer.twitch.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

// 这个类是 Java Record（不可变数据结构），比普通 class 更简洁，适合数据存储。
@Table("users") // 表明这个实体对应数据库中的 users 表
public record UserEntity(
        @Id Long id, // @Id：标识 id 是主键
        String username,
        String firstName,
        String lastName,
        String password
) {
}
