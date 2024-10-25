package com.lt.questnest.controller;

import com.lt.questnest.entity.User;
import com.lt.questnest.service.MailService;
import com.lt.questnest.service.OnlineUserService;
import com.lt.questnest.service.RedisService;
import com.lt.questnest.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@RestController
@CrossOrigin
public class UserController {

    @Autowired
    RedisService redisService;

    @Autowired
    MailService mailService;

    @Autowired
    UserService userService;

    @Autowired
    OnlineUserService onlineUserService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    /**
     * 上传头像
     * 20240916
     * @param file
     * @param session
     * @return
     */
    @PostMapping("/uploadPicture")
    public ResponseEntity<Map<String, Object>> uploadPicture(@RequestParam("file") MultipartFile file,
                                                             HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {

            if (email != null && !email.isEmpty()) { // 用户已登录

                Map<String, String> map = userService.uploadPicture(email, file);
                if (map.containsValue("success")) {
                    result.put("status", "success");
                    result.put("headUrl", map.get("headUrl"));  // 返回头像的URL，前端可以根据这个显示图片
                    return ResponseEntity.ok(result);  // 返回200 OK状态
                } else {
                    result.put("status", "error");
                    result.put("message", map.get("msg"));
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);  // 返回400状态，表示请求有误
                }

            } else {

                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);  // 返回401未授权状态

            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "头像上传失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);  // 返回500状态
        }
    }

    /**
     * 返回用户信息
     * 20240915
     *
     * @param session
     * @return
     */
    @GetMapping("/getUser")
    public ResponseEntity<Map<String, Object>> getUser(HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");
        try {
            if (email != null && !email.isEmpty()) { // 用户已登录
                Map<String,Object> map = userService.getUser(email);

                if (map.containsValue("success")) {
                    result.put("status", "success");
                    result.put("user", map.get("user")); // 将用户信息放入返回体
                    return ResponseEntity.ok(result); // 返回 HTTP 200 OK
                } else {
                    result.put("status", "error");
                    result.put("message", map.get("msg"));
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result); // 返回 HTTP 404 Not Found
                }
            } else {
                result.put("status", "error");
                result.put("message", "用户未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result); // 返回 HTTP 401 Unauthorized
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "服务器错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result); // 返回 HTTP 500 Internal Server Error
        }
    }

    /**
     * 修改用户信息
     * 20240915
     *
     * @param username
     * @param gender
     * @param birthday
     * @param session
     * @return
     */
    @PostMapping("/updateUser")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestParam("username") String username,
                                                          @RequestParam("gender") String gender,
                                                          @RequestParam("birthday") String birthday,
                                                          HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String email = (String) session.getAttribute("email");

        try {
            // 用户已登录
            if (email != null && !email.isEmpty()) {
                Map<String, String> map = userService.updateUser(email, username, gender, birthday);

                if (map.containsValue("success")) {
                    result.put("status", "success");
                    result.put("message", "用户信息修改成功");
                    // 返回 HTTP 200 OK
                    return ResponseEntity.ok(result);
                } else {
                    result.put("status", "error");
                    result.put("message", map.get("msg"));
                    // 返回 HTTP 400 Bad Request
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                }

            } else {
                logger.info("用户尚未登录");
                result.put("status", "error");
                result.put("message", "用户未登录，无法修改个人信息");
                // 返回 HTTP 401 Unauthorized
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

        } catch (Exception e) {
            logger.info("用户信息修改异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "服务器错误");
            // 返回 HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 通过email重置密码
     * 20240915
     *
     * @param email
     * @param password
     * @param inputCode
     * @return
     */
    @PostMapping("/resetPasswd")
    public ResponseEntity<Map<String, Object>> resetPasswd(@RequestParam("email") String email,
                                                           @RequestParam("password") String password,
                                                           @RequestParam("inputCode") String inputCode) {

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, String> map = userService.resetPasswd(email, password, inputCode);

            if (map.containsValue("success")) {
                result.put("status", "success");
                result.put("message", "密码修改成功");
                // 返回 HTTP 200 OK
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", map.get("msg"));
                // 返回 HTTP 400 Bad Request
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (Exception e) {
            logger.info("密码修改异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "服务器错误");
            // 返回 HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 注销用户账号
     * 20240915
     *
     * @param session 当前会话
     * @return 注销操作结果
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 从 session 中获取当前登录用户的 email
            String email = (String) session.getAttribute("email");

            // 检查用户是否已登录
            if (email != null && !email.isEmpty()) {
                // 调用业务层注销用户账号
                Map<String, String> serviceResult = userService.delete(email);

                // 移除在线用户
                onlineUserService.removeOnlineUser(email);

                // 如果注销成功，清除会话
                if ("success".equals(serviceResult.get("status"))) {
                    session.removeAttribute("email");
                    session.invalidate();

                    // 返回注销成功的消息和状态码 200 OK
                    result.put("status", "success");
                    result.put("message", serviceResult.get("msg"));
                    return new ResponseEntity<>(result, HttpStatus.OK);
                } else {
                    // 返回业务层处理失败的消息和状态码 400 Bad Request
                    result.put("status", "error");
                    result.put("message", serviceResult.get("msg"));
                    return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
                }
            } else {
                // 返回用户未登录的消息和状态码 401 Unauthorized
                result.put("status", "error");
                result.put("message", "用户未登录，无法注销账号");
                return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            // 记录异常并返回服务器错误的消息和状态码 500 Internal Server Error
            logger.info("注销账号异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "服务器错误");
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 用户登出
     * 20240915
     *
     * @param session
     * @return
     */
    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            String email = (String) session.getAttribute("email");
            if (email != null && !email.isEmpty()) { // 用户已登录

                // 移除在线用户
                onlineUserService.removeOnlineUser(email);

                // 移除 session 中的用户信息
                session.removeAttribute("email");

                // 销毁 session
                session.invalidate();

                result.put("status", "success");
                result.put("message", "用户已成功登出");
                // 返回 HTTP 200 OK
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", "用户未登录");
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


    /**
     * 公共登录处理逻辑
     * 20240915
     *
     * @param map     登录结果信息
     * @param session HttpSession
     * @param email   用户邮箱
     * @return 登录结果
     */
    private Map<String, Object> handleLogin(Map<String, String> map, HttpSession session, String email) {
        Map<String, Object> result = new HashMap<>();
        if (map.containsValue("success")) {
            result.put("status", "success");
            result.put("message", "登录成功");
            session.setAttribute("email", email);
            // 记录在线用户
            onlineUserService.addOnlineUser(email);
        } else {
            result.put("status", "error");
            result.put("message", map.get("msg"));
        }
        return result;
    }



    /**
     * 密码登录
     * 20240915
     *
     * @param email
     * @param password
     * @param session
     * @return
     */
    @PostMapping("/loginByPasswd")
    public ResponseEntity<Map<String, Object>> loginByPasswd(@RequestParam("email") String email,
                                                             @RequestParam("password") String password,
                                                             HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 检查用户是否已经登录
            String loginEmail = (String) session.getAttribute("email");
            if (loginEmail != null && loginEmail.equals(email)) {
                result.put("status", "error");
                result.put("message", "该账号已登录，请先退出！");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            Map<String, String> map = userService.loginByPasswd(email, password);

            if (map.containsValue("success")) {
                result.put("status", "success");
                result.put("message", "登录成功");
                session.setAttribute("email", email);
                logger.info("HttpSession中存储的email:{}",session.getAttribute("email"));
                // 记录在线用户
                onlineUserService.addOnlineUser(email);
                // 登录成功，返回 200 OK
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", map.get("msg"));
                // 登录失败，返回 400 Bad Request
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (Exception e) {
            logger.info("密码登录异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "服务器错误");
            // 服务器异常，返回 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 验证码登录
     * 20240915
     *
     * @param email
     * @param inputCode
     * @param session
     * @return
     */
    @PostMapping("/loginByCode")
    public ResponseEntity<Map<String, Object>> loginByCode(@RequestParam("email") String email,
                                                           @RequestParam("inputCode") String inputCode,
                                                           HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 检查用户是否已经登录
            String loginEmail = (String) session.getAttribute("email");
            if (loginEmail != null && loginEmail.equals(email)) {
                result.put("status", "error");
                result.put("message", "该账号已登录，请先退出！");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            Map<String, String> map = userService.loginByCode(email, inputCode);
            result = handleLogin(map, session, email);

            if (result.get("status").equals("success")) {
                // 返回 HTTP 200 OK
                return ResponseEntity.ok(result);
            } else {
                // 返回 HTTP 400 Bad Request，错误信息在 result 中
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (Exception e) {
            logger.info("验证码登录异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "服务器错误");
            // 返回 HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 注册
     * 20240914
     *
     * @param username
     * @param password
     * @param email
     * @param inputCode
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("inputCode") String inputCode) {

        logger.info("进入register的controller方法");

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用服务层的注册逻辑
            Map<String, String> map = userService.register(email, username, password, inputCode);

            // 如果注册成功
            if (map.containsValue("success")) {
                result.put("status", "success");
                result.put("message", "注册成功");
                // 返回 HTTP 200 OK
                return ResponseEntity.ok(result);
            } else {
                // 如果注册失败，返回错误信息
                result.put("status", "error");
                result.put("message", map.get("msg"));
                // 返回 HTTP 400 Bad Request
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (Exception e) {
            // 处理异常情况
            logger.info("注册异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "服务器错误");
            // 返回 HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 发送验证码
     * 20240914
     *
     * @param email
     * @return
     */
    @PostMapping("/sendCode")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 生成验证码
            String code = String.format("%06d", new Random().nextInt(999999));

            // 存储到 Redis 并发送邮件
            redisService.setVerificationCode(email, code); // 验证码5分钟有效
            mailService.sendVerificationCode(email, code);

            // 返回成功的 JSON 响应
            response.put("status", "success");
            response.put("message", "Verification code sent!");
            // 返回 HTTP 200 OK
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 捕获异常并返回服务器错误
            logger.info("发送验证码异常: " + e.getMessage());
            response.put("status", "error");
            response.put("message", "服务器错误");
            // 返回 HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
