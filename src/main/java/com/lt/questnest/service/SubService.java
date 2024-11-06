package com.lt.questnest.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SubService {

    List<String> getChannel();

    String add(String email);

    String delete(String email);

}

