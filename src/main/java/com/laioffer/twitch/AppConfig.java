package com.laioffer.twitch;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.sql.DataSource;

@Configuration // 提供配置的，其实改成service也可以，但一般不会这样
public class AppConfig {
    // 重听的新版本，主要是为了消除红色划线（只是警告那些API可能在后面更新的版本不能用，实际上影响不大）
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF（跨站请求伪造）保护，一般在非浏览器客户端的应用中禁用
                .authorizeHttpRequests(auth -> // 配置请求的授权规则
                        auth
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 静态资源（如 JS、CSS）允许公开访问
                                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/*.json", "/*.png", "/static/**").permitAll() // 允许 GET 请求访问的路径
                                .requestMatchers(HttpMethod.POST, "/login", "/register", "/logout").permitAll() // 允许 POST 请求访问的路径（登录、注册、注销）
                                .requestMatchers(HttpMethod.GET, "/recommendation", "/game", "/search").permitAll() // 公开的推荐、游戏、搜索页面
                                .anyRequest().authenticated() // 其它请求需要认证（即需要登录）
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // 如果未认证的用户访问受保护资源，返回 401 Unauthorized
                ) // 配置异常处理
                .formLogin(form -> form // formLogin实际上实现的就是Session-based验证ID。下面都是在定义不同情况下的行为，防止它自己重定向，打开其他页面
                        .successHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value())) // 如果成功，返回 HTTP 204 No Content
                        .failureHandler(new SimpleUrlAuthenticationFailureHandler()) // 如果失败，使用默认的失败处理器
                ) // 配置表单登录
                .logout(logout -> logout
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)) // 如果成功，返回 HTTP 204 No Content
                ); // 配置登出
        return http.build(); // 返回构建好的安全过滤链
    }

    /**
     * UserDetailsManager：是 Spring Security 的接口，用于管理用户数据（如用户名、密码、权限等）
     * @param dataSource：数据库连接的数据源，Spring 会自动注入这个 Bean，用于执行数据库操作。
     * @return JdbcUserDetailsManager：Spring Security 提供的一个默认实现，它使用 JDBC 访问数据库存储的用户信息
     */
    @Bean
    UserDetailsManager users(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    /**
     * PasswordEncoder：密码加密器接口，负责 加密和验证密码。
     * @return PasswordEncoderFactories.createDelegatingPasswordEncoder()：创建一个支持多种编码方式的密码加密器（默认使用 bcrypt）。
     */
    @Bean
    PasswordEncoder passwordEncoder() { // 用于给密码加密，比较简单的密码会被GPU快速破解（暴力解）
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}