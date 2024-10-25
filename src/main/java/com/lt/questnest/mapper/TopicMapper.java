package com.lt.questnest.mapper;

import com.lt.questnest.entity.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TopicMapper {

    // 找topic
    Topic findByTopic(@Param("name") String name);

    // 添加topic
    int addTopic(@Param("name") String name);

    String findByTopicId(@Param("topicId") Integer topicId);
}
