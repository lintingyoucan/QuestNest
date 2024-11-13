package com.lt.questnest.controller;

import com.lt.questnest.service.FavouriteItemService;
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
public class FavouriteItemController {

    @Autowired
    FavouriteItemService favouriteItemService;

    private static final Logger logger = LoggerFactory.getLogger(FavouriteItemController.class);


    /**
     * 收藏回答
     * 20241012
     * @param favouriteId
     * @param articleId
     * @return
     */
    @PostMapping("/collectArticle")
    public ResponseEntity<Map<String,Object>> collectArticle(@RequestParam("favouriteId") Integer favouriteId,
                                                             @RequestParam("articleId") Integer articleId,
                                                             Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,Object> map = favouriteItemService.collectArticle(email,favouriteId,articleId);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("favouriteItemId",map.get("favouriteItemId"));// 返回收藏项ID
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
            result.put("message", "收藏回答失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 取消收藏回答
     * 20241012
     * @param favouriteItemId
     * @return
     */
    @PostMapping("/cancelCollect")
    public ResponseEntity<Map<String,Object>> cancelCollect(@RequestParam("favouriteItemId") Integer favouriteItemId,
                                                            Principal principal){
        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,Object> map = favouriteItemService.cancelCollect(email,favouriteItemId);

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
            result.put("message", "取消收藏回答失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 显示用户收藏，包括收藏夹和回答
     * 20241012
     * @return
     */
    @GetMapping("/showCollection")
    public ResponseEntity<Map<String,Object>> showCollection(Principal principal){

        Map<String,Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出email:{}",email);
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,Object> map = favouriteItemService.showCollection(email);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("collection",map.get("collection"));// 返回收藏信息
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
            result.put("message", "显示收藏失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }
}
