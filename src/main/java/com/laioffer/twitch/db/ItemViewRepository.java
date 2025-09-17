package com.laioffer.twitch.db;

import com.laioffer.twitch.db.entity.ItemViewEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ItemViewRepository extends ListCrudRepository<ItemViewEntity, Long> {

    @Query("SELECT i.game_id AS gameId, COUNT(1) AS cnt FROM item_views v JOIN items i ON i.twitch_id = v.twitch_id WHERE v.user_id = :userId GROUP BY i.game_id")
    List<com.laioffer.twitch.recommendation.BehaviorCount> countViewsByGameForUser(Long userId);

    @Query("SELECT * FROM item_views WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<ItemViewEntity> findByUserIdOrderByCreatedAtDesc(Long userId, int limit, int offset);

    @Query("SELECT EXISTS(SELECT 1 FROM item_views WHERE user_id = :userId AND twitch_id = :twitchId)")
    boolean existsByUserIdAndTwitchId(Long userId, String twitchId);
}


