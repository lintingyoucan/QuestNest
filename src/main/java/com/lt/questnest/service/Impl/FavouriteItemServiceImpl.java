package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.entity.Favourite;
import com.lt.questnest.entity.FavouriteItem;
import com.lt.questnest.mapper.*;
import com.lt.questnest.service.FavouriteItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavouriteItemServiceImpl implements FavouriteItemService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    ArticleMapper articleMapper;

    @Autowired
    FavouriteMapper favouriteMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    FavouriteItemMapper favouriteItemMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 添加收藏
    public Map<String, Object> collectArticle(String email,Integer favouriteId,Integer articleId) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (favouriteId == null || favouriteId <= 0) {
            result.put("status", "error");
            result.put("msg", "收藏夹为空或无效");
            return result;
        }
        if (articleId == null || articleId <= 0) {
            result.put("status", "error");
            result.put("msg", "回答为空或无效");
            return result;
        }

        try {
            // 检查收藏夹是否存在
            Favourite favourite = favouriteMapper.findByFavouriteId(favouriteId);
            if (favourite == null){
                result.put("status","error");
                result.put("msg","收藏夹不存在");
                return result;
            }

            // 获取收藏夹的作者
            Integer authorId = favourite.getUserId();
            // 获取登录用户ID
            Integer userId = userMapper.getUserByEmail(email).getId();
            // 检查收藏夹的作者和用户是否一致
            if (authorId != userId){
                result.put("status","error");
                result.put("msg","非法操作");
                return result;
            }

            // 检查回答是否存在
            String content = articleMapper.findContent(articleId);
            if (content == null || content.isEmpty()){
                result.put("status","error");
                result.put("msg","回答不存在");
                return result;
            }

            // 检查用户是否已经收藏该回答
            FavouriteItem favouriteItem = favouriteItemMapper.findByBoth(favouriteId,articleId);
            if (favouriteItem != null){
                result.put("status","error");
                result.put("msg","该回答已收藏");
                return result;
            }

            // 创建FavouriteItem实例
            favouriteItem = new FavouriteItem();
            favouriteItem.setFavouriteId(favouriteId);
            favouriteItem.setArticleId(articleId);

            // 保存数据到数据库
            Integer addResult = favouriteItemMapper.addFavouriteItem(favouriteItem);
            if (addResult == null || addResult <= 0) {
                result.put("status", "error");
                result.put("msg", "数据保存失败");
                return result;
            }

            // 将添加后收藏夹ID返回
            Integer favouriteItemId = favouriteItem.getFavouriteItemId();
            logger.info("添加收藏后返回的ID：{}",favouriteItemId);
            result.put("favouriteItemId",favouriteItemId);

        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }

    // 取消收藏
    public Map<String, Object> cancelCollect(String email,Integer favouriteItemId) {

        Map<String, Object> result = new HashMap<>();
        // 判断参数是否为空
        if (favouriteItemId == null || favouriteItemId <= 0) {
            result.put("status", "error");
            result.put("msg", "收藏项为空或无效");
            return result;
        }

        try {
            // 检查收藏项是否存在
            FavouriteItem favouriteItem = favouriteItemMapper.findByFavouriteItemId(favouriteItemId);
            if (favouriteItem == null){
                result.put("status","error");
                result.put("msg","收藏过该回答");
                return result;
            }

            // 检查收藏项作者和登录用户是否为同一个人
            // 找出收藏作者
            Integer authorId = favouriteMapper.findByFavouriteId(favouriteItem.getFavouriteId()).getUserId();
            // 找出用户ID
            Integer userId = userMapper.getUserByEmail(email).getId();
            if (authorId != userId){
                result.put("status","error");
                result.put("msg","非法操作");
                return result;
            }

            // 修改state,保存数据到数据库
            Integer updateResult = favouriteItemMapper.deleteFavouriteItemByFavouriteItemId(favouriteItemId);
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

    // 显示收藏
    public Map<String, Object> showCollection(String email) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 找出用户ID
            Integer userId = userMapper.getUserByEmail(email).getId();
            // 根据userId找出用户收藏夹favouriteId,肯定存在，因为用户注册时会给用户创建默认收藏夹
            List<Favourite> favourites = favouriteMapper.findByUserId(userId);

            // 数据结果列表
            List<Map<String, Object>> collectionList = new ArrayList<>();

            for (Favourite favourite : favourites) {

                // 根据favouriteId找出用户收藏项favouriteItemId
                List<FavouriteItem> favouriteItems = favouriteItemMapper.findByFavouriteId(favourite.getFavouriteId());

                Map<String,Object> favouriteMap = new HashMap<>();
                favouriteMap.put("favouriteId", favourite.getFavouriteId());
                favouriteMap.put("favouriteName", favourite.getName());
                // 检查收藏项是否为空
                if (favouriteItems == null || favouriteItems.isEmpty()){
                    favouriteMap.put("items", new ArrayList<>()); // 返回空列表
                    collectionList.add(favouriteMap);
                    continue;
                }

                // 添加收藏项
                List<Map<String,Object>> itemList = new ArrayList<>();

                for (FavouriteItem favouriteItem : favouriteItems) {

                    Map<String, Object> itemMap = new HashMap<>();

                    // 根据favouriteItemId找出回答articleId
                    Integer articleId = favouriteItem.getArticleId();
                    // 根据articleId找出问题questionId
                    Integer questionId = articleMapper.findQuestionId(articleId);
                    // 找出回答内容
                    String content = articleMapper.findContent(articleId);
                    // 找出问题title
                    String title = questionMapper.findQuestionTitle(questionId);
                    // 重新整合数据：收藏项ID、收藏回答ID、回答内容，问题ID、问题title
                    itemMap.put("favouriteItemId", favouriteItem.getFavouriteItemId());
                    itemMap.put("articleId", articleId);
                    itemMap.put("articleContent", content);
                    itemMap.put("questionId", questionId);
                    itemMap.put("questionTitle", title);

                    itemList.add(itemMap);
                }

                favouriteMap.put("items",itemList);

                collectionList.add(favouriteMap);

            }

            // 将数据返回
            result.put("collection",collectionList);


        } catch (Exception e) {

            result.put("status", "error");
            result.put("msg", "数据库操作异常");
            return result;
        }

        result.put("status", "success");
        return result;
    }
}
