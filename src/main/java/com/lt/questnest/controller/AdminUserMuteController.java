package com.lt.questnest.controller;

import com.lt.questnest.service.AdminUserMuteService;
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
public class AdminUserMuteController {

    @Autowired
    AdminUserMuteService adminUserMuteService;

    private static final Logger logger = LoggerFactory.getLogger(AdminUserMuteController.class);


    /**
     * 用户禁言
     * 20241014
     * @param email
     * @param reason
     * @return
     */
    @PostMapping("/addMute")
    public ResponseEntity<Map<String, Object>> addMute(@RequestParam("email") String email,
                                                             @RequestParam("reason") String reason,
                                                       Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String account = principal.getName();
        logger.info("取出account:{}",account);
        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,Object> map = adminUserMuteService.addMute(account,email,reason);

                if (map.containsValue("success")){
                    result.put("status","success");
                    // 返回禁言ID
                    result.put("adminUserMuteId",map.get("adminUserMuteId"));
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
            result.put("message", "禁言用户添加失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 取消禁言
     * 20241014
     * @param email
     * @return
     */
    @PostMapping("/cancelMute")
    public ResponseEntity<Map<String, Object>> cancelMute(@RequestParam("email") String email,
                                                          Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String account = principal.getName();
        logger.info("取出account:{}",account);
        try {
            if (account != null && !(account.isEmpty()) ){ // 管理员已登录

                // 返回添加结果
                Map<String,Object> map = adminUserMuteService.cancelMute(email);

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
            result.put("message", "取消禁言用户添加失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

}
