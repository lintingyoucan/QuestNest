package com.lt.questnest.controller;

import com.lt.questnest.service.AdminCheckArticleService;
import com.lt.questnest.service.AdminDeleteArticleService;
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
@RequestMapping("/admin")
public class AdminArticleController {

    @Autowired
    AdminDeleteArticleService adminDeleteArticleService;

    @Autowired
    AdminCheckArticleService adminCheckArticleService;

    private static final Logger logger = LoggerFactory.getLogger(AdminArticleController.class);


    /**
     * 20241024
     * 审核不通过文章
     * @param articleId
     * @param reason
     * @return
     */
    @PostMapping("/failToArticle")
    public ResponseEntity<Map<String, Object>> failToArticle(@RequestParam("articleId") Integer articleId,
                                                             @RequestParam("reason") String reason,
                                                             Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String account = principal.getName();
        logger.info("取出account:{}",account);
        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,String> map = adminDeleteArticleService.failToArticle(account,articleId,reason);

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
                result.put("message", "管理员未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "回答审核不通过失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 显示未审核文章
     * 20241106
     * @return
     */
    @GetMapping("/showUncheckArticle")
    public ResponseEntity<Map<String, Object>> showUncheckArticle(Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String account = principal.getName();
        logger.info("取出account:{}",account);        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,Object> map  = adminCheckArticleService.getArticle();

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("article",map.get("article")); // 返回回答列表
                    return ResponseEntity.ok(result);
                } else {
                    result.put("status","error");
                    result.put("message",map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态
                }
            } else {
                result.put("status", "error");
                result.put("message", "管理员未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401状态
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "查看未审核回答失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

}
