package com.laioffer.twitch.auth;

import com.laioffer.twitch.db.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GithubOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public GithubOAuth2UserService(UserDetailsManager userDetailsManager,
                                   PasswordEncoder passwordEncoder,
                                   UserRepository userRepository) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        Map<String, Object> attrs = oAuth2User.getAttributes();
        // github id 必定存在
        String githubId = String.valueOf(attrs.get("id"));
        String login = (String) attrs.getOrDefault("login", "");
        String name = (String) attrs.getOrDefault("name", login);
        String username = "github_" + githubId;

        if (!userDetailsManager.userExists(username)) {
            UserDetails newUser = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode("oauth2-login-placeholder"))
                    .roles("USER")
                    .build();
            userDetailsManager.createUser(newUser);
            // 尝试把 name 写到 first_name，last_name 置空
            userRepository.updateNameByUsername(username, name, "");
        }

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attrs,
                "login" // 任意现有属性作为 nameAttributeKey
        ) {
            @Override
            public String getName() {
                // 返回本地系统用户名，确保 Principal.getName() 可用于后续查询
                return username;
            }
        };
    }
}


