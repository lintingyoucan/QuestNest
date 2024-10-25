package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface FavouriteService {

    Map<String, Object> createFavourite(String email, String name);

    Map<String, Object> deleteFavourite(String email, Integer favouriteId);
}
