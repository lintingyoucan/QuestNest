package com.lt.questnest.service;

import com.lt.questnest.mapper.SubMapper;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public interface SubService {

    List<String> getChannel();

    String add(String email);

    String delete(String email);

}

