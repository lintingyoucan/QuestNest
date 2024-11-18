package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Admin;
import com.lt.questnest.mapper.AdminMapper;
import com.lt.questnest.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    AdminMapper adminMapper;

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);


    // 登录
    public Map<String, Object> loginByPasswd(String account, String password) {

        Map<String,Object> result = new HashMap<>();

        // 对传入参数判空处理
        if (account == null || account.isEmpty()){
            result.put("status", "error");
            result.put("msg","账号不能为空!");
            return result;
        }
        if (password == null || password.isEmpty()){
            result.put("status", "error");
            result.put("msg","密码不能为空!");
            return result;
        }

        // 判断用户是否存在
        Admin admin = adminMapper.findByAccount(account);
        logger.info("admin:{}",admin);
        if (admin == null || !(admin.isState())){
            result.put("status", "error");
            result.put("msg","管理员不存在!");
            return result;
        }

        // 判断密码是否正确
        if (!((admin.getPassword()).equals(password))){
            result.put("status", "error");
            result.put("msg","密码错误!");
            return result;
        }


        // 登录成功
        result.put("status", "success");
        result.put("adminId",admin.getAdminId());

        return result;
    }
}
