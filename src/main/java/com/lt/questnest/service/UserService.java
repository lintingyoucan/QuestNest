package com.lt.questnest.service;

import com.lt.questnest.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public interface UserService {

    //注册
    Map<String,String> register(String email,String username,String password,String inputCode);

    // 通过密码登录
    Map<String, Object> loginByPasswd(String email, String password);

    //通过验证码登录
    Map<String,Object> loginByCode(String email,String inputCode);

    // 注销
    Map<String, String> delete(String email);

    // 重置密码
    Map<String,String> resetPasswd(String email,String password,String inputCode);

    // 修改个人信息
    Map<String,String> updateUser(String email,String username,String gender,String birthday);

    // 显示个人信息
    Map<String,Object> getUser(String email);

    // 上传头像
    Map<String,String> uploadPicture(String email, MultipartFile file);

    // 返回头像
    Map<String,String> getHeadUrl(String email);
}
