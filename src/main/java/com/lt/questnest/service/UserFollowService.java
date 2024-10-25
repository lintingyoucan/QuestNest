package com.lt.questnest.service;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface UserFollowService {

    Map<String, Object> addFollow(String email, Integer followedId);

    Map<String, Object> cancelFollow(String email,Integer userFollowId);

    Map<String, Object> showFan(String email);

    Map<String, Object> showFollowed(String email);

}
