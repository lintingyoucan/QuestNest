package com.lt.questnest.controller;


import com.lt.questnest.service.AdminUserMuteService;
import com.lt.questnest.service.FileService;
import com.lt.questnest.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin
public class QuestionController {

    @Autowired
    QuestionService questionService;

    @Autowired
    FileService fileService;

    @Autowired
    AdminUserMuteService adminUserMuteService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 添加问题
     * 20240919
     * @param title
     * @param content
     * @param topics
     * @return
     */
    @PostMapping("/addQuestion")
    public ResponseEntity<Map<String, Object>> addQuestion(
            @RequestParam("title") String title,
            @RequestParam("content") String content,  // 可能包含占位符的文本内容
            @RequestParam("topic") Set<String> topics,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 图片文件
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos,  // 视频文件
            Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {
            if (email != null && !email.isEmpty()) {  // 判断用户是否登录

                // 检查用户是否被禁言
                Map<String,Object> isMute = adminUserMuteService.isMute(email);
                if (isMute.containsValue("error")){ // 用户已被禁言
                    result.put("status","error");
                    result.put("message",isMute.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                }

                // 处理 content，上传图片和视频，并替换占位符
                String processedContent = fileService.processContentAndMedia(content, images, videos);

                // 将处理后的内容和其他信息一起传递给业务层
                Map<String, Object> map = questionService.addQuestion(email, title, processedContent, topics);;
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    result.put("questionId",map.get("questionId")); // 返回添加后的问题ID
                    return ResponseEntity.ok(result);
                } else {
                    result.put("status", "error");
                    result.put("message", map.get("msg"));
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
     * 编辑问题
     * 20241011
     * @param questionId
     * @param title
     * @param content
     * @param topics
     * @param images
     * @param videos
     * @return
     */
    @PostMapping("/updateQuestion")
    public ResponseEntity<Map<String, Object>> updateQuestion(
            @RequestParam("questionId") Integer questionId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,  // 可能包含占位符的文本内容
            @RequestParam("topic") Set<String> topics,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 图片文件
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos,  // 视频文件
            Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);

        try {
            if (email != null && !(email.isEmpty())) { // 判断用户是否已经登录

                // 检查用户是否被禁言
                Map<String,Object> isMute = adminUserMuteService.isMute(email);
                if (isMute.containsValue("error")){ // 用户已被禁言
                    result.put("status","error");
                    result.put("message",isMute.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                }

                // 处理 content，上传图片和视频，并替换占位符
                String processedContent = fileService.processContentAndMedia(content, images, videos);

                // 从Service层获取搜索结果
                Map<String, String> updateResult = questionService.updateQuestion(questionId,email,title,processedContent,topics);
                if (updateResult.containsValue("success")) { // 是否有返回问题列表
                    result.put("status", "success");
                    return ResponseEntity.ok(result); // 返回200状态，并返回问题列表
                } else {
                    result.put("status", "error");
                    result.put("message", updateResult.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }

            } else {
                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "编辑问题失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }


    /**
     * 搜索问题：关键词
     * 20240920
     * @param keyword
     * @return 返回查到的问题
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("keyword") String keyword,
                                                      Principal principal) {

        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 判断用户是否已经登录
                // 从Service层获取搜索结果
                Map<String, Object> searchResult = questionService.search(keyword);
                if (searchResult.containsKey("questions")){ // 是否有返回问题列表
                    result.put("status","success");
                    result.put("questions",searchResult.get("questions"));
                    logger.info("controller中接收的question列表:{}",searchResult.get("questions"));
                    return ResponseEntity.ok(result); // 返回200状态，并返回问题列表
                } else {
                    result.put("status","error");
                    result.put("message",searchResult.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }

            } else {
                result.put("status","error");
                result.put("message","用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "搜索失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 利用AI回答搜索的问题
     * 20241025
     * @param keyword
     * @return
     */
    @PostMapping("/searchByAI")
    public ResponseEntity<Map<String, Object>> searchByAI(@RequestParam("keyword") String keyword,
                                                          Principal principal) {

        Map<String,Object> result = new HashMap<>();

        String email = principal.getName();
        logger.info("取出的email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 判断用户是否已经登录
                // 从Service层获取搜索结果
                Map<String, Object> searchResult = questionService.searchByAI(keyword);
                if (searchResult.containsValue("success")){ // 是否有返回问题列表
                    result.put("status","success");
                    result.put("content",searchResult.get("content"));
                    logger.info("controller中接收的content:{}",searchResult.get("content"));
                    return ResponseEntity.ok(result); // 返回200状态，并返回问题列表
                } else {
                    result.put("status","error");
                    result.put("message",searchResult.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }

            } else {
                result.put("status","error");
                result.put("message","用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "AI获取回答失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }


    /**
     * 显示用户违规的问题
     * 20241106
     * @return
     */
    @GetMapping("/showIllegalQuestion")
    public ResponseEntity<Map<String, Object>> showIllegalQuestion(Principal principal) {

        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);


        try {
            if (email != null && !(email.isEmpty())){ // 判断用户是否已经登录
                // 从Service层获取搜索结果
                Map<String, Object> getResult = questionService.getIllegalQuestion(email);
                if (getResult.containsValue("success")){ // 是否有返回问题列表
                    result.put("status","success");
                    result.put("illegalQuestion",getResult.get("illegalQuestion")); // 返回违规问题列表
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
            result.put("message", "显示用户违规问题失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 显示问题回答数
     * 20241106
     * @param title
     * @return
     */
    @PostMapping("/showAnswerNumber")
    public ResponseEntity<Map<String, Object>> sshowAnswerNumber(@RequestParam("title") String title,
                                                                 Principal principal) {

        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);


        try {
            if (email != null && !(email.isEmpty())){ // 判断用户是否已经登录
                // 从Service层获取搜索结果
                Integer answerNumber = questionService.getAnswerNumber(title);
                result.put("status","success");
                result.put("answerNumber",answerNumber);
                return ResponseEntity.ok(result); // 返回200状态
            } else {
                result.put("status","error");
                result.put("message","用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "显示问题回答数目失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 显示问题对应的回答
     * 20241107
     * @param questionId
     * @return
     */
    @PostMapping("/showQuestionAndArticle")
    public ResponseEntity<Map<String, Object>> showQuestionAndArticle(@RequestParam("questionId") Integer questionId,
                                                                      Principal principal) {

        Map<String,Object> result = new HashMap<>();

        String email = principal.getName();
        logger.info("取出的email:{}",email);

        try {
            if (email != null && !(email.isEmpty())){ // 判断用户是否已经登录
                // 从Service层获取搜索结果
                Map<String,Object> map = questionService.getQuestionAndArticle(questionId);
                if (map.containsValue("success")){
                    result.put("status","success");
                    if (map.containsKey("userQuestionArticle")){
                        result.put("userQuestionArticle",map.get("userQuestionArticle"));
                    }
                    result.put("questionId",map.get("questionId"));
                    result.put("questionTitle",map.get("questionTitle"));
                    result.put("questionContent",map.get("questionContent"));
                    return ResponseEntity.ok(result); // 返回200状态
                } else {
                    result.put("status","error");
                    result.put("message",map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }

            } else {
                result.put("status","error");
                result.put("message","用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "显示问题对应的回答失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

}
