package com.laioffer.twitch.recommendation;

import com.laioffer.twitch.auth.AuthUtils;
import com.laioffer.twitch.db.entity.UserEntity;
import com.laioffer.twitch.model.TypeGroupedItemList;
import com.laioffer.twitch.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    public RecommendationController(
            RecommendationService recommendationService,
            UserService userService
    ) {
        this.recommendationService = recommendationService;
        this.userService = userService;
    }

    @GetMapping("/recommendation")
    public TypeGroupedItemList getRecommendation(@AuthenticationPrincipal Object principal) {
        UserEntity userEntity = null;
        if (principal != null) {
            String username = AuthUtils.getUsername(principal);
            userEntity = userService.findByUsername(username);
        }
        return recommendationService.recommendItems(userEntity);
    }
}
