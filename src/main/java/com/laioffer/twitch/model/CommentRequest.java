package com.laioffer.twitch.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotNull @NotBlank String twitchId,
        @NotNull @NotBlank @Size(max = 500) String content
) {
}


