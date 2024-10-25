package com.lt.questnest.controller;

import com.lt.questnest.service.UserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class UserFollowController {

    @Autowired
    UserFollowService userFollowService;

    /**
     * 关注用户
     * 20241012
     * @param followedId
     * @param session
     * @return
     */
    @PostMapping("/addFollow")
    public ResponseEntity<Map<String, Object>> addFollow(@RequestParam("followedId") Integer followedId,
                                                             HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = userFollowService.addFollow(email,followedId);
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    result.put("userFollowId", map.get("userFollowId"));  // 返回添加后的ID
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
            result.put("message", "关注用户失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }


    /**
     * 取消关注用户
     * 20241013
     * @param userFollowId
     * @param session
     * @return
     */
    @PostMapping("/cancelFollow")
    public ResponseEntity<Map<String, Object>> cancelFollow(@RequestParam("userFollowId") Integer userFollowId,
                                                         HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = userFollowService.cancelFollow(email,userFollowId);
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
            result.put("message", "取消关注用户失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示粉丝列表
     * 20241013
     * @param session
     * @return
     */
    @GetMapping("/showFan")
    public ResponseEntity<Map<String, Object>> showFan(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = userFollowService.showFan(email);
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    // 检查是否有粉丝
                    if (map.containsKey("fan")){
                        result.put("fan",map.get("fan")); // 如果有粉丝，返回粉丝列表
                    } else {
                        result.put("msg","暂无粉丝");
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
            result.put("message", "显示粉丝列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示关注列表
     * 20241013
     * @param session
     * @return
     */
    @GetMapping("/showFollowed")
    public ResponseEntity<Map<String, Object>> showFollowed(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, Object> map = userFollowService.showFollowed(email);
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    // 检查是否有粉丝
                    if (map.containsKey("followed")){
                        result.put("followed",map.get("followed")); // 如果有粉丝，返回粉丝列表
                    } else {
                        result.put("msg","暂无关注用户");
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
            result.put("message", "显示关注列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }
}
