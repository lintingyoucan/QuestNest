package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Admin;
import com.lt.questnest.entity.AdminUserMute;
import com.lt.questnest.entity.User;
import com.lt.questnest.mapper.AdminMapper;
import com.lt.questnest.mapper.AdminUserMuteMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.AdminUserMuteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminUserMuteServiceImpl implements AdminUserMuteService {

    @Autowired
    AdminUserMuteMapper adminUserMuteMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    AdminMapper adminMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 禁言用户
    public Map<String, Object> addMute(String account,String email, String reason) {

        Map<String,Object> result = new HashMap<>();

        // 对传入参数判空处理
        if (email == null || email.isEmpty()){
            result.put("status", "error");
            result.put("msg","用户不能为空!");
            return result;
        }
        if (reason == null || reason.isEmpty()){
            result.put("status", "error");
            result.put("msg","原因不能为空!");
            return result;
        }

        try {
            // 判断用户是否存在
            User user = userMapper.getUserByEmail(email);
            if (user == null || !(user.isState())){
                result.put("status", "error");
                result.put("msg","用户不存在!");
                return result;
            }

            // 判断是否已禁言该用户
            result = isMute(email);
            if (result.containsValue("error")){
                return result;
            }

            // 创建实例
            AdminUserMute adminUserMute = new AdminUserMute();
            adminUserMute.setAdminId(adminMapper.findByAccount(account).getAdminId());
            adminUserMute.setUserId(user.getId());
            adminUserMute.setReason(reason);

            // 保存数据
            Integer addResult = adminUserMuteMapper.add(adminUserMute);
            if (addResult == null || addResult <= 0){
                result.put("status","error");
                result.put("msg","保存数据失败");
                return result;
            }

            // 返回添加后的ID
            Integer adminUserMuteId = adminUserMute.getAdminUserMuteId();
            result.put("adminUserMuteId",adminUserMuteId);

        } catch (Exception e) {

            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        // 成功
        result.put("status", "success");
        return result;
    }


    // 检查用户是否被禁言
    public Map<String, Object> isMute(String email){

        logger.info("进入isMute方法");
        Map<String,Object> result = new HashMap<>();

        // 获取用户ID
        User user = userMapper.getUserByEmail(email);
        Integer userId = user.getId();

        // 检查是否禁言
        AdminUserMute adminUserMute = adminUserMuteMapper.findByUserId(userId);
        if (adminUserMute != null){
            result.put("status", "error");
            result.put("msg","用户已被禁言!");
            logger.info("退出isMute方法");
            return result;
        }

        result.put("status","success");
        result.put("msg","用户未被禁言");
        logger.info("退出isMute方法");
        return result;
    }

    // 取消禁言 cancelMute
    public Map<String, Object> cancelMute(String email) {

        logger.info("进入cancelMute方法");
        Map<String,Object> result = new HashMap<>();

        // 对传入参数判空处理
        if (email == null || email.isEmpty()){
            result.put("status", "error");
            result.put("msg","用户不能为空!");
            return result;
        }

        try {

            // 判断用户是否存在
            User user = userMapper.getUserByEmail(email);
            if (user == null || !(user.isState())){
                result.put("status", "error");
                result.put("msg","用户不存在!");
                return result;
            }

            // 判断是否已禁言该用户
            result = isMute(email);
            logger.info("从isMute方法来到cancel方法");
            if (result.containsValue("success")){ // 用户未被禁言
                result.remove("status","success");
                result.put("status","error");
                return result;
            }

            logger.info("删除记录前");
            // 删除记录，保存数据
            Integer deleteResult = adminUserMuteMapper.delete(userMapper.getUserByEmail(email).getId());
            if (deleteResult == null || deleteResult <= 0){
                result.put("status","error");
                result.put("msg","保存数据失败");
                return result;
            }
            logger.info("删除记录后");

        } catch (Exception e) {

            result.put("status","error");
            result.put("msg","数据库操作失败");
            return result;
        }

        // 成功
        result.put("status", "success");
        return result;
    }
}
