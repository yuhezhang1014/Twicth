package com.laioffer.twitch.db;

import com.laioffer.twitch.db.entity.FavoriteRecordEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

// 一般数据库里有几个 table，就会有几个 Repository

public interface FavoriteRecordRepository extends ListCrudRepository<FavoriteRecordEntity, Long> {
    // 同理
    List<FavoriteRecordEntity> findAllByUserId(Long userId);
    // 看看有没有这个数据
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    // 返回的是 List<Long>，没法自动根据函数名决定了。如果是 List<FavoriteRecordEntity> 就可以用父类的了
    // 这个时候函数名就可以乱写了
    @Query("SELECT item_id FROM favorite_records WHERE user_id = :userId")
    List<Long> findFavoriteItemIdsByUserId(Long userId);

    // @Modifying 告诉框架这条 SQL 会改变数据（INSERT、UPDATE、DELETE），而不是简单的查询。必须带上
    @Modifying
    @Query("DELETE FROM favorite_records WHERE user_id = :userId AND item_id = :itemId")
    void delete(Long userId, Long itemId);
}
