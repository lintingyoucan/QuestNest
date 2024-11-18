package com.lt.questnest.controller;

import com.lt.questnest.configuration.SecurityConfig;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.pubsub.RedisMessageSubscriber;
import com.lt.questnest.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;
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

    @Autowired
    RedisMessageSubscriber redisMessageSubscriber;

    @Autowired
    SubService subService;

    @Autowired
    private SecurityConfig securityConfig;


    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserMapper userMapper;
    private final String imagePath = "E:\\code\\zhangJava\\files\\images\\";

    private final String videoPath = "E:\\code\\zhangJava\\files\\videos\\";

    /**
     * 显示用户头像
     * 20241110
     *
     * @return
     */
    @GetMapping("/images")
    public ResponseEntity<Resource> getImage(@RequestParam("email") String email) {

        Map<String, String> map = userService.getHeadUrl(email);

        try {
            String imageName = map.get("fileName");
            if (imageName != null && !(imageName.isEmpty())) {
                File file = new File(imagePath + imageName);
                if (!file.exists()) {
                    return ResponseEntity.notFound().build();
                }

                Resource resource = new org.springframework.core.io.FileSystemResource(file);
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getName());

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.IMAGE_PNG) // 如果是 PNG 图片
                        .body(resource);
            }
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("/videos")
    public ResponseEntity<Resource> getVideo(@RequestParam("videoName") String videoName) {
        try {
            File file = new File(videoPath + videoName);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new org.springframework.core.io.FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 上传头像
     * 20240916
     *
     * @param file
     * @return
     */
    @PostMapping("/uploadPicture")
    public ResponseEntity<Map<String, Object>> uploadPicture(@RequestParam("file") MultipartFile file,
                                                             Principal principal) {
        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
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
     * @return
     */
    @GetMapping("/getUser")
    public ResponseEntity<Map<String, Object>> getUser(Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);
        try {
            if (email != null && !email.isEmpty()) { // 用户已登录
                Map<String, Object> map = userService.getUser(email);

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
     * @return
     */
    @PostMapping("/updateUser")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestParam("username") String username,
                                                          @RequestParam("gender") String gender,
                                                          @RequestParam("birthday") String birthday,
                                                          Principal principal) {

        Map<String, Object> result = new HashMap<>();
        String email = principal.getName();
        logger.info("取出的email:{}",email);

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
     * @return 注销操作结果
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(Principal principal) {

        Map<String, Object> result = new HashMap<>();

        try {
            String email = principal.getName();
            logger.info("取出的email:{}", email);

            // 调用业务层注销用户账号
            Map<String, String> map = userService.delete(email);

            // 移除在线用户
            onlineUserService.removeOnlineUser(email);

            // 如果注销成功，清除会话
            if (map.containsValue("success")) {
                // 返回注销成功的消息和状态码 200 OK
                result.put("status", "success");
                result.put("message", map.get("msg"));
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                // 返回业务层处理失败的消息和状态码 400 Bad Request
                result.put("status", "error");
                result.put("message", map.get("msg"));
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            // 记录异常并返回服务器错误的消息和状态码 500 Internal Server Error
            logger.info("注销账号异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "注销账号异常: " + e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 密码登录
     * 20240915
     *
     * @param email
     * @param password
     * @return
     */
    @PostMapping("/loginByPasswd")
    public ResponseEntity<Map<String, Object>> loginByPasswd(@RequestParam("email") String email,
                                                             @RequestParam("password") String password) {

        Map<String, Object> result = new HashMap<>();

        try {

            Map<String, Object> map = userService.loginByPasswd(email, password);
            if (map.containsValue("success")) {

                // 生成JWT Token
                String token = securityConfig.generateToken(email, "ROLE_USER");
                result.put("status", "success");
                result.put("userId",map.get("userId"));
                result.put("token", token); // 返回token

                // 记录在线用户
                onlineUserService.addOnlineUser(email);

                // 返回 HTTP 200 OK
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", map.get("msg"));
                // 返回 HTTP 400 Bad Request，错误信息在 result 中
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (Exception e) {
            logger.info("验证码登录异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "验证码登录异常: " + e.getMessage());
            // 返回 HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 验证码登录
     * 20240915
     *
     * @param email
     * @param inputCode
     * @return
     */
    @PostMapping("/loginByCode")
    public ResponseEntity<Map<String, Object>> loginByCode(@RequestParam("email") String email,
                                                           @RequestParam("inputCode") String inputCode) {

        Map<String, Object> result = new HashMap<>();

        try {

            Map<String, Object> map = userService.loginByCode(email, inputCode);
            if (map.containsValue("success")) {

                // 生成JWT Token
                String token = securityConfig.generateToken(email, "ROLE_USER");
                result.put("status", "success");
                result.put("userId",map.get("userId"));
                result.put("token", token); // 返回token

                // 记录在线用户
                onlineUserService.addOnlineUser(email);

                // 返回 HTTP 200 OK
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", map.get("msg"));
                // 返回 HTTP 400 Bad Request，错误信息在 result 中
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (Exception e) {
            logger.info("验证码登录异常: " + e.getMessage());
            result.put("status", "error");
            result.put("message", "验证码登录异常: " + e.getMessage());
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

                // 订阅用户关于自己的消息
                String subByRedis = redisMessageSubscriber.subscribe(email);
                String subByDB = subService.add(email);
                if (subByRedis.equals("success") && subByDB.equals("success")) {
                    result.put("status", "success");
                    // result.put("message", "订阅消息成功,注册成功");
                    logger.info("userController中调用查看订阅的人:{}", RedisMessageSubscriber.emitters.get(email));
                    // 返回 HTTP 200 OK
                    return ResponseEntity.ok(result);
                } else {
                    result.put("status", "error");
                    result.put("message", "订阅消息失败");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                }

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
            response.put("message", "发送验证码异常: " + e.getMessage());
            // 返回 HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
