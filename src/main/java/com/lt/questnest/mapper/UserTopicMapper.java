package com.lt.questnest.mapper;

import com.lt.questnest.entity.UserTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserTopicMapper {

    UserTopic findByBoth(@Param("userId") Integer userId,@Param("topicId") Integer topicId);

    int addUserTopic(UserTopic userTopic);

    UserTopic findByUserTopicId(@Param("userTopicId") Integer userTopicId);

    int deleteByUserTopicId(@Param("userTopicId") Integer userTopicId);

    int deleteByUserId(@Param("userId") Integer userId);

    List<UserTopic> findByUserId(@Param("userId") Integer userId);
}
