package com.laioffer.twitch.external;

import com.laioffer.twitch.external.model.Game;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 获取指定游戏或热门游戏
@RestController
public class GameController {

    private final TwitchService twitchService;

    public GameController(TwitchService twitchService) {
        this.twitchService = twitchService;
    }

    @GetMapping("/game")
    public List<Game> getGames(@RequestParam(value = "game_name", required = false) String gameName) {
        if (gameName == null) { // 没有指定，就返回最流行的
            return twitchService.getTopGames();
        } else {
            return twitchService.getGames(gameName);
        }
    }
}
