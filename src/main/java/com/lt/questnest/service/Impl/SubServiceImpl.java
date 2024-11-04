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

    public String add(String email){
        Integer result = subMapper.add(email);
        if (result == null || result <= 0){
            return "error";
        }
        return "success";
    }

    public String delete(String email){
        Integer result = subMapper.delete(email);
        if (result == null || result <= 0){
            return "error";
        }
        return "success";
    }
}
