package com.lt.questnest.mapper;

import com.lt.questnest.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationMapper {

    int addConversation(Conversation conversation);

    Conversation findConversation(@Param("user1Id") int user1Id,@Param("user2Id") int user2Id);

    int updateConversation(Conversation conversation);

    int deleteByUserId(@Param("userId") Integer userId);

    int deleteByConversationId(@Param("conversationId") Integer conversationId);

    List<Conversation> findByUserId(@Param("userId") Integer userId);

    Conversation findByConversationId(@Param("conversationId") Integer conversationId);

    int updateUnreadCount(@Param("conversationId") Integer conversationId,@Param("unreadCount") Integer unreadCount);

    int updateLastMessageId(@Param("conversationId") Integer conversationId,@Param("lastMessageId") Integer lastMessageId);
}
