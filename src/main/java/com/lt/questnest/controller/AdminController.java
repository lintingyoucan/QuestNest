package com.lt.questnest.controller;

import com.lt.questnest.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 管理员登录
     * 20241014
     * @param account
     * @param password
     * @param session
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginByPasswd(@RequestParam("account") String account,
                                                             @RequestParam("password") String password,
                                                             HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 检查用户是否已经登录
            String loginAccount = (String) session.getAttribute("account");
            if (loginAccount != null && !(loginAccount.isEmpty()) && loginAccount.equals(account)) {
                result.put("status", "error");
                result.put("message", "该账号已登录，请先退出！");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            Map<String, String> map = adminService.loginByPasswd(account,password);
            if (map.containsValue("success")) {
                result.put("status", "success");
                result.put("message", "登录成功");
                session.setAttribute("account",account); // 在会话中添加管理员account
                // 登录成功，返回 200 OK
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

    /**
     * 管理员登出系统
     * 20241014
     * @param session
     * @return
     */
    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            String account = (String) session.getAttribute("account");
            if (account != null && !account.isEmpty()) { // 用户已登录

                // 移除 session 中的用户信息
                session.removeAttribute("account");

                // 销毁 session
                session.invalidate();

                result.put("status", "success");
                result.put("message", "管理员已成功登出");
                // 返回 HTTP 200 OK
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", "管理员未登录");
                // 返回 HTTP 400 Bad Request
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            logger.info("登出异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "服务器错误，登出失败");
            // 返回 HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }



}
