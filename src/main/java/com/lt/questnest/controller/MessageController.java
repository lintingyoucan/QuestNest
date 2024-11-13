package com.lt.questnest.controller;

import com.lt.questnest.service.FileService;
import com.lt.questnest.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class MessageController {

    @Autowired
    MessageService messageService;

    @Autowired
    FileService fileService;

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);


    /**
     * 私信
     * 202041021
     * @param receiverEmail
     * @param content
     * @param images
     * @param videos
     * @return
     */
    @PostMapping("/sendMessage")
    public ResponseEntity<Map<String,Object>> sendMessage(@RequestParam("receiverEmail") String receiverEmail,
                                                          @RequestParam("messageContent") String content,
                                                          @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 图片文件
                                                          @RequestParam(value = "videos", required = false) List<MultipartFile> videos,  // 视频文件
                                                          Principal principal) {

        String senderEmail = principal.getName();
        logger.info("取出的email:{}",senderEmail);
        Map<String,Object> result = new HashMap<>();
        try {
            if (senderEmail != null || !(senderEmail.isEmpty())){ // 用户已登录
                // 对content进行加工，对图片、视频转成URL插入到原文
                String processedContent = fileService.processContentAndMedia(content, images, null);
                // 处理后结果
                Map<String,Object> map = messageService.sendMessage(senderEmail,receiverEmail,processedContent);
                if (map.containsValue("success")){
                    result.put("status","success");

                    if (map.containsKey("conversationId")){
                        result.put("conversationId",map.get("conversationId"));//如果创建新的会话，那么返回会话ID
                    }
                    return ResponseEntity.ok(result);
                } else {
                    result.put("status","error");
                    result.put("message",map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }
            } else {
            result.put("status", "error");
            result.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
        }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "私信失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态

        }
    }

    /**
     * 显示用户间的私信
     * 20241021
     * @param conversationId
     * @return
     */
    @PostMapping("/getMessage")
    public ResponseEntity<Map<String,Object>> getMessage(@RequestParam("conversationId") Integer conversationId,
                                                         Principal principal) {

        String senderEmail = principal.getName();
        logger.info("取出的email:{}",senderEmail);
        Map<String,Object> result = new HashMap<>();
        try {
            if (senderEmail != null || !(senderEmail.isEmpty())){ // 用户已登录
                // 处理后结果
                Map<String,Object> map = messageService.getMessage(conversationId);
                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("msg",map.get("messages")); // 返回用户间的私信列表
                    return ResponseEntity.ok(result);
                } else {
                    result.put("status","error");
                    result.put("message",map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }
            } else {
                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "显示私信失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态

        }
    }

}
