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

    /**
     * 根据游戏名称，获取游戏，如未指定，则返回热门游戏
     * 这里指定了游戏名的场景是给search item做辅助的，先通过游戏名获取游戏id，前端再把这个id带上给search item的接口，相当于前端会先后调用这两个接口
     */
    @GetMapping("/game")
    public List<Game> getGames(@RequestParam(value = "game_name", required = false) String gameName) {
        if (gameName == null) { // 没有指定，就返回最流行的
            return twitchService.getTopGames();
        } else {
            return twitchService.getGames(gameName);
        }
    }
}
