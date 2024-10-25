package com.lt.questnest.service.Impl;

import com.lt.questnest.service.OnlineUserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserServiceImpl implements OnlineUserService {

    private final ConcurrentHashMap<String, Boolean> onlineUsers = new ConcurrentHashMap<>();

    public void addOnlineUser(String email) {
        onlineUsers.put(email, true);
    }

    public void removeOnlineUser(String email) {
        onlineUsers.remove(email);
    }

    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers.keySet();
    }
}
