package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Favourite;
import com.lt.questnest.entity.FavouriteItem;
import com.lt.questnest.mapper.FavouriteItemMapper;
import com.lt.questnest.mapper.FavouriteMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.FavouriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavouriteServiceImpl implements FavouriteService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    FavouriteMapper favouriteMapper;

    @Autowired
    FavouriteItemMapper favouriteItemMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    // 创建收藏夹
    public Map<String, Object> createFavourite(String email, String name) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (name == null || name.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "名称不能为空");
            return result;
        }

        try {
            // 获取收藏夹的作者
            Integer userId = userMapper.getUserByEmail(email).getId();
            // 检查用户是否已经创建该收藏夹
            Favourite favourite = favouriteMapper.findByBoth(userId,name);
            if (favourite != null){
                result.put("status","error");
                result.put("msg","该文件夹已存在");
                return result;
            }

            // 创建Favourite实例
            favourite = new Favourite();
            favourite.setUserId(userId);
            favourite.setName(name);

            // 保存数据到数据库
            Integer addResult = favouriteMapper.addFavourite(favourite);
            if (addResult == null || addResult <= 0) {
                result.put("status", "error");
                result.put("msg", "数据保存失败");
                return result;
            }

            // 将添加后收藏夹ID返回
            Integer favouriteId = favourite.getFavouriteId();
            logger.info("添加收藏夹后返回的ID：{}",favouriteId);
            result.put("favouriteId",favouriteId);

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }

    // 删除收藏夹
    @Transactional
    public Map<String, Object> deleteFavourite(String email, Integer favouriteId) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (favouriteId == null || favouriteId <= 0) {
            result.put("status", "error");
            result.put("msg", "收藏夹ID不能为空");
            return result;
        }

        try {

            Favourite favourite = favouriteMapper.findByFavouriteId(favouriteId);
            // 检查收藏夹是否存在
            if (favourite == null){
                result.put("status","error");
                result.put("msg","收藏夹不存在");
                return result;
            }

            // 获取收藏夹的作者
            Integer authorId = favourite.getUserId();
            // 获取用户ID
            Integer userId = userMapper.getUserByEmail(email).getId();
            // 检查用户ID和收藏夹作者是否一致
            if (authorId != userId){
                result.put("status","error");
                result.put("msg","非法操作");
                return result;
            }

            // 修改favourite的state=0
            Integer updateFavourite = favouriteMapper.deleteFavouriteByFavouriteId(favouriteId);
            if (updateFavourite == null || updateFavourite <= 0){
                result.put("status","error");
                result.put("msg","数据库操作失败");
                return result;
            }

            // 找出favouriteId对应的收藏项favouriteItem
            List<FavouriteItem> favouriteItems =  favouriteItemMapper.findByFavouriteId(favouriteId);
            // 修改对应favouriteItem的state = 0
            for (FavouriteItem favouriteItem : favouriteItems) {
                Integer updateFavouriteItem = favouriteItemMapper.deleteFavouriteItemByFavouriteItemId(favouriteItem.getFavouriteItemId());
                if (updateFavouriteItem == null || updateFavouriteItem <= 0){
                    result.put("status","error");
                    result.put("msg","数据库操作失败");
                    return result;
                }
            }

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }
}
