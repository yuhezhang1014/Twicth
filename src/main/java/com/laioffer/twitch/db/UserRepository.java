package com.laioffer.twitch.db;

import com.laioffer.twitch.db.entity.UserEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

// 继承 ListCrudRepository<UserEntity, Long>，提供 增删改查 功能
// UserEntity 是操作的实体类。
// Long 是主键（id）的类型。
public interface UserRepository extends ListCrudRepository<UserEntity, Long> {

    List<UserEntity> findByLastName(String lastName); //  SELECT * FROM users WHERE last_name = ?

    List<UserEntity> findByFirstName(String firstName); //  SELECT * FROM users WHERE first_name = ?

    // username是不重复的，所以可以不用list
    UserEntity findByUsername(String username); // SELECT * FROM users WHERE username = ?

    // 改名
    @Modifying // @Modifying：标明这是 修改数据库 的操作（而非查询）
    // 自定义 SQL 语句，避免 Spring 自动推导 SQL
    @Query("UPDATE users SET first_name = :firstName, last_name = :lastName WHERE username = :username")
    void updateNameByUsername(String username, String firstName, String lastName);
}

// 总结：
// 这段代码是 Spring Data JDBC 代码，负责操作 users 表，提供：
// 基础的 CRUD 操作（继承 ListCrudRepository）
// 根据 firstName、lastName 查询用户。
// 根据 username 获取唯一用户。
// 自定义 SQL 语句修改用户的姓名。