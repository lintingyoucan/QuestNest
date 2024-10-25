package com.lt.questnest.controller;

import com.lt.questnest.service.AdminUserMuteService;
import com.lt.questnest.service.CommentService;
import com.lt.questnest.service.FileService;
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
import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class CommentController {

    @Autowired
    FileService fileService;

    @Autowired
    CommentService commentService;

    @Autowired
    AdminUserMuteService adminUserMuteService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 评论文章、回答评论
     * 20240922
     * @param articleId
     * @param content
     * @param images
     * @param session
     * @return
     */
    @PostMapping("/addComment")
    public ResponseEntity<Map<String, Object>> addComment(@RequestParam("articleId") Integer articleId,
                                                          @RequestParam(value = "parentCommentId", required = false) Integer parentCommentId,  // 使用包装类型 Integer
                                                          @RequestParam("content") String content,
                                                          @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 图片文件
                                                          HttpSession session){

        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 检查用户是否被禁言
                Map<String,Object> isMute = adminUserMuteService.isMute(email);
                if (isMute.containsValue("error")){ // 用户已被禁言
                    result.put("status","error");
                    result.put("message",isMute.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                }

                // 对content进行加工，对图片、视频转成URL插入到原文
                String processedContent = fileService.processContentAndMedia(content, images, null);
                logger.info("处理后的content:{}",processedContent);
                // 返回文章添加结果
                Map<String,Object> map = commentService.addComment(email,articleId,parentCommentId,processedContent);

                if (map.containsValue("success")){
                    result.put("status","success");
                    if (parentCommentId == null || parentCommentId <= 0) {
                        result.put("commentId", map.get("commentId"));//返回commentId,便于用户点击问题时可以定位到评论
                    } else {
                        result.put("parentCommentContent",map.get("parentCommentContent")); // 返回父评论内容
                        result.put("commentId",map.get("commentId")); // 返回commentId,便于用户点击问题时可以定位到评论
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
            result.put("message", "评论文章失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 点赞评论
     * 20240921
     * @param commentId
     * @param session
     * @return
     */
    @PostMapping("/agreeComment")
    public ResponseEntity<Map<String,Object>> agreeComment(@RequestParam("commentId") int commentId,
                                                           HttpSession session){
        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                Map<String,String> map = commentService.agreeComment(email,commentId);
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
            result.put("message", "点赞评论失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 取消点赞评论
     * 20240921
     * @param commentId
     * @param session
     * @return
     */
    @PostMapping("/cancelAgreeComment")
    public ResponseEntity<Map<String,Object>> cancelAgreeComment(@RequestParam("commentId") int commentId,
                                                                 HttpSession session){
        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,String> map = commentService.cancelAgreeComment(email,commentId);
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
            result.put("message", "取消点赞评论失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }


    /**
     * 返回评论点赞人数
     * 20240921
     * @param commentId
     * @param session
     * @return
     */
    @PostMapping("/agreeCommentNumber")
    public ResponseEntity<Map<String,Object>> agreeCommentNumber(@RequestParam("commentId") int commentId,
                                                                  HttpSession session){
        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,Object> map = commentService.agreeCommentNumber(commentId);
                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("like",map.get("like"));//返回点赞人数
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
            result.put("message", "返回点赞评论人数失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 删除评论
     * 20241010
     * @param commentId
     * @param session
     * @return
     */
    @PostMapping("/deleteComment")
    public ResponseEntity<Map<String,Object>> deleteComment(@RequestParam("commentId") int commentId,
                                                                 HttpSession session){
        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,String> map = commentService.deleteComment(commentId,email);
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
            result.put("message", "删除评论失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

}
