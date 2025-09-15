package com.laioffer.twitch;

import com.laioffer.twitch.db.FavoriteRecordRepository;
import com.laioffer.twitch.db.ItemRepository;
import com.laioffer.twitch.db.entity.ItemEntity;
import com.laioffer.twitch.db.entity.UserEntity;
import com.laioffer.twitch.favorite.FavoriteService;
import com.laioffer.twitch.model.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTests {

    @Mock private ItemRepository itemRepository;
    @Mock private FavoriteRecordRepository favoriteRecordRepository;

    private FavoriteService favoriteService;

    @BeforeEach
    public void setup() {
        favoriteService = new FavoriteService(itemRepository, favoriteRecordRepository);
    }

    /**
     * 收藏时，item 不存在 → 保存到 item 数据表
     */
    @Test
    public void whenItemNotExist_setFavoriteItem_shouldSaveItem() {
        UserEntity user = new UserEntity(1L, "user", "foo", "bar", "123456");
        ItemEntity item = new ItemEntity(null, "twitchId", "title", "url", "thumb", "broadcaster", "gameid", ItemType.VIDEO);
        ItemEntity persisted = new ItemEntity(1L, "twitchId", "title", "url", "thumb", "broadcaster", "gameid", ItemType.VIDEO);
        Mockito.when(itemRepository.findByTwitchId("twitchId")).thenReturn(null);
        Mockito.when(itemRepository.save(item)).thenReturn(persisted); // 打桩：假设以后有人调用 save(item)，就返回 persisted

        favoriteService.setFavoriteItem(user, item);

        // 验证 itemRepository.save(item) 这个调用是否发生过，也就是验证上面的 favoriteService.setFavoriteItem 是否调用了 itemRepository.save(item)
        Mockito.verify(itemRepository).save(item);
    }

    /**
     * 收藏时，item 存在 → 不保存
     */
    @Test
    public void whenItemExist_setFavoriteItem_shouldNotSaveItem() {
        UserEntity user = new UserEntity(1L, "user", "foo", "bar", "123456");
        ItemEntity item = new ItemEntity(null, "twitchId", "title", "url", "thumb", "broadcaster", "gameid", ItemType.VIDEO);
        ItemEntity persisted = new ItemEntity(1L, "twitchId", "title", "url", "thumb", "broadcaster", "gameid", ItemType.VIDEO);
        Mockito.when(itemRepository.findByTwitchId("twitchId")).thenReturn(persisted);

        favoriteService.setFavoriteItem(user, item);

        Mockito.verify(itemRepository, Mockito.never()).save(item);
    }

    /**
     * 取消收藏时，item 不存在 → 不删除记录
     */
    @Test
    public void whenItemNotExist_unsetFavoriteItem_shouldNotDeleteFavoriteRecord() {
        UserEntity user = new UserEntity(1L, "user", "foo", "bar", "123456");
        Mockito.when(itemRepository.findByTwitchId("twitchId")).thenReturn(null);

        favoriteService.unsetFavoriteItem(user, "twitchId");

        Mockito.verifyNoInteractions(favoriteRecordRepository);
    }

    /**
     * 取消收藏时，item 存在 → 删除记录
     */
    @Test
    public void whenItemExist_unsetFavoriteItem_shouldDeleteFavoriteRecord() {
        UserEntity user = new UserEntity(1L, "user", "foo", "bar", "123456");
        ItemEntity persisted = new ItemEntity(1L, "twitchId", "title", "url", "thumb", "broadcaster", "gameid", ItemType.VIDEO);
        Mockito.when(itemRepository.findByTwitchId("twitchId")).thenReturn(persisted);

        favoriteService.unsetFavoriteItem(user, "twitchId");

        Mockito.verify(favoriteRecordRepository).delete(1L, 1L);
    }
}
