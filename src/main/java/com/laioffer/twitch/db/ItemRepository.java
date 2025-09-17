package com.laioffer.twitch.db;

import com.laioffer.twitch.db.entity.ItemEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface ItemRepository extends ListCrudRepository<ItemEntity, Long> {
    // 从数据库上要数据
    // 实际上执行的是：SELECT * FROM items WHERE twitch_id = twitchId
    // 根据函数名的find知道执行的是select语句，twitch_id也是函数名里的ByTwitchId
    // 查官方文档，找这些对应关系
    ItemEntity findByTwitchId(String twitchId);
    @org.springframework.data.jdbc.repository.query.Query("SELECT game_id AS gameId, COUNT(1) AS cnt FROM items i JOIN favorite_records f ON f.item_id = i.id WHERE f.user_id = :userId GROUP BY game_id")
    java.util.List<com.laioffer.twitch.recommendation.BehaviorCount> countFavoritesByGameForUser(Long userId);
}
