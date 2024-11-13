package com.lt.questnest.controller;

import com.lt.questnest.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class HistoryController {

    @Autowired
    HistoryService historyService;

    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);


    /**
     * 添加浏览历史记录:需要在用户浏览回答时，发送回答ID
     * 20241013
     * @param articleId
     * @return
     */
    @PostMapping("/addHistory")
    public ResponseEntity<Map<String, Object>> addHistory(@RequestParam("articleId") Integer articleId,
                                                          Principal principal) {
        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = historyService.addHistory(email,articleId);
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    return ResponseEntity.ok(result);  // 返回200 OK状态
                } else {
                    result.put("status", "error");
                    result.put("message", map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态，表示请求有误
                }

            } else {

                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401未授权状态

            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "添加浏览历史失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示7天内浏览历史
     * 20241013
     * @return
     */
    @GetMapping("/showHistory")
    public ResponseEntity<Map<String, Object>> showHistory(Principal principal) {
        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = historyService.showHistory(email);
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    if (map.containsKey("history")){
                        result.put("history",map.get("history"));
                    } else {
                        result.put("msg",map.get("msg"));
                    }
                    return ResponseEntity.ok(result);  // 返回200 OK状态
                } else {
                    result.put("status", "error");
                    result.put("message", map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态，表示请求有误
                }

            } else {

                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401未授权状态

            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "显示浏览历史失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }
}
