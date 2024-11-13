package com.lt.questnest.controller;

import com.lt.questnest.service.UserTopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class UserTopicController {

    @Autowired
    UserTopicService userTopicService;

    private static final Logger logger = LoggerFactory.getLogger(UserTopicController.class);


    /**
     * 添加关注话题：一次性可以选择一个或多个
     * 20241013
     * @param topicId
     * @return
     */
    @PostMapping("/addConcernTopic")
    public ResponseEntity<Map<String, Object>> addConcernTopic(@RequestParam("topicIds") List<Integer> topicId,
                                                               Principal principal) {
        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = userTopicService.addConcernTopic(email,topicId);
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    result.put("userTopic", map.get("userTopic"));  // 返回添加后的信息
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
            result.put("message", "添加关注话题失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 取消关注话题
     * 20241013
     * @param userTopicId
     * @return
     */
    @PostMapping("/cancelConcernTopic")
    public ResponseEntity<Map<String, Object>> cancelConcernTopic(@RequestParam("userTopicId") Integer userTopicId,
                                                                  Principal principal) {
        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = userTopicService.cancelConcernTopic(email,userTopicId);
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
            result.put("message", "取消关注话题失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示关注话题列表
     * 20241013
     * @return
     */
    @GetMapping("/showTopic")
    public ResponseEntity<Map<String, Object>> showTopic(Principal principal) {
        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = userTopicService.showTopic(email);
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    // 检查是否有关注话题
                    if (map.containsKey("topic")){
                        result.put("topic",map.get("topic")); // 如果有，返回关注话题列表
                    } else {
                        result.put("msg","暂无关注话题");
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
            result.put("message", "显示关注话题列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }


}
