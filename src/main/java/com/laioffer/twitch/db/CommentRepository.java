package com.laioffer.twitch.db;

import com.laioffer.twitch.db.entity.CommentEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface CommentRepository extends ListCrudRepository<CommentEntity, Long> {

    @Query("SELECT * FROM comments WHERE twitch_id = :twitchId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    List<CommentEntity> findByTwitchId(String twitchId, int limit, int offset);

    @Query("SELECT i.game_id AS gameId, COUNT(1) AS cnt FROM comments c JOIN items i ON i.twitch_id = c.twitch_id WHERE c.user_id = :userId GROUP BY i.game_id")
    List<com.laioffer.twitch.recommendation.BehaviorCount> countCommentByGameForUser(Long userId);

    @Modifying
    @Query("DELETE FROM comments WHERE id = :id AND user_id = :userId")
    void deleteByIdAndUserId(Long id, Long userId);
}


