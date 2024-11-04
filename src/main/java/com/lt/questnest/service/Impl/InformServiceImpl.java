package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.Inform;
import com.lt.questnest.entity.InformView;
import com.lt.questnest.entity.User;
import com.lt.questnest.mapper.InformMapper;
import com.lt.questnest.mapper.InformViewMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.InformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InformServiceImpl implements InformService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    InformMapper informMapper;

    @Autowired
    InformViewMapper informViewMapper;

    // 插入通知信息
    public String add(Integer senderId,Integer receiverId,String body){

        Inform inform = new Inform();
        inform.setSenderId(senderId);
        inform.setReceiverId(receiverId);
        inform.setContent(body);
        inform.setSendTime(new Timestamp(System.currentTimeMillis()));
        Integer addResult = informMapper.add(inform);
        if (addResult > 0){
            return "success";
        }
        return "error";
    }

    // 统计未读消息数
    public Integer getRead(String email){

        User user = userMapper.getUserByEmail(email);
        Integer read = 0;
        read = informMapper.getRead(user.getId());
        return read;
    }

    // 获取消息，将未读消息置为0
    public List<Object> showInform(String email){

        Map<String,Object> result = new HashMap<>();
        List<Object> resultList = null;
        List<InformView> informs = informViewMapper.getInform(email);

        for (InformView inform : informs) {
            result.put("sender",inform.getSenderUsername());
            result.put("content",inform.getContent());

            Timestamp sendTimeFormat  = inform.getSendTime();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // 格式：年-月-日 时:分
            String sendTime = dateTimeFormat.format(sendTimeFormat);
            result.put("sendTime",sendTime);

            resultList.add(result);

        }
        return resultList;
    }
}
