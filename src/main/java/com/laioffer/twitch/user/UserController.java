package com.laioffer.twitch.user;

import com.laioffer.twitch.model.RegisterBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController // Spring MVC 的注解，表示这是一个 RESTful API 控制器。
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register") // 提交用户数据（注册、新增用户）
    @ResponseStatus(value = HttpStatus.OK) // 处理成功后，返回 HTTP 200 OK（默认返回）。
    public void register(@RequestBody RegisterBody body) {
        userService.register(body.username(), body.password(), body.firstName(), body.lastName());
    }
}
