package com.lt.questnest.controller;

import com.lt.questnest.service.AdminUserMuteService;
import com.lt.questnest.service.ArticleService;
import com.lt.questnest.service.FileService;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class ArticleController {

    @Autowired
    ArticleService articleService;

    @Autowired
    FileService fileService;

    @Autowired
    AdminUserMuteService adminUserMuteService;

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);


    /**
     * 添加文章
     * 20240920
     * @param title
     * @param content
     * @param images
     * @param videos
     * @return
     */
    @PostMapping("/addArticle")
    public ResponseEntity<Map<String, Object>> addArticle(@RequestParam("title") String title,
                                                          @RequestParam("content") String content,
                                                          @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 图片文件
                                                          @RequestParam(value = "videos", required = false) List<MultipartFile> videos,  // 视频文件
                                                          Principal principal){

        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

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
                String processedContent = fileService.processContentAndMedia(content, images, videos);
                // 返回文章添加结果
                Map<String,Object> map = articleService.addArticle(email,title,processedContent);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("articleId",map.get("articleId")); // 返回文章ID
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
            result.put("message", "问题添加失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 赞同文章
     * 20240920
     * @return
     */
    @PostMapping("/agreeArticle")
    public ResponseEntity<Map<String, Object>> agreeArticle(@RequestParam("articleId") int articleId,
                                                            Principal principal){

        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);
        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,String> map = articleService.agreeArticle(email,articleId);
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
            result.put("message", "赞同文章失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 反对文章
     * 20240920
     * @param articleId
     * @return
     */
    @PostMapping("/disagreeArticle")
    public ResponseEntity<Map<String, Object>> disagreeArticle(@Param("articleId") int articleId,
                                                               Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,String> map = articleService.disagreeArticle(email,articleId);
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
            result.put("message", "反对文章失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 取消赞同文章
     * 20240920
     * @param articleId
     * @return
     */
    @PostMapping("/cancelAgreeArticle")
    public ResponseEntity<Map<String, Object>> cancelAgreeArticle(@RequestParam("articleId") int articleId,
                                                                  Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,String> map = articleService.cancelAgreeArticle(email,articleId);
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
            result.put("message", "取消赞同文章失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 取消反对文章
     * 20240920
     * @param articleId
     * @return
     */
    @PostMapping("/cancelDisAgreeArticle")
    public ResponseEntity<Map<String, Object>> cancelDisAgreeArticle(@RequestParam("articleId") int articleId,
                                                                     Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,String> map = articleService.cancelDisagreeArticle(email,articleId);
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
            result.put("message", "取消反对文章失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 返回点赞文章人数
     * 20240921
     * @param articleId
     * @return
     */
    @PostMapping("/agreeArticleNumber")
    public ResponseEntity<Map<String,Object>> agreeArticleNumber(@RequestParam("articleId") int articleId,
                                                                 Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,Object> map = articleService.agreeArticleNumber(articleId);
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
            result.put("message", "返回点赞文章人数失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }


    /**
     * 修改文章
     * 20241010
     * @param articleId
     * @param content
     * @param images
     * @param videos
     * @return
     */
    @PostMapping("/updateArticle")
    public ResponseEntity<Map<String,Object>> updateArticle(@RequestParam("articleId") Integer articleId,
                                                            @RequestParam("content") String content,
                                                            @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 图片文件
                                                            @RequestParam(value = "videos", required = false) List<MultipartFile> videos,  // 视频文件
                                                            Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

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
                String processedContent = fileService.processContentAndMedia(content, images, videos);
                // 返回结果
                Map<String,String> map = articleService.updateArticle(articleId,email,processedContent);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("articleId",articleId);
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
            result.put("message", "文章更新失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 删除文章
     * 20241010
     * @param articleId
     * @return
     */
    @PostMapping("/deleteArticle")
    public ResponseEntity<Map<String,Object>> deleteArticle(@RequestParam("articleId") Integer articleId,
                                                            Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,String> map = articleService.updateArticleState(articleId,email);

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
            result.put("message", "删除文章失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示文章内容
     * 20240921
     * @param articleId
     * @return
     */
    @PostMapping("/showArticleContent")
    public ResponseEntity<Map<String,Object>> showArticleContent(@RequestParam("articleId") int articleId,
                                                                 Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 用户已登录

                Map<String,Object> map = articleService.showArticleContent(articleId);
                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("content",map.get("content"));//返回文章内容
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
            result.put("message", "显示文章内容失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示用户违规的回答
     * 20241106
     * @return
     */
    @GetMapping("/showIllegalArticle")
    public ResponseEntity<Map<String, Object>> showIllegalArticle(Principal principal) {

        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 判断用户是否已经登录
                // 从Service层获取搜索结果
                Map<String, Object> getResult = articleService.getIllegalArticle(email);
                if (getResult.containsValue("success")){ // 是否有返回问题列表
                    result.put("status","success");
                    result.put("illegalArticle",getResult.get("illegalArticle")); // 返回违规回答列表
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
            result.put("message", "显示用户违规回答失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }


}
