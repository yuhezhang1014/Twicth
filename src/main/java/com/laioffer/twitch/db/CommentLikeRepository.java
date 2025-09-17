package com.laioffer.twitch.db;

import com.laioffer.twitch.db.entity.CommentLikeEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface CommentLikeRepository extends ListCrudRepository<CommentLikeEntity, Long> {

    @Query("SELECT COUNT(1) FROM comment_likes WHERE comment_id = :commentId")
    long countByCommentId(Long commentId);

    @Query("SELECT EXISTS(SELECT 1 FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId)")
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    @Query("SELECT i.game_id AS gameId, COUNT(1) AS cnt FROM comment_likes cl JOIN comments c ON c.id = cl.comment_id JOIN items i ON i.twitch_id = c.twitch_id WHERE cl.user_id = :userId GROUP BY i.game_id")
    java.util.List<com.laioffer.twitch.recommendation.BehaviorCount> countLikesByGameForUser(Long userId);

    @Modifying
    @Query("DELETE FROM comment_likes WHERE comment_id = :commentId AND user_id = :userId")
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}


