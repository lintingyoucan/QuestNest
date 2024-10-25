package com.lt.questnest.mapper;

import com.lt.questnest.entity.Favourite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavouriteMapper {

    int addFavourite(Favourite favourite);

    int deleteFavouriteByFavouriteId(@Param("favouriteId") Integer favouriteId);

    Favourite findByBoth(@Param("userId") Integer userId,@Param("name") String name);

    Favourite findByFavouriteId(@Param("favouriteId") Integer favouriteId);

    List<Favourite> findByUserId(@Param("userId") Integer userId);

    int deleteByUserId(@Param("userId") Integer userId);
}
