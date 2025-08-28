package com.laioffer.twitch.user;

import com.laioffer.twitch.db.UserRepository;
import com.laioffer.twitch.db.entity.UserEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    // 三个dependencies
    private final UserDetailsManager userDetailsManager; // Spring Security 提供的 用户管理接口，用于管理用户的身份信息（如创建、删除用户）
    private final PasswordEncoder passwordEncoder; // 负责 密码加密，防止明文存储密码，提高安全性。
    private final UserRepository userRepository;

    public UserService(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * 注册
     * 事务：有多个写操作，中间出错还可以撤回
     */
    @Transactional
    public void register(String username, String password, String firstName, String lastName) {
        UserDetails user = User.builder() // 它自己的api，builder pattern。为什么不直接new？因为看不出来顺序对不对（比如有30个string的参数），可读性差，维护性差
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles("USER") // authentication table
                .build();
        userDetailsManager.createUser(user); // 创建user，通常存入 users 表
        userRepository.updateNameByUsername(username, firstName, lastName); // 让Spring Boot更新一下
    }

    /**
     * 辅助方法：通过用户名查找用户 entity
     */
    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}