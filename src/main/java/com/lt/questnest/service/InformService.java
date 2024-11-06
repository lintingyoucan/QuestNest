package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface InformService {

    String add(Integer senderId,Integer receiverId,String body);

    Integer getUnreadNumber(String email);

    Map<String,Object> showInform(String email);
}
