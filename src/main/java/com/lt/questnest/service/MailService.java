package com.lt.questnest.service;

import org.springframework.stereotype.Service;

@Service
public interface MailService {
    void sendVerificationCode(String to,String code);
}
