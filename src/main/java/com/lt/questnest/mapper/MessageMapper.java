package com.lt.questnest.mapper;

import com.lt.questnest.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    int addMessage(Message message);

    List<Message> findMessage(@Param("senderId") int senderId, @Param("receiverId") int receiverId);


    int deleteByUserId(@Param("userId") Integer userId);

    int deleteByConversationId(@Param("conversationId") Integer conversationId);


    List<Message> findByUserId(@Param("userId") Integer userId);

    Message findByMessageId(@Param("messageId") Integer messageId);

    int updateRead(@Param("conversationId") Integer conversationId);

    List<Message> findByConversationId(@Param("conversationId") Integer conversationId);
}
