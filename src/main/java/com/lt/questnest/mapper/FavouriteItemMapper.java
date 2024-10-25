package com.lt.questnest.mapper;

import com.lt.questnest.entity.FavouriteItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavouriteItemMapper {

    FavouriteItem findByBoth(@Param("favouriteId") Integer favouriteId,@Param("articleId") Integer articleId);

    int addFavouriteItem(FavouriteItem favouriteItem);

    FavouriteItem findByFavouriteItemId(@Param("favouriteItemId") Integer favouriteItemId);

    int deleteFavouriteItemByFavouriteItemId(@Param("favouriteItemId") Integer favouriteItemId);

    List<FavouriteItem> findByFavouriteId(@Param("favouriteId") Integer favouriteId);
}
