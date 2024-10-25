package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface FavouriteItemService {

    Map<String, Object> collectArticle(String email,Integer favouriteId, Integer articleId);

    Map<String, Object> cancelCollect(String email,Integer favouriteItemId);

    Map<String, Object> showCollection(String email);
}
