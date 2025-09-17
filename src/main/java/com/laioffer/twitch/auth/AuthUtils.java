package com.laioffer.twitch.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class AuthUtils {
    
    public static String getUsername(Object principal) {
        if (principal instanceof User user) {
            return user.getUsername();
        } else if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getName();
        }
        throw new IllegalArgumentException("Unsupported principal type: " + principal.getClass());
    }
}
