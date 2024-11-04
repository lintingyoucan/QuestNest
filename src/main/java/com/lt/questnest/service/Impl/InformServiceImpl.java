package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.Inform;
import com.lt.questnest.mapper.InformMapper;
import com.lt.questnest.service.InformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InformServiceImpl implements InformService {

    @Autowired
    InformMapper informMapper;

    // 插入信息
    public String add(String sender,String receiver,String body){
        Inform inform = new Inform();
        inform.setSender(sender);
        inform.setReceiver(receiver);
        inform.setContent(body);
        inform.setSendTime(new Timestamp(System.currentTimeMillis()));
        Integer addResult = informMapper.add(inform);
        if (addResult > 0){
            return "success";
        }
        return "error";
    }

    // 统计未读消息数
    public int getRead(String account){

        Integer read = 0;
        read = informMapper.getRead(account);
        return read;
    }

    // 获取消息，将未读消息置为0
    public List<Object> showInform(String account){

        Map<String,Object> result = new HashMap<>();
        List<Object> resultList = null;

        List<Inform> informs = informMapper.getInform(account);
        for (Inform inform : informs) {
            // 将read置为1
            Integer readResult = informMapper.updateRead(inform.getInformId());
            if (readResult > 0){
                result.put("sender",inform.getSender());
                result.put("sendTime",inform.getSendTime());
                result.put("content",inform.getContent());
                resultList.add(result);
            } else {
                resultList.add("error");
            }
        }
        return resultList;
    }
}
