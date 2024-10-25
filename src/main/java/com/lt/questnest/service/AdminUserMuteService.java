package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AdminUserMuteService {

    Map<String, Object> addMute(String account, String email, String reason);

    Map<String, Object> isMute(String email);

    Map<String, Object> cancelMute(String email);
}
