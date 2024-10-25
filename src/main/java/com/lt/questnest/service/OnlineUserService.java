package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface OnlineUserService {

    void addOnlineUser(String email);

    void removeOnlineUser(String email);

    int getOnlineUserCount();

    Set<String> getOnlineUsers();
}
