package com.lt.questnest.controller;

import com.lt.questnest.service.DraftService;
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
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class DraftController {

    @Autowired
    FileService fileService;

    @Autowired
    DraftService draftService;

    private static final Logger logger = LoggerFactory.getLogger(DraftController.class);


    /**
     * 保存草稿
     * 20241011
     * @param title
     * @param content
     * @param images
     * @param videos
     * @return
     */
    @PostMapping("/saveDraft")
    public ResponseEntity<Map<String,Object>> saveDraft(@RequestParam("title") String title,
                                                        @RequestParam("content") String content,
                                                        @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 图片文件
                                                        @RequestParam(value = "videos", required = false) List<MultipartFile> videos,  // 视频文件
                                                        Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 对content进行加工，对图片、视频转成URL插入到原文
                String processedContent = fileService.processContentAndMedia(content, images, videos);
                // 返回文章添加结果
                Map<String,Object> map = draftService.saveDraft(email,title,processedContent);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("draftId",map.get("draftId"));
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
            result.put("message", "草稿保存失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 更新草稿
     * 20241011
     * @param draftId
     * @param content
     * @param images
     * @param videos
     * @return
     */
    @PostMapping("/updateDraft")
    public ResponseEntity<Map<String,Object>> updateDraft(@RequestParam("draftId") int draftId,
                                                          @RequestParam("content") String content,
                                                          @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 图片文件
                                                          @RequestParam(value = "videos", required = false) List<MultipartFile> videos,  // 视频文件
                                                          Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 对content进行加工，对图片、视频转成URL插入到原文
                String processedContent = fileService.processContentAndMedia(content, images, videos);
                // 返回结果
                Map<String,String> map = draftService.updateDraft(draftId,processedContent);

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
            result.put("message", "草稿更新失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 发布草稿
     * 20241011
     * @param draftId
     * @return
     */
    @PostMapping("/postDraft")
    public ResponseEntity<Map<String,Object>> postDraft(@RequestParam("draftId") Integer draftId,
                                                        Principal principal){

        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,Object> map = draftService.postDraft(draftId);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("articleId",map.get("articleId"));
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
            result.put("message", "草稿发布失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }


    /**
     * 删除草稿
     * 20241011
     * @param draftId
     * @return
     */
    @PostMapping("/deleteDraft")
    public ResponseEntity<Map<String,Object>> deleteDraft(@RequestParam("draftId") Integer draftId,
                                                          Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,String> map = draftService.deleteDraft(draftId);

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
            result.put("message", "草稿删除失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }


    /**
     * 根据draftId显示草稿
     * 20241011
     * @param draftId
     * @return
     */
    @PostMapping("/showDraftByDraftId")
    public ResponseEntity<Map<String,Object>> showDraftByDraftId(@RequestParam("draftId") Integer draftId,
                                                                 Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,Object> map = draftService.showDraftByDraftId(draftId);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("draft",map.get("draft"));//返回草稿信息
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
            result.put("message", "显示草稿失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }


    /**
     * 根据用户显示草稿
     * 20241011
     * @return
     */
    @PostMapping("/showDraftByUserId")
    public ResponseEntity<Map<String,Object>> showDraftByUserId(Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,Object> map = draftService.showDraftByUserId(email);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("drafts",map.get("drafts"));//返回草稿信息
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
            result.put("message", "显示草稿失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }
}
