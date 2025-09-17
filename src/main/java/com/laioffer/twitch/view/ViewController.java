package com.laioffer.twitch.view;

import com.laioffer.twitch.auth.AuthUtils;
import com.laioffer.twitch.db.entity.ItemViewEntity;
import com.laioffer.twitch.db.entity.UserEntity;
import com.laioffer.twitch.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/views")
public class ViewController {

    private final ViewService viewService;
    private final UserService userService;

    public ViewController(ViewService viewService, UserService userService) {
        this.viewService = viewService;
        this.userService = userService;
    }

    @PostMapping("/{twitchId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void recordView(@AuthenticationPrincipal Object principal, @PathVariable("twitchId") String twitchId) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        viewService.recordView(userEntity, twitchId);
    }

    @GetMapping
    public List<ItemViewEntity> getUserViews(@AuthenticationPrincipal Object principal,
                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "20") int size) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        return viewService.getUserViews(userEntity.id(), page, size);
    }
}
