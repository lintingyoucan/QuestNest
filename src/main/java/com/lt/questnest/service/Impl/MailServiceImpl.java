package com.lt.questnest.service.Impl;

import com.lt.questnest.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    JavaMailSender mailSender;

    public void sendVerificationCode(String to,String code){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1370910756@qq.com");
        message.setTo(to);
        message.setSubject("Your Verification Code");
        message.setText("Your verification code is:"+code);
        mailSender.send(message);
    }
}
