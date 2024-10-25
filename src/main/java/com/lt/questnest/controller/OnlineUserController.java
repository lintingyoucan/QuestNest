package com.lt.questnest.controller;

import com.lt.questnest.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin
public class OnlineUserController {

    @Autowired
    OnlineUserService onlineUserService;

    /**
     * 获取在线人数
     * 20241015
     * @param session
     * @return
     */
    @GetMapping("/getOnlineUserCount")
    public ResponseEntity<Map<String,Object>> getOnlineUserCount(HttpSession session){

        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Integer onlineUserNumber = onlineUserService.getOnlineUserCount();

                if (onlineUserNumber == null){
                    result.put("status","success");
                    result.put("在线人数:","0");
                    return ResponseEntity.ok(result);
                }

                result.put("status","success");
                result.put("在线人数:",onlineUserNumber);
                return ResponseEntity.ok(result);

            } else {
                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "获取在线人数失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }


    /**
     * 获取在线用户
     * 20241015
     * @param session
     * @return
     */
    @GetMapping("/getOnlineUser")
    public ResponseEntity<Map<String,Object>> getOnlineUser(HttpSession session){

        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Set<String> users = onlineUserService.getOnlineUsers();

                if (users == null || users.isEmpty()){
                    result.put("status","success");
                    result.put("在线人数:","0");
                    return ResponseEntity.ok(result);
                }

                result.put("status","success");
                result.put("在线人数:",users);
                return ResponseEntity.ok(result);

            } else {
                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "获取在线用户失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

}
