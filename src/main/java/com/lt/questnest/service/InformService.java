package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InformService {

    String add(Integer senderId,Integer receiverId,String body);

    Integer getRead(String email);

    List<Object> showInform(String email);
}
