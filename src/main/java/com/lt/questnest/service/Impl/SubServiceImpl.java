package com.lt.questnest.service.Impl;

import com.lt.questnest.mapper.SubMapper;
import com.lt.questnest.service.SubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubServiceImpl implements SubService {

    @Autowired
    SubMapper subMapper;

    public List<String> getChannel(){
        return subMapper.getChannel();
    }

    public String add(String account){
        Integer result = subMapper.add(account);
        if (result == null || result <= 0){
            return "error";
        }
        return "ok";
    }
}
