package com.laioffer.twitch.model;

import com.laioffer.twitch.db.entity.ItemEntity;
import com.laioffer.twitch.external.model.Clip;
import com.laioffer.twitch.external.model.Stream;
import com.laioffer.twitch.external.model.Video;

import java.util.ArrayList;
import java.util.List;

public record TypeGroupedItemList(
        List<ItemEntity> streams,
        List<ItemEntity> videos,
        List<ItemEntity> clips
) {

    // 全参构造器（默认的，JVM会创建）可以不用写出来
    // 任何额外的重载构造器都必须用 this(...) 调它初始化字段，就像下面那样
    public TypeGroupedItemList(
            List<ItemEntity> streams,
            List<ItemEntity> videos,
            List<ItemEntity> clips
    ) {
        this.streams = streams;
        this.videos = videos;
        this.clips = clips;
    }

    // 因为 record 的变量都是 final，所以不能这样构造
//    public TypeGroupedItemList(List<ItemEntity> items) {
//        this.streams = filterForType(items, ItemType.STREAM);
//        this.videos = filterForType(items, ItemType.VIDEO);
//        this.clips = filterForType(items, ItemType.CLIP);
//    }

    // 从数据库里拿：要分类，数据库里是不分类的，拿到的直接是所有items，这里转换成对应的类型
    // this: 这不是直接赋值给字段，而是调用 canonical constructor（全参构造器）
    public TypeGroupedItemList(List<ItemEntity> items) {
        this(
                filterForType(items, ItemType.STREAM),
                filterForType(items, ItemType.VIDEO),
                filterForType(items, ItemType.CLIP)
        );
    }

    // 从twitch拿：这里是分好类的，但是数据类型不对，要全部转为item entity
    public TypeGroupedItemList(String gameId, List<Stream> streams, List<Video> videos, List<Clip> clips) {
        this(
                fromStreams(streams),
                fromVideos(gameId, videos),
                fromClips(clips)
        );
    }

    private static List<ItemEntity> filterForType(List<ItemEntity> items, ItemType type) {
        List<ItemEntity> filtered = new ArrayList<>();
        for (ItemEntity item : items) {
            if (item.type() == type) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private static List<ItemEntity> fromStreams(List<Stream> streams) {
        List<ItemEntity> items = new ArrayList<>();
        for (Stream stream : streams) {
            items.add(new ItemEntity(stream));
        }
        return items;
    }

    private static List<ItemEntity> fromVideos(String gameId, List<Video> videos) {
        List<ItemEntity> items = new ArrayList<>();
        for (Video video : videos) {
            items.add(new ItemEntity(gameId, video));
        }
        return items;
    }

    private static List<ItemEntity> fromClips(List<Clip> clips) {
        List<ItemEntity> items = new ArrayList<>();
        for (Clip clip : clips) {
            items.add(new ItemEntity(clip));
        }
        return items;
    }
}
