package com.laioffer.twitch.recommendation;

import com.laioffer.twitch.db.CommentLikeRepository;
import com.laioffer.twitch.db.CommentRepository;
import com.laioffer.twitch.db.ItemRepository;
import com.laioffer.twitch.db.ItemViewRepository;
import com.laioffer.twitch.db.entity.ItemEntity;
import com.laioffer.twitch.db.entity.UserEntity;
import com.laioffer.twitch.external.TwitchService;
import com.laioffer.twitch.external.model.Clip;
import com.laioffer.twitch.external.model.Stream;
import com.laioffer.twitch.external.model.Video;
import com.laioffer.twitch.favorite.FavoriteService;
import com.laioffer.twitch.model.TypeGroupedItemList;
import com.laioffer.twitch.view.ViewService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    private static final int MAX_GAME_SEED = 3; // 所有点赞过的游戏里取3个
    private static final int PER_PAGE_ITEM_SIZE = 20; // 每个种类最多推荐20个

    private final TwitchService twitchService;
    private final FavoriteService favoriteService;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ItemViewRepository itemViewRepository;
    private final ViewService viewService;

    public RecommendationService(TwitchService twitchService,
                                 FavoriteService favoriteService,
                                 ItemRepository itemRepository,
                                 CommentRepository commentRepository,
                                 CommentLikeRepository commentLikeRepository,
                                 ItemViewRepository itemViewRepository,
                                 ViewService viewService) {
        this.twitchService = twitchService;
        this.favoriteService = favoriteService;
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.itemViewRepository = itemViewRepository;
        this.viewService = viewService;
    }

    @Cacheable("recommend_items")
    public TypeGroupedItemList recommendItems(UserEntity userEntity) {
        List<String> gameIds;
        Set<String> exclusions = new HashSet<>();
        if (userEntity == null) {
            gameIds  = twitchService.getTopGameIds();
        } else {
            // 1) 计算用户对各个游戏的加权分数
            Map<String, Double> gameScore = new HashMap<>();

            // 收藏 1.0
            for (BehaviorCount bc : itemRepository.countFavoritesByGameForUser(userEntity.id())) {
                gameScore.merge(bc.gameId(), 1.0 * bc.cnt(), Double::sum);
            }
            // 评论 0.8
            for (BehaviorCount bc : commentRepository.countCommentByGameForUser(userEntity.id())) {
                gameScore.merge(bc.gameId(), 0.8 * bc.cnt(), Double::sum);
            }
            // 点赞 0.6
            for (BehaviorCount bc : commentLikeRepository.countLikesByGameForUser(userEntity.id())) {
                gameScore.merge(bc.gameId(), 0.6 * bc.cnt(), Double::sum);
            }
            // 浏览 0.3
            for (BehaviorCount bc : itemViewRepository.countViewsByGameForUser(userEntity.id())) {
                gameScore.merge(bc.gameId(), 0.3 * bc.cnt(), Double::sum);
            }

            if (gameScore.isEmpty()) {
                gameIds = twitchService.getTopGameIds();
            } else {
                // 2) 取分数最高的若干游戏作为种子
                gameIds = gameScore.entrySet().stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .limit(MAX_GAME_SEED)
                        .map(Map.Entry::getKey)
                        .toList();
            }

            // 3) 排除已收藏内容
            for (ItemEntity item : favoriteService.getFavoriteItems(userEntity)) {
                exclusions.add(item.twitchId());
            }
        }

        int gameSize = Math.min(gameIds.size(), MAX_GAME_SEED);
        int perGameListSize = Math.max(1, PER_PAGE_ITEM_SIZE / Math.max(1, gameSize));

        List<ItemEntity> streams = recommendStreams(gameIds, exclusions);
        List<ItemEntity> clips = recommendClips(gameIds.subList(0, gameSize), perGameListSize, exclusions);
        List<ItemEntity> videos = recommendVideos(gameIds.subList(0, gameSize), perGameListSize, exclusions);

        // 自动记录用户浏览推荐内容（仅对已登录用户）
        if (userEntity != null) {
            recordViewsForUser(userEntity, streams, clips, videos);
        }

        return new TypeGroupedItemList(streams, videos, clips);
    }

    private List<ItemEntity> recommendStreams(List<String> gameIds, Set<String> exclusions) {
        List<Stream> streams = twitchService.getStreams(gameIds, PER_PAGE_ITEM_SIZE); // 这里不分配每个游戏，因为twitch这个api自带这个功能，接受多个查询参数，不一定是均匀分布
        List<ItemEntity> resultItems = new ArrayList<>();
        for (Stream stream: streams) {
            if (!exclusions.contains(stream.id())) { // 确保用户没有收藏过
                resultItems.add(new ItemEntity(stream));
            }
        }
        return resultItems;
    }

    private List<ItemEntity> recommendVideos(List<String> gameIds, int perGameListSize, Set<String> exclusions) {
        List<ItemEntity> resultItems = new ArrayList<>();
        for (String gameId : gameIds) {
            List<Video> listPerGame = twitchService.getVideos(gameId, perGameListSize);
            for (Video video : listPerGame) {
                if (!exclusions.contains(video.id())) {
                    resultItems.add(new ItemEntity(gameId, video));
                }
            }
        }
        return resultItems;
    }

    private List<ItemEntity> recommendClips(List<String> gameIds, int perGameListSize, Set<String> exclusions) {
        List<ItemEntity> resultItem = new ArrayList<>();
        for (String gameId : gameIds) {
            List<Clip> listPerGame = twitchService.getClips(gameId, perGameListSize);
            for (Clip clip : listPerGame) {
                if (!exclusions.contains(clip.id())) {
                    resultItem.add(new ItemEntity(clip));
                }
            }
        }
        return resultItem;
    }

    private void recordViewsForUser(UserEntity user, List<ItemEntity> streams, List<ItemEntity> clips, List<ItemEntity> videos) {
        // 记录用户浏览推荐内容，用于后续推荐算法
        for (ItemEntity item : streams) {
            viewService.recordView(user, item.twitchId());
        }
        for (ItemEntity item : clips) {
            viewService.recordView(user, item.twitchId());
        }
        for (ItemEntity item : videos) {
            viewService.recordView(user, item.twitchId());
        }
    }
}
