package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Favourite;
import com.lt.questnest.entity.User;
import com.lt.questnest.entity.UserFollow;
import com.lt.questnest.mapper.UserFollowMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.UserFollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserFollowMapper userFollowMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 关注用户
    public Map<String, Object> addFollow(String email,Integer followedId) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (followedId == null) {
            result.put("status", "error");
            result.put("msg", "被关注者传入ID为空");
            return result;
        }

        try {
            // 检查followedId是否存在
            User followedUser = userMapper.getUserById(followedId);
            if (followedUser == null){
                result.put("status","error");
                result.put("msg","该用户不存在,关注失败");
                return result;
            }

            // 获取用户ID
            Integer userId = userMapper.getUserByEmail(email).getId();
            // 检查用户是否已经关注过该followedId
            UserFollow userFollow = userFollowMapper.findByBoth(userId,followedId);
            if (userFollow != null){
                result.put("status","error");
                result.put("msg","已关注该用户");
                return result;
            }

            // 创建UserFollow实例
            userFollow = new UserFollow();
            userFollow.setFollowerId(userId);
            userFollow.setFollowedId(followedId);

            // 保存数据到数据库
            Integer addResult = userFollowMapper.addFollow(userFollow);
            if (addResult == null || addResult <= 0) {
                result.put("status", "error");
                result.put("msg", "数据保存失败");
                return result;
            }

            // 将添加后ID返回
            Integer userFollowId = userFollow.getUserFollowId();
            logger.info("添加收藏夹后返回的ID：{}",userFollowId);
            result.put("userFollowId",userFollowId);

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }

    // 取消关注用户
    public Map<String, Object> cancelFollow(String email,Integer userFollowId) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (userFollowId == null) {
            result.put("status", "error");
            result.put("msg", "userFollowId为空");
            return result;
        }

        try {
            // 检查userFollowId是否存在
            UserFollow userFollow = userFollowMapper.findByUserFollowId(userFollowId);
            if (userFollow == null){
                result.put("status","error");
                result.put("msg","没有关注该用户，无法取消关注操作");
                return result;
            }

            // 检查取消关注的follower和登录是否为同一个人
            Integer userId = userMapper.getUserByEmail(email).getId();
            Integer followerId  = userFollow.getFollowerId();
            if (userId != followerId){
                result.put("status","error");
                result.put("msg","非法操作");
                return result;
            }

            // 修改state保存数据到数据库
            Integer updateResult = userFollowMapper.updateState(userFollowId);
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

    // 显示粉丝列表
    public Map<String, Object> showFan(String email) {

        Map<String, Object> result = new HashMap<>();

        // 获取用户ID
        Integer userId = userMapper.getUserByEmail(email).getId();
        try {
            // 数据结果列表
            List<Map<String, Object>> fanList = new ArrayList<>();

            // 找出粉丝follower
            List<UserFollow> userFollows = userFollowMapper.findByFollowedId(userId);
            logger.info("userFollows:{}",userFollows);
            if (userFollows == null || userFollows.isEmpty()){
                result.put("status","success");
                result.put("msg","暂无粉丝");
                return result;
            }

            // 找出userFollows中每一个followerId
            for (UserFollow userFollow : userFollows) {

                Map<String, Object> fanItem = new HashMap<>();

                // 获取关注者Id
                Integer followerId = userFollow.getFollowerId();
                // 获取关注者信息，将信息返回给前端，包括userId、username、headUrl
                User user = userMapper.getUserById(followerId);
                fanItem.put("userId",followerId);
                fanItem.put("username",user.getUsername());
                fanItem.put("headUrl",user.getHeadUrl());
                // 添加
                fanList.add(fanItem);
            }

            // 返回粉丝列表
            result.put("fan",fanList);

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }

    // 显示关注列表
    public Map<String, Object> showFollowed(String email) {

        Map<String, Object> result = new HashMap<>();

        // 获取用户ID
        Integer userId = userMapper.getUserByEmail(email).getId();
        try {
            // 数据结果列表
            List<Map<String, Object>> followedList = new ArrayList<>();

            // 找出被关注者followed
            List<UserFollow> userFollows = userFollowMapper.findByFollowerId(userId);
            logger.info("userFollows:{}",userFollows);
            if (userFollows == null || userFollows.isEmpty()){
                result.put("status","success");
                result.put("msg","暂无关注用户");
                return result;
            }

            // 找出userFollows中每一个followedId
            for (UserFollow userFollow : userFollows) {

                Map<String, Object> followedItem = new HashMap<>();

                // 获取被关注者Id
                Integer followedId = userFollow.getFollowedId();
                // 获取关注者信息，将信息返回给前端，包括userId、username、headUrl
                User user = userMapper.getUserById(followedId);
                followedItem.put("userId",followedId);
                followedItem.put("username",user.getUsername());
                followedItem.put("headUrl",user.getHeadUrl());
                // 添加
                followedList.add(followedItem);
            }

            // 返回粉丝列表
            result.put("followed",followedList);

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }
}
