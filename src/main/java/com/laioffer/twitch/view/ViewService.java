package com.laioffer.twitch.view;

import com.laioffer.twitch.db.ItemViewRepository;
import com.laioffer.twitch.db.entity.ItemViewEntity;
import com.laioffer.twitch.db.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ViewService {

    private final ItemViewRepository itemViewRepository;

    public ViewService(ItemViewRepository itemViewRepository) {
        this.itemViewRepository = itemViewRepository;
    }

    @Transactional
    public void recordView(UserEntity user, String twitchId) {
        // 检查是否已存在浏览记录（避免重复记录）
        boolean exists = itemViewRepository.existsByUserIdAndTwitchId(user.id(), twitchId);
        if (!exists) {
            ItemViewEntity view = new ItemViewEntity(null, user.id(), twitchId, Instant.now());
            itemViewRepository.save(view);
        }
    }

    public List<ItemViewEntity> getUserViews(Long userId, int page, int size) {
        int limit = Math.max(1, Math.min(100, size));
        int offset = Math.max(0, page) * limit;
        return itemViewRepository.findByUserIdOrderByCreatedAtDesc(userId, limit, offset);
    }
}
