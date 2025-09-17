package com.laioffer.twitch.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("item_views")
public record ItemViewEntity(
        @Id Long id,
        Long userId,
        String twitchId,
        Instant createdAt
) {}


