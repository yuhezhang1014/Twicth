package com.laioffer.twitch.comment;

import com.laioffer.twitch.db.CommentLikeRepository;
import com.laioffer.twitch.db.CommentRepository;
import com.laioffer.twitch.db.entity.CommentEntity;
import com.laioffer.twitch.db.entity.CommentLikeEntity;
import com.laioffer.twitch.db.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public CommentService(CommentRepository commentRepository, CommentLikeRepository commentLikeRepository) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    @Transactional
    public void postComment(UserEntity user, String twitchId, String content) {
        String sanitized = sanitize(content);
        CommentEntity entity = new CommentEntity(null, twitchId, user.id(), sanitized, Instant.now());
        commentRepository.save(entity);
    }

    public List<CommentEntity> listComments(String twitchId, int page, int size) {
        int pageSize = Math.max(1, Math.min(100, size));
        int upto = Math.min(200, pageSize * (page + 3)); // 抓取更多用于加权排序
        List<CommentEntity> recent = commentRepository.findByTwitchId(twitchId, upto, 0);
        if (recent.isEmpty()) return recent;

        // 点赞数
        Map<Long, Long> likeCounts = recent.stream()
                .collect(Collectors.toMap(CommentEntity::id, c -> commentLikeRepository.countByCommentId(c.id())));

        long maxLikes = likeCounts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        Instant minTime = recent.stream().map(CommentEntity::createdAt).min(Instant::compareTo).orElse(Instant.now());
        Instant maxTime = recent.stream().map(CommentEntity::createdAt).max(Instant::compareTo).orElse(Instant.now());
        long timeRangeSec = Math.max(1, ChronoUnit.SECONDS.between(minTime, maxTime));

        List<CommentEntity> sorted = new ArrayList<>(recent);
        sorted.sort(Comparator.comparingDouble(c -> -weightedScore(c, likeCounts.getOrDefault(c.id(), 0L), maxLikes, minTime, timeRangeSec)));

        int from = Math.max(0, page * pageSize);
        int to = Math.min(sorted.size(), from + pageSize);
        if (from >= to) return List.of();
        return sorted.subList(from, to);
    }

    private double weightedScore(CommentEntity c, long likes, long maxLikes, Instant minTime, long timeRangeSec) {
        double likeNorm = (maxLikes == 0) ? 0.0 : ((double) likes / (double) maxLikes);
        long ageSec = Math.max(0, c.createdAt().getEpochSecond() - minTime.getEpochSecond());
        double timeNorm = (double) ageSec / (double) timeRangeSec;
        return 0.6 * likeNorm + 0.4 * timeNorm;
    }

    @Transactional
    public void deleteOwnComment(UserEntity user, Long commentId) {
        commentRepository.deleteByIdAndUserId(commentId, user.id());
    }

    @Transactional
    public void like(UserEntity user, Long commentId) {
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, user.id())) {
            return;
        }
        commentLikeRepository.save(new CommentLikeEntity(null, commentId, user.id(), Instant.now()));
    }

    @Transactional
    public void unlike(UserEntity user, Long commentId) {
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, user.id());
    }

    private String sanitize(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.length() > 500) {
            trimmed = trimmed.substring(0, 500);
        }
        return trimmed;
    }
}


