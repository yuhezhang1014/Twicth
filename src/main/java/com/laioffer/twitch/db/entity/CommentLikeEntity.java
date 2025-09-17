package com.laioffer.twitch.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("comment_likes")
public record CommentLikeEntity(
        @Id Long id,
        Long commentId,
        Long userId,
        Instant createdAt
) {
}


