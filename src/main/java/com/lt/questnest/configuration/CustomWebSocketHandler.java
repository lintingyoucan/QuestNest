package com.lt.questnest.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketHandler.class);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>(); // 存储 WebSocket 会话

    private String receiverEmail; // 接收方邮箱

    // 多行注释代码：Ctrl + Shift + /
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        // 从 WebSocket 会话中获取用户邮箱（存储在会话属性中）
        String email = (String) session.getAttributes().get("email");
        logger.info("为用户{}建立新的连接", email);
        sessions.put(email, session); // 记录用户的 WebSocket 会话

        // 向前端发送用户邮箱
        try {
            // 创建一个JSON对象，假设使用的是org.json库
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userEmail", email); // 将用户邮箱添加到JSON对象中
            logger.info("JSON形式的用户邮箱:{}", jsonObject);

            // 将JSON对象转换为字符串
            String jsonMessage = jsonObject.toString();
            logger.info("将JSON转化为字符串的用户邮箱:{}", jsonMessage);

            // 发送JSON字符串给前端
            session.sendMessage(new TextMessage(jsonMessage)); // 发送账号以JSON格式
        } catch (IOException e) {
            logger.error("发送消息失败", e);
        }
    }


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        Object payloadMessage = message.getPayload();
        logger.info("未区分消息对象前的取出消息:{}", payloadMessage);
        logger.info("接收到的消息类型: {}", message.getClass().getName());
        logger.info("消息内容: {}", payloadMessage);
        try {
            if (message instanceof BinaryMessage) {
                logger.info("进入message属于二进制的代码");
                handleBinaryMessage(session, (BinaryMessage) message);
            } else if (message instanceof TextMessage) {
                logger.info("进入message属于文本信息的代码");
                // 处理文本消息，解析JSON
                String payload = ((TextMessage) message).getPayload().trim();
                // 检查是否为心跳消息
                if ("heartbeat".equals(payload)) {
                    // 忽视心跳消息
                    return;
                }
                // 使用 ObjectMapper 解析 JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(payload);

                // 确保 jsonNode 被正确解析
                if (jsonNode.has("type") && jsonNode.get("type").asText().equals("binaryMessage")) {
                    // 获取接收者账号和内容
                    receiverEmail = jsonNode.get("receiver").asText();
                    logger.info("从元数据中获取接收方账号为: {}", receiverEmail);
                    return;
                }

                // 处理文本消息
                handleTextMessage(session, (TextMessage) message);

            } else {
                logger.warn("未知消息类型: {}", message.getClass().getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

        String payload = message.getPayload().trim();

        logger.info("消息message是:{}", message);
        logger.info("接收文本消息{}: ", payload);

        // 检查是否为心跳消息
        if ("heartbeat".equals(payload)) {
            // 忽视心跳消息
            return;
        }

        // 使用 ObjectMapper 解析 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(payload);

        // 检查是否同时包含 "user", "receiver" 和 "content"
        if (jsonNode.has("receiver") && jsonNode.has("content")) {
            String receiverEmail = jsonNode.path("receiver").asText();
            String msgContent = jsonNode.path("content").asText();

            logger.info("接收方邮箱:{}", receiverEmail);
            logger.info("消息内容: {}", msgContent);

            // 从 sessions 中获取接收方的 WebSocket 会话
            WebSocketSession recipientSession = sessions.get(receiverEmail);
            logger.info("从WebSessions中取出接收方邮箱:{}", sessions.get(receiverEmail));

            // 创建 JSON 格式的消息
            ObjectNode jsonResponse = objectMapper.createObjectNode();
            jsonResponse.put("sender", (String) session.getAttributes().get("email"));
            jsonResponse.put("content", msgContent);

            logger.info("组成的JSON消息:{}", jsonResponse);
            if (recipientSession != null) { // 检查接收方是否在线
                recipientSession.sendMessage(new TextMessage(jsonResponse.toString())); // 发送 JSON 格式的消息
            } else {
                // 如果接收方不在线，构造相应的 JSON 消息并发送
                jsonResponse.put("error", "用户 " + receiverEmail + " 不在线或不存在。");
                session.sendMessage(new TextMessage(jsonResponse.toString())); // 发送错误消息
            }
        }
    }

    private void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {

        logger.info("进入处理二进制数据的方法");
        byte[] payload = message.getPayload().array(); // 获取二进制数据
        logger.info("接收到二进制数据，大小: " + payload.length);
        logger.info("接收方账号receiverAccount:{}", receiverEmail);

        // 检查接收者是否在线并发送二进制消息
        WebSocketSession recipientSession = sessions.get(receiverEmail);
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new BinaryMessage(message.getPayload().array()));
        } else {
            session.sendMessage(new TextMessage("User " + receiverEmail + " 不在线."));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("连接出现错误 " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String account = (String) session.getAttributes().get("account");
        System.out.println("用户{}连接关闭: " + account);
        sessions.remove(account);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false; // 不支持局部消息
    }

}
