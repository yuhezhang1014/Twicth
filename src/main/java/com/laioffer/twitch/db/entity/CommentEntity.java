package com.laioffer.twitch.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("comments")
public record CommentEntity(
        @Id Long id,
        String twitchId,
        Long userId,
        String content,
        Instant createdAt
) {
}


