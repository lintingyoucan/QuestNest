package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InformService {

    String add(String sender,String receiver,String body);

    int getRead(String account);

    List<Object> showInform(String account);
}
