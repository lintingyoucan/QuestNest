package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RedisService {

    void setVerificationCode(String email,String code);

    String getVerificationCode(String email);

    void removeVerificationCode(String email);


}
