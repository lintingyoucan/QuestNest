package com.lt.questnest.controller;

import com.lt.questnest.service.InformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class InformController {

    @Autowired
    InformService informService;

    /**
     * 返回未读消息条数
     * 20241105
     * @param session
     * @return
     */
    @GetMapping("/getUnreadNumber")
    public ResponseEntity<Map<String,Object>> getUnreadNumber(HttpSession session) {

        String email = (String) session.getAttribute("email");
        Map<String,Object> result = new HashMap<>();
        try {
            if (email != null || !(email.isEmpty())){ // 用户已登录
                // 处理后结果
                Integer unread = informService.getUnreadNumber(email);
                result.put("status","success");
                result.put("unread",unread); // 返回未读消息条数
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "显示未读消息条数失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态

        }
    }

    /**
     * 20241106
     * @param session
     * @return
     */
    @GetMapping("/showInform")
    public ResponseEntity<Map<String, Object>> showInform(HttpSession session) {

        Map<String,Object> result = new HashMap<>();
        String email = (String)session.getAttribute("email");

        try {
            if (email != null && !(email.isEmpty())){ // 判断用户是否已经登录
                Map<String, Object> getResult = informService.showInform(email);
                if (getResult.containsValue("success")){ // 是否有返回列表
                    result.put("status","success");
                    result.put("inform",getResult.get("inform")); // 返回消息列表
                    return ResponseEntity.ok(result); // 返回200状态，并返回问题列表
                } else {
                    result.put("status","error");
                    result.put("message",getResult.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }

            } else {
                result.put("status","error");
                result.put("message","用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "显示用户订阅信息失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }


}
