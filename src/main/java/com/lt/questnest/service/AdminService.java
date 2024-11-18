package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AdminService {

    Map<String, Object> loginByPasswd(String account, String password);

}
