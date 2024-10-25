package com.lt.questnest.mapper;

import com.lt.questnest.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFollowMapper {

    UserFollow findByBoth(@Param("followerId") Integer followerId,@Param("followedId") Integer followedId);

    int addFollow(UserFollow userFollow);

    UserFollow findByUserFollowId(@Param("userFollowId") Integer userFollowId);

    int updateState(@Param("userFollowId") Integer userFollowId);

    List<UserFollow> findByFollowedId(@Param("followedId") Integer followedId);

    List<UserFollow> findByFollowerId(@Param("followerId") Integer followerId);
}
