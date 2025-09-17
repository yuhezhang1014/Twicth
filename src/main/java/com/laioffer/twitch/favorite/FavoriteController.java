package com.laioffer.twitch.favorite;

import com.laioffer.twitch.auth.AuthUtils;
import com.laioffer.twitch.db.entity.UserEntity;
import com.laioffer.twitch.model.FavoriteRequestBody;
import com.laioffer.twitch.model.TypeGroupedItemList;
import com.laioffer.twitch.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/favorite")
@ControllerAdvice
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    public FavoriteController(
            FavoriteService favoriteService,
            UserService userService
    ) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    /**
     * 获取收藏过的项目
     */
    @GetMapping
    public TypeGroupedItemList getFavoriteItems(@AuthenticationPrincipal Object principal) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        return favoriteService.getGroupedFavoriteItems(userEntity);
    }

    /**
     * 收藏某个项目
     * RequestBody : 前端以JSON的格式（Body）发送请求给后端，而不是直接写在URL里（不安全，空间小）
     */
    @PostMapping
    public void setFavoriteItem(@AuthenticationPrincipal Object principal, @RequestBody FavoriteRequestBody body) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        try {
            favoriteService.setFavoriteItem(userEntity, body.favorite());
        } catch (DuplicateFavoriteException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate entry for favorite record", e);
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public void unsetFavoriteItem(@AuthenticationPrincipal Object principal, @RequestBody FavoriteRequestBody body) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        favoriteService.unsetFavoriteItem(userEntity, body.favorite().twitchId());
    }
}