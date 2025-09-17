package com.laioffer.twitch.comment;

import com.laioffer.twitch.auth.AuthUtils;
import com.laioffer.twitch.db.entity.CommentEntity;
import com.laioffer.twitch.db.entity.UserEntity;
import com.laioffer.twitch.model.CommentRequest;
import com.laioffer.twitch.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void post(@AuthenticationPrincipal Object principal,
                     @RequestBody @Valid CommentRequest request) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        commentService.postComment(userEntity, request.twitchId(), request.content());
    }

    @GetMapping
    public List<CommentEntity> list(@RequestParam("twitch_id") String twitchId,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "20") int size) {
        return commentService.listComments(twitchId, page, size);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable("id") Long id) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        commentService.deleteOwnComment(userEntity, id);
    }

    @PostMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@AuthenticationPrincipal Object principal, @PathVariable("id") Long id) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        commentService.like(userEntity, id);
    }

    @DeleteMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@AuthenticationPrincipal Object principal, @PathVariable("id") Long id) {
        String username = AuthUtils.getUsername(principal);
        UserEntity userEntity = userService.findByUsername(username);
        commentService.unlike(userEntity, id);
    }
}


