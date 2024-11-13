package com.lt.questnest.controller;

import com.lt.questnest.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class ConversationController {

    @Autowired
    ConversationService conversationService;

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    /**
     * 显示用户所有会话
     * 20241021
     * @return
     */
    @GetMapping("/showConversation")
    public ResponseEntity<Map<String,Object>> showConversation(Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                Map<String,Object> map = conversationService.getConversation(email);
                if (map.containsValue("success")){
                    result.put("status","success");
                    // 返回用户会话
                    result.put("conversation",map.get("conversation"));
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
            // 打印堆栈跟踪到日志
            // 捕获异常并记录堆栈跟踪
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw); // 将异常的堆栈写入 PrintWriter
            logger.error("显示会话失败: {}", sw); // 记录异常堆栈信息
            result.put("status", "error");
            result.put("message", "显示会话失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 删除会话：删除会话和私信
     * 20241021
     * @param conversationId
     * @return
     */
    @PostMapping("/deleteConversation")
    public ResponseEntity<Map<String,Object>> deleteConversation(@RequestParam("conversationId") Integer conversationId,
                                                                 Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                Map<String,Object> map = conversationService.deleteConversation(email,conversationId);
                if (map.containsValue("success")){
                    result.put("status","success");
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
            result.put("message", "显示会话失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }
}
