package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.*;
import com.lt.questnest.mapper.*;
import com.lt.questnest.pubsub.RedisMessageSubscriber;
import com.lt.questnest.service.*;
import com.lt.questnest.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    MailService mailService;

    @Autowired
    FileService fileService;

    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    DraftMapper draftMapper;

    @Autowired
    FavouriteMapper favouriteMapper;

    @Autowired
    HistoryMapper historyMapper;

    @Autowired
    UserTopicMapper userTopicMapper;

    @Autowired
    SubService subService;

    @Autowired
    RedisMessageSubscriber redisMessageSubscriber;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    /**
     * 注销用户账号，将 state 更新为 0
     * 将用户相关的内容都删除，比如会话、聊天信息、草稿、收藏夹、收藏项、浏览历史、关注话题、订阅消息
     * @param email 用户邮箱
     * @return 操作结果
     */
    @Override
    @Transactional
    public Map<String, String> delete(String email) {

        Map<String, String> result = new HashMap<>();

        try {
            // 获取用户id
            Integer userId = userMapper.getUserByEmail(email).getId();

            // 删除与用户有关的会话
            Integer deleteConversation = conversationMapper.deleteByUserId(userId);
            if (deleteConversation == null || deleteConversation < 0) {
                result.put("status", "error");
                result.put("msg", "用户注销失败，删除会话失败！");
                return result;
            }

            // 删除用户草稿
            Integer deleteDraft = draftMapper.deleteDraftByUserId(userId);
            if (deleteDraft == null || deleteDraft < 0) {
                result.put("status", "error");
                result.put("msg", "用户注销失败，删除用户草稿失败！");
                return result;
            }

            // 用户一定有“默认收藏夹”，删除收藏夹
            Integer deleteFavourite = favouriteMapper.deleteByUserId(userId);
            if (deleteFavourite == null || deleteFavourite <= 0){
                result.put("status", "error");
                result.put("msg", "用户注销失败，删除收藏夹失败！");
                return result;
            }

            // 删除浏览历史
            Integer deleteHistory = historyMapper.deleteByUserId(userId);
            if (deleteHistory == null || deleteHistory < 0){
                result.put("status", "error");
                result.put("msg", "用户注销失败，删除浏览记录失败！");
                return result;
            }

            // 删除用户是否有关注话题
            Integer deleteUserTopic = userTopicMapper.deleteByUserId(userId);
            if (deleteUserTopic == null || deleteUserTopic < 0){
                result.put("status", "error");
                result.put("msg", "用户注销失败，删除浏览记录失败！");
                return result;
            }

            // 取消用户监听频道
            String unsub = redisMessageSubscriber.unsubscribe(email);
            if (!(unsub.equals("success"))){
                result.put("status","error");
                result.put("msg","取消用户监听频道失败!");
                return result;
            }

            // 删除用户订阅消息频道
            String deleteSub = subService.delete(email);
            if (!(deleteSub.equals("success"))){
                result.put("status","error");
                result.put("msg","删除用户订阅信息失败!");
                return result;
            }

            // 将用户状态设置为注销（state = 0）
            Integer update = userMapper.updateUserState(email);

            if (update > 0) {
                result.put("status", "success");
                result.put("msg", "用户注销成功！");
                return result;
            } else {
                result.put("status", "error");
                result.put("msg", "用户注销失败，请稍后再试！");
                return result;
            }

        } catch (Exception e) {

            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

    }


    /**
     * 注册
     * @param email
     * @param username
     * @param password
     * @param inputCode
     * @return
     */
    @Override
    public Map<String, String> register(String email, String username, String password,String inputCode) {

        logger.info("进入register的service方法");
        Map<String,String> result = new HashMap<>();

        // 对传入参数判空处理
        if (email == null || email.isEmpty()){
            result.put("status", "error");
            result.put("msg","邮箱不能为空!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }
        if (username == null || username.isEmpty()){
            result.put("status", "error");
            result.put("msg","用户名不能为空!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }
        if (password == null || password.isEmpty()){
            result.put("status", "error");
            result.put("msg","密码不能为空!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }
        if (inputCode == null || inputCode.isEmpty()){
            result.put("status", "error");
            result.put("msg","验证码不能为空!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }


        // 从redis中取出验证码验证是否正确
        String code = redisService.getVerificationCode(email);
        if (code == null) {
            logger.info("redis中的验证码为:{}",code);
            logger.info("redis中的邮箱为:{}",email);
            result.put("status", "error");
            result.put("msg", "验证码已过期或不存在或邮箱不存在，请重新获取！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }
        if (!code.equals(inputCode)) {
            result.put("status", "error");
            result.put("msg", "验证码错误！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }

        // 判断用户是否存在
        User user = userMapper.getUserByEmail(email);
        if (user != null && user.isState()){//如果用户存在，没有注销
            result.put("status", "error");
            result.put("msg","用户已存在");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }

        // 注册
        user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(EncryptionUtil.encryptPassword(password));// 使用加密算法SHA-256

        Integer addResult = userMapper.addUser(user);

        if (addResult == null || addResult > 0) {
            result.put("status", "success");
            result.put("msg", "用户注册成功！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
        } else {
            result.put("status", "error");
            result.put("msg", "用户注册失败，请稍后再试！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
        }

        return result;
    }

    /**
     * 通过密码登录
     * @param email
     * @param password
     * @return
     */
    @Override
    public Map<String, String> loginByPasswd(String email, String password) {
        Map<String, String> result = new HashMap<>();

        // 判断传入参数是否为空
        if (email == null || email.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "邮箱不能为空!");
            return result;
        }
        if (password == null || password.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "密码不能为空!");
            return result;
        }

        // 判断用户是否存在
        User user = userMapper.getUserByEmail(email);
        if (user == null || !user.isState()) {
            result.put("status", "error");
            result.put("msg", "用户不存在!");
            return result;
        }

        // 判断密码是否正确
        String encryptPassword = EncryptionUtil.encryptPassword(password);
        if (!user.getPassword().equals(encryptPassword)) {
            result.put("status", "error");
            result.put("msg", "密码错误!");
            return result;
        }

        // 登录成功
        result.put("status", "success");
        result.put("msg", "用户登录成功！");
        return result;
    }

    /**
     * 通过验证码登录
     * @param email
     * @param inputCode
     * @return
     */
    public Map<String,String> loginByCode(String email,String inputCode){
        Map<String,String> result = new HashMap<>();

        // 对传入参数判空处理
        if (email == null || email.isEmpty()){
            result.put("status", "error");
            result.put("msg","邮箱不能为空!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }

        inputCode = inputCode.trim();// 去掉末尾的空格
        if (inputCode == null || inputCode.isEmpty()){
            result.put("status", "error");
            result.put("msg","验证码不能为空!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }

        // 判断用户是否存在
        User user = userMapper.getUserByEmail(email);
        if (user == null || !(user.isState())){
            result.put("status", "error");
            result.put("msg","用户不存在!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }

        // 从redis中取出验证码验证是否正确
        String code = redisService.getVerificationCode(email);
        if (code == null) {
            logger.info("redis中的验证码为:{}",code);
            logger.info("redis中的邮箱为:{}",email);
            result.put("status", "error");
            result.put("msg", "验证码已过期或不存在或邮箱不存在，请重新获取！");
            String redisCode = redisService.getVerificationCode(email);
            logger.info("redis中的code:{}",redisCode);
            logger.info("输入的code:{}",code);
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }
        if (!code.equals(inputCode)) {
            result.put("status", "error");
            result.put("msg", "验证码错误！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }

        // 登录成功
        result.put("status", "success");
        result.put("msg", "用户登录成功！");
        // 删除验证码 key
        redisService.removeVerificationCode(email);

        return result;
    }

    /**
     * 重置密码
     * @param email
     * @param password
     * @param inputCode
     * @return
     */
    public Map<String,String> resetPasswd(String email,String password,String inputCode){
        Map<String,String> result = new HashMap<>();

        // 对传入参数判空处理
        if (email == null || email.isEmpty()){
            result.put("status", "error");
            result.put("msg","邮箱不能为空!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }
        if (password == null || password.isEmpty()){
            result.put("status","error");
            result.put("msg","新密码不能为空");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }
        if (inputCode == null || inputCode.isEmpty()){
            result.put("status", "error");
            result.put("msg","验证码不能为空!");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }

        // 从redis中取出验证码验证是否正确
        String code = redisService.getVerificationCode(email);
        if (code == null) {
            logger.info("redis中的验证码为:{}",code);
            logger.info("redis中的邮箱为:{}",email);
            result.put("status", "error");
            result.put("msg", "验证码已过期或不存在或邮箱不存在，请重新获取！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }
        if (!code.equals(inputCode)) {
            result.put("status", "error");
            result.put("msg", "验证码错误！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
            return result;
        }

        // 判断用户是否存在
        User user = userMapper.getUserByEmail(email);
        if (user == null || !(user.isState())){
            result.put("status", "error");
            result.put("msg", "用户不存在！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
        }

        // 重置密码
        Integer update = userMapper.updatePasswd(email,EncryptionUtil.encryptPassword(password));
        if (update > 0) {
            result.put("status", "success");
            result.put("msg", "密码修改成功！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
        } else {
            result.put("status", "error");
            result.put("msg", "密码修改失败，请稍后再试！");
            // 删除验证码 key
            redisService.removeVerificationCode(email);
        }
        return result;
    }



    /**
     * 更新用户信息
     * @param email
     * @param username
     * @param gender
     * @param birthday
     * @return
     */
    public Map<String,String> updateUser(String email,String username,String gender,String birthday){

        Map<String,String> result = new HashMap<>();
        User user = userMapper.getUserByEmail(email);

        // 对username判空
        if (username == null || username.isEmpty()){
            result.put("status","error");
            result.put("msg","用户名不能为空!");
            return result;
        }
        // 对 gender判空
        if (gender == null || gender.isEmpty()){
            result.put("status","error");
            result.put("msg","性别不能为空");
        }

        // 将(String)birthday转换为(Date)birthday
        Date birthdayDate = null;
        // 定义日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // 将字符串转换为 Date 类型
            birthdayDate = dateFormat.parse(birthday);
            logger.info("转换后的 Date 对象:{} ",birthdayDate);
        } catch (ParseException e) {
            logger.info("日期格式错误: {}",e.getMessage());
            result.put("status","error");
            result.put("msg","日期格式错误");
            return result;
        }


        // 修改信息
        user.setUsername(username);
        user.setGender(gender);
        user.setBirthday(birthdayDate);

        Integer update = userMapper.updateUser(email,username,gender,birthdayDate);
        if (update > 0) {
            result.put("status", "success");
            result.put("msg", "用户信息修改成功！");
        } else {
            result.put("status", "error");
            result.put("msg", "用户信息修改失败，请稍后再试！");
        }

        return result;
    }

    /**
     * 显示个人信息
     * @param email
     * @return
     */
    public Map<String,Object> getUser(String email){

        // 结果
        Map<String,Object> result = new HashMap<>();
        // 数据
        Map<String,Object> userMap = new HashMap<>();

        // 检查用户是否存在
        User user = userMapper.getUserByEmail(email);
        if (user == null || !(user.isState())){
            result.put("status","error");
            result.put("msg","用户不存在");
            return result;
        }

        // 修改生日格式
        Date birthdayTime = user.getBirthday(); //2020-02-20T16:00:00.000+00:00
        // 创建日期格式化对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 格式化日期
        String birthday = sdf.format(birthdayTime);
        logger.info("修改格式后的birthday:{}",birthday);

        // 返回完整URL
        String headUrl = "http://localhost:8080"+user.getHeadUrl();
        logger.info("拼接后的headURL：{}",headUrl);
        // 保存数据
        userMap.put("email",email);
        userMap.put("username",user.getUsername());
        userMap.put("gender",user.getGender());
        userMap.put("birthday",birthday);
        userMap.put("headUrl",headUrl);

        // 返回
        result.put("status","success");
        result.put("user",userMap);
        return result;
    }


    /**
     * 上传头像
     * @param email
     * @param file
     * @return
     */
    public Map<String,String> uploadPicture(String email,MultipartFile file){

        logger.info("进入userService的方法uploadPicture");
        Map<String, String> result = fileService.uploadFile(file, "image");
        logger.info("退出fileService方法");
        if ("success".equals(result.get("status"))) {
            String headUrl = result.get("fileUrl");

            // 更新数据库中的头像URL
            try {
                Integer update = userMapper.uploadPicture(email, headUrl);
                if (update > 0) {
                    result.put("status", "success");
                    result.put("headUrl", headUrl);  // 返回头像的URL
                } else {
                    result.put("status", "error");
                    result.put("msg", "数据库更新失败");
                }
            } catch (Exception e) {
                result.put("status", "error");
                result.put("msg", "头像上传失败：" + e.getMessage());
            }
        }

        return result;

    }

}



