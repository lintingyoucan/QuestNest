package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface UserTopicService {

    Map<String, Object> addConcernTopic(String email, List<Integer> topicIds);

    Map<String, Object> cancelConcernTopic(String email,Integer userTopicId);

    Map<String, Object> showTopic(String email);
}
