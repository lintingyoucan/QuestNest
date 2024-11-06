package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.UserTopic;
import com.lt.questnest.mapper.TopicMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.mapper.UserTopicMapper;
import com.lt.questnest.service.UserTopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserTopicServiceImpl implements UserTopicService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserTopicMapper userTopicMapper;

    @Autowired
    TopicMapper topicMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 添加关注话题
    public Map<String, Object> addConcernTopic(String email, List<Integer> topicIds) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (topicIds == null || topicIds.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "传入话题ID为空");
            return result;
        }

        try {
            // 数据结果列表
            List<Map<String, Object>> userTopicList = new ArrayList<>();

            // 获取用户ID
            Integer userId = userMapper.getUserByEmail(email).getId();

            // 遍历每一个话题
            for (Integer topicId : topicIds) {

                Map<String, Object> userTopicItem = new HashMap<>();
                // 检查topicId是否存在
                String topicName = topicMapper.findByTopicId(topicId);
                if (topicName == null){
                    userTopicItem.put("topicId", topicId);
                    userTopicItem.put("topicName", null);
                    userTopicItem.put("msg", "该话题不存在，关注失败");
                    // 添加
                    userTopicList.add(userTopicItem);
                    continue;
                }

                // 检查用户是否已经关注过该topicId
                UserTopic userTopic = userTopicMapper.findByBoth(userId,topicId);
                if (userTopic != null){
                    userTopicItem.put("topicId", topicId);
                    userTopicItem.put("topicName", topicName);
                    userTopicItem.put("msg", "已关注该话题");
                    // 添加
                    userTopicList.add(userTopicItem);
                    continue;
                }

                // 创建UserTopic实例
                userTopic = new UserTopic();
                userTopic.setUserId(userId);
                userTopic.setTopicId(topicId);

                // 保存数据到数据库
                Integer addResult = userTopicMapper.addUserTopic(userTopic);
                if (addResult == null || addResult <= 0) {
                    userTopicItem.put("topicId", topicId);
                    userTopicItem.put("topicName", topicName);
                    userTopicItem.put("msg", "数据保存失败");
                    // 添加
                    userTopicList.add(userTopicItem);
                    continue;
                }

                // 将添加后ID返回
                Integer userTopicId = userTopic.getUserTopicId();
                logger.info("添加后返回的ID：{}",userTopicId);
                userTopicItem.put("userTopicId", userTopicId);
                userTopicItem.put("topicId", topicId);
                userTopicItem.put("topicName", topicName);
                userTopicItem.put("msg", "关注成功");
                // 添加
                userTopicList.add(userTopicItem);
            }

            // 返回处理结果
            result.put("status", "success");
            result.put("userTopic", userTopicList);


        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        return result;
    }

    // 取消关注话题
    public Map<String, Object> cancelConcernTopic(String email,Integer userTopicId) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (userTopicId == null) {
            result.put("status", "error");
            result.put("msg", "userTopicId为空");
            return result;
        }

        try {
            // 检查userTopicId是否存在
            UserTopic userTopic = userTopicMapper.findByUserTopicId(userTopicId);
            if (userTopic == null){
                result.put("status","error");
                result.put("msg","没有关注该话题，无法取消关注操作");
                return result;
            }

            // 检查取消关注的话题和登录是否为同一个人
            Integer userId = userMapper.getUserByEmail(email).getId();
            Integer concernUserId = userTopic.getUserId();
            if (userId != concernUserId){
                result.put("status","error");
                result.put("msg","非法操作");
                return result;
            }

            // 修改state保存数据到数据库
            Integer updateResult = userTopicMapper.deleteByUserTopicId(userTopicId);
            if (updateResult == null || updateResult <= 0) {
                result.put("status", "error");
                result.put("msg", "数据保存失败");
                return result;
            }

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }

    // 显示关注话题列表
    public Map<String, Object> showTopic(String email) {

        Map<String, Object> result = new HashMap<>();

        // 获取用户ID
        Integer userId = userMapper.getUserByEmail(email).getId();
        try {
            // 数据结果列表
            List<Map<String, Object>> topicList = new ArrayList<>();

            // 找出被关注话题
            List<UserTopic> userTopics = userTopicMapper.findByUserId(userId);
            logger.info("userTopics:{}",userTopics);
            if (userTopics == null || userTopics.isEmpty()){
                result.put("status","success");
                result.put("msg","暂无关注话题");
                return result;
            }

            // 找出话题名称
            for (UserTopic userTopic : userTopics) {

                Map<String, Object> topicItem = new HashMap<>();

                // 获取被关注话题Id
                Integer topicId = userTopic.getTopicId();
                // 获取话题信息，将信息返回给前端，包括userTopicId、topicId、name
                String topicName = topicMapper.findByTopicId(topicId);
                topicItem.put("userTopicId",userTopic.getUserTopicId());
                topicItem.put("topicId",topicId);
                topicItem.put("name",topicName);
                // 添加
                topicList.add(topicItem);
            }

            // 返回话题列表
            result.put("status", "success");
            result.put("topic",topicList);

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        return result;
    }


}
