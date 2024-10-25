package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.*;
import com.lt.questnest.mapper.ConversationMapper;
import com.lt.questnest.mapper.MessageMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    MessageMapper messageMapper;

    private static final Logger logger = LoggerFactory.getLogger(ConversationServiceImpl.class);

    // 找出用户会话
    public Map<String, Object> getConversation(String email) {

        Map<String, Object> result = new HashMap<>();

        User user = userMapper.getUserByEmail(email); // 取出用户
        Integer userId = user.getId(); // 用户ID

        List<Conversation> conversations = conversationMapper.findByUserId(userId); // 取出会话
        // 取出用户会话
        if (conversations == null || conversations.isEmpty()) {
            result.put("status", "success");
            result.put("conversation", new ArrayList<>()); // 返回空列表
            return result;
        }
        logger.info("conversation列表:{}",conversations);

        // 数据结果列表
        List<Map<String, Object>> conversationList = new ArrayList<>();

        // 组合数据：对话用户ID、头像、用户名，会话ID、最近通信时间（年月日）、最后一条通信消息、未读消息数
        for (Conversation conversation : conversations) {

            Map<String, Object> conversationItem = new HashMap<>();
            Integer user1Id = conversation.getUser1Id();
            Integer user2Id = conversation.getUser2Id();
            // 取出对方用户相关信息
            if (user1Id != userId) { // 如果用user1 != user,说明另一方是user1,当前登录用户是user2
                conversationItem.put("userId", conversation.getUser1Id());
                // 取出对方用户的username、headUrl
                User receiver = userMapper.getUserById(user1Id);
                String username = receiver.getUsername();
                String headUrl = receiver.getHeadUrl();
                conversationItem.put("username", username);
                conversationItem.put("headUrl", headUrl);
            } else {
                conversationItem.put("userId", user2Id);
                User receiver = userMapper.getUserById(user2Id);
                String username = receiver.getUsername();
                String headUrl = receiver.getHeadUrl();
                conversationItem.put("username", username);
                conversationItem.put("headUrl", headUrl);
            }

            // 取出会话ID
            conversationItem.put("conversationId", conversation.getConversationId());
            // 取出会话未读消息数
            conversationItem.put("unreadCount",conversation.getUnreadCount());

            // 取出最近通话时间:改变时间格式为：年-月-日
            Timestamp lastTimeBefore = conversation.getUpdateTime();
            // 创建日期格式化对象
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            // 格式化时间戳
            String lastTime = dateFormat.format(lastTimeBefore);
            conversationItem.put("lastMessageTime", lastTime);

            // 取出最近通话信息ID
            Integer messageId = conversation.getLastMessageId();

            // 取出最近消息内容
            String content = messageMapper.findByMessageId(messageId).getContent();
            conversationItem.put("lastMessageContent", content);

            conversationList.add(conversationItem);
        }

        logger.info("conversation:{}",conversationList);
        result.put("conversation", conversationList); // 返回会话列表
        result.put("status", "success");

        return result;

    }


    // 删除会话
    @Transactional
    public Map<String, Object> deleteConversation(String email, Integer conversationId) {

        Map<String, Object> result = new HashMap<>();

        // 找出用户ID
        Integer userId = userMapper.getUserByEmail(email).getId();
        // 找出会话
        Conversation conversation = conversationMapper.findByConversationId(conversationId);
        if (conversation == null) {
            result.put("status", "error");
            result.put("msg", "会话不存在");
            return result;
        }

        // 检查用户是否非法操作：会话的双方，有没有一个是用户
        if (conversation.getUser1Id() != userId && conversation.getUser2Id() != userId) {
            result.put("status", "error");
            result.put("msg", "非法操作");
            return result;
        }

        Integer deleteConversationResult = conversationMapper.deleteByConversationId(conversationId);
        if (deleteConversationResult == null || deleteConversationResult <= 0){
            result.put("status","error");
            result.put("msg","删除会话失败");
            return result;
        }

        result.put("status", "success");
        return result;
    }
}
