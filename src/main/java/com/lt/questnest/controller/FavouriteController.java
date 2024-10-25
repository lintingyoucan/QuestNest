package com.lt.questnest.controller;

import com.lt.questnest.service.FavouriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class FavouriteController {

    @Autowired
    FavouriteService favouriteService;


    /**
     * 创建收藏夹
     * 20241011
     * @param name
     * @param session
     * @return
     */
    @PostMapping("/createFavourite")
    public ResponseEntity<Map<String,Object>> createFavourite(@RequestParam("name") String name,
                                                       HttpSession session){
        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,Object> map = favouriteService.createFavourite(email,name);

                if (map.containsValue("success")){
                    result.put("status","success");
                    result.put("favouriteId",map.get("favouriteId"));// 返回收藏夹ID
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
            result.put("message", "创建文件夹失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }

    /**
     * 删除收藏夹
     * 20241012
     * @param favouriteId
     * @param session
     * @return
     */
    @PostMapping("/deleteFavourite")
    public ResponseEntity<Map<String,Object>> deleteFavourite(@RequestParam("favouriteId") Integer favouriteId,
                                                              HttpSession session){
        Map<String,Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !(email.isEmpty()) ){ // 用户已登录

                // 返回结果
                Map<String,Object> map = favouriteService.deleteFavourite(email,favouriteId);

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
            result.put("message", "删除文件夹失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }

    }
}
