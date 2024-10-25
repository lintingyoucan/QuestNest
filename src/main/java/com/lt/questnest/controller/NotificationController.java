package com.lt.questnest.controller;

import com.lt.questnest.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class NotificationController {

    @Autowired
    RedisService redisService;

    /**
     * 用户获取所有通知
     * 20241005
     * @param session
     * @return
     */
    @PostMapping("/getNotification")
    public ResponseEntity<Map<String,Object>> getNotification(HttpSession session){

        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                String redisKey =  "notifications:"+email;
                // 返回结果
                List<String> notifications = redisService.getNotifications(redisKey);

                if (notifications == null || notifications.isEmpty()){
                    result.put("status","success");
                    result.put("message","暂无通知");
                    return ResponseEntity.ok(result);
                }

                result.put("status","success");
                result.put("通知",notifications);
                return ResponseEntity.ok(result);

            } else {
                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "获取通知失败失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

}
