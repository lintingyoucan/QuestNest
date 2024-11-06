package com.lt.questnest.service.Impl;

import com.lt.questnest.configuration.CustomWebSocketHandler;
import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Conversation;
import com.lt.questnest.entity.Message;
import com.lt.questnest.entity.User;
import com.lt.questnest.mapper.ConversationMapper;
import com.lt.questnest.mapper.MessageMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.MessageService;
import com.lt.questnest.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    RedisService redisService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    CustomWebSocketHandler webSocketHandler;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 发送信息
    @Transactional
    public Map<String,Object> sendMessage(String senderEmail,String receiverEmail,String content){

        Map<String,Object> result = new HashMap<>();

        // 参数判空
        if (receiverEmail == null || receiverEmail.isEmpty()){
            result.put("status","error");
            result.put("msg","receiverEmail参数不能空");
            return result;
        }
        if (content == null || content.isEmpty()){
            result.put("status","error");
            result.put("msg","content不能空");
            return result;
        }

        // 创建Message实体类
        Message message = new Message();

        try {
            // 更新会话信息并保存到数据库
            // 将信息插入message和conversation
            // 获取用户ID
            Integer senderId = userMapper.getUserByEmail(senderEmail).getId();
            User receiver = userMapper.getUserByEmail(receiverEmail);
            // 检查接收方是否存在
            if (receiver == null){
                result.put("status","error");
                result.put("msg","接收方不存在");
                return result;
            }
            Integer receiverId = receiver.getId();

            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setContent(content);

            // 找出两个人是否为第一次会话
            Map<Object,Object> isConversation = findConversation(senderId,receiverId);
            // 如果对话不存在，创建新对话
            if (!(isConversation.containsKey("conversation"))){
                // 创建Conversation实体类
                Conversation conversation = new Conversation();
                conversation.setUser1Id(senderId);
                conversation.setUser2Id(receiverId);
                conversation.setLastMessageId(null);
                conversation.setUnreadCount(1); // 会话的未读消息为1条

                // 将数据保存进数据库
                logger.info("进行conversation添加操作");
                Integer addConversationResult = conversationMapper.addConversation(conversation);
                if (addConversationResult == null || addConversationResult <= 0){
                    result.put("status","error");
                    result.put("msg","创建新会话进数据库失败");
                    return result;
                }
                // 将会话ID返回
                Integer conversationId = conversation.getConversationId();
                result.put("conversationId",conversationId);

                message.setConversationId(conversationId);
                // 将Message数据保存进数据库
                logger.info("进行message数据库添加操作");
                Integer addMessageResult = messageMapper.addMessage(message);
                if (addMessageResult == null || addMessageResult <= 0){
                    result.put("status","error");
                    result.put("msg","将Message添加进数据库失败");
                    return result;
                }

                // 更新会话的lastMessageId:将刚刚添加的messageId作为conversation的lastMessageId
                Integer updateLastMessageId = conversationMapper.updateLastMessageId(conversationId,message.getMessageId());
                if (updateLastMessageId == null || updateLastMessageId <= 0){
                    result.put("status","error");
                    result.put("msg","更新lastMessageId添加进数据库失败");
                    return result;
                }

            } else {

                // 如果对话存在，那么更新对话的最新时间
                Conversation conversation = (Conversation) isConversation.get("conversation");

                message.setConversationId(conversation.getConversationId());
                // 将Message数据保存进数据库
                logger.info("进行message数据库添加操作");
                Integer addMessageResult = messageMapper.addMessage(message);
                if (addMessageResult == null || addMessageResult <= 0){
                    result.put("status","error");
                    result.put("msg","将Message添加进数据库失败");
                    return result;
                }

                // 找出双方最后一次通信的信息ID
                Integer messageId = message.getMessageId();
                conversation.setLastMessageId(messageId);
                // 更新未读信息
                conversation.setUnreadCount((conversation.getUnreadCount()+1));
                Integer updateConversationResult = conversationMapper.updateConversation(conversation);
                if (updateConversationResult == null || updateConversationResult <= 0){
                    result.put("status","error");
                    result.put("msg","更新会话进数据库失败");
                    return result;
                }
            }


        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库操作失败");
        }

        result.put("status","success");
        return result;

    }

    private Map<Object,Object> findConversation(int senderId,int receiverId){

        Map<Object,Object> result = new HashMap<>();
        Conversation conversation1 = conversationMapper.findConversation(senderId,receiverId);
        if (conversation1 != null){
            result.put("msg","对话已存在");
            result.put("conversation",conversation1);
            return result;
        }
        Conversation conversation2 = conversationMapper.findConversation(receiverId,senderId);
        if (conversation2 != null){
            result.put("msg","对话已存在");
            result.put("conversation",conversation2);
            return result;
        }
        result.put("msg","无对话");
        return result;
    }

    // 显示用户间的会话
    // 找出两个对话人:user1,user2
    // 找出两个人之间的私信
    // 按时间先后排列出来
    // 获取用户间私信
    // 将会话未读信息置为0和message的read置为1
    @Transactional
    public Map<String,Object> getMessage(Integer conversationId){

        Map<String,Object> result = new HashMap<>();

        // 参数判空
        if (conversationId == null){
            result.put("status","error");
            result.put("msg","会话ID不能空");
            return result;
        }

        List<Message> messages = null;

        try {

            // 检查会话是否存在
            Conversation conversation = conversationMapper.findByConversationId(conversationId);
            if (conversation == null){
                result.put("status","error");
                result.put("msg","会话不存在");
                return result;
            }

            // 将会话未读消息置为0
            Integer updateUnreadCount = conversationMapper.updateUnreadCount(conversationId, 0);
            if (updateUnreadCount == null || updateUnreadCount <= 0){
                result.put("status","error");
                result.put("msg","未读消息置为0失败");
                return result;
            }

            // 找出两人私信的信息
            messages = messageMapper.findByConversationId(conversationId);

        } catch (Exception e) {
            result.put("status","error");
            result.put("msg","数据库访问失败");
            return result;
        }

        // 如果messages为空或null，返回空列表
        if (messages == null || messages.isEmpty()){
            result.put("messages", new ArrayList<>());
            result.put("status","success");
            return result;
        }

        // 按照 createTime 属性排序，时间早的在前面
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                return m1.getCreateTime().compareTo(m2.getCreateTime());
            }
        });

        // 根据会话ID，将消息的状态置为已读
        try {
            Integer updateRead = messageMapper.updateRead(conversationId);
        } catch (Exception e) {
            throw new RuntimeException("更新私信的已读状态失败");
        }

        // 数据结果列表
        Map<String, List<Map<String, Object>>> groupedMessages = new HashMap<>();

        // 取出消息的content、发送时间createTime,发送方ID、username、headUrl，返回
        for (Message message : messages) {

            Map<String, Object> messageItem = new HashMap<>();

            Integer senderId = message.getSenderId();
            User sender = userMapper.getUserById(senderId);
            String username = sender.getUsername();
            String headUrl = sender.getHeadUrl();
            String content = message.getContent();
            Timestamp sendTimeBefore = message.getCreateTime();
            // 创建日期格式化对象
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            // 格式化时间戳
            String sendTime = dateFormat.format(sendTimeBefore);

            // 将消息按时间分组：3分钟间隔
            // 计算分组的起始时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sendTimeBefore);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 计算3分钟的间隔
            long intervalInMillis = 3 * 60 * 1000;
            long groupStartTimeMillis = calendar.getTimeInMillis() - (calendar.getTimeInMillis() % intervalInMillis);
            String groupKey = dateFormat.format(new Date(groupStartTimeMillis));

            // 将消息项添加到分组
            List<Map<String, Object>> groupMessages = groupedMessages.getOrDefault(groupKey, new ArrayList<>());
            messageItem.put("username", username);
            messageItem.put("headUrl", headUrl);
            messageItem.put("content", content);
            groupMessages.add(messageItem);

            // 更新分组消息集合
            groupedMessages.put(groupKey, groupMessages);

        }

        // 返回合并后的消息列表
        result.put("messages",groupedMessages);
        result.put("status","success");

        return result;

    }
}
