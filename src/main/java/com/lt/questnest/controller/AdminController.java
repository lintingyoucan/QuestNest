package com.lt.questnest.controller;

import com.lt.questnest.configuration.SecurityConfig;
import com.lt.questnest.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    @Autowired
    SecurityConfig securityConfig;

    /**
     * 管理员登录
     * 20241014
     * @param account
     * @param password
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginByPasswd(@RequestParam("account") String account,
                                                             @RequestParam("password") String password) {

        Map<String, Object> result = new HashMap<>();

        try {

            Map<String, Object> map = adminService.loginByPasswd(account,password);
            if (map.containsValue("success")) {
                // 生成token
                String token = securityConfig.generateToken(account,"ROLE_ADMIN");
                // 登录成功，返回 200 OK
                result.put("token",token);
                result.put("adminId",map.get("adminId"));
                result.put("status", "success");
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", map.get("msg"));
                // 登录失败，返回 400 Bad Request
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (Exception e) {

            result.put("status", "error");
            result.put("message", "服务器错误");
            // 服务器异常，返回 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

}
