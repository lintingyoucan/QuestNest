package com.lt.questnest.service.Impl;

import com.lt.questnest.entity.Inform;
import com.lt.questnest.entity.InformView;
import com.lt.questnest.entity.User;
import com.lt.questnest.mapper.InformMapper;
import com.lt.questnest.mapper.InformViewMapper;
import com.lt.questnest.mapper.UserMapper;
import com.lt.questnest.service.InformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class InformServiceImpl implements InformService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    InformMapper informMapper;

    @Autowired
    InformViewMapper informViewMapper;

    private static final Logger logger = LoggerFactory.getLogger(InformServiceImpl.class);


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
    public Integer getUnreadNumber(String email){

        User user = userMapper.getUserByEmail(email);
        Integer read = 0;
        read = informMapper.getUnreadNumber(user.getId());
        return read;
    }

    // 获取消息，将未读消息置为0
    @Transactional
    public Map<String,Object> showInform(String email){

        Map<String,Object> result = new HashMap<>();

        // 取出用户ID
        User user = userMapper.getUserByEmail(email);
        // 将接收者的所有信息都置为已读状态，可能所有消息都已读，不必更新状态可能所有消息都已读，不必更新状态
        try {
            Integer updateResult = informMapper.updateRead(user.getId());
            logger.info("更新订阅消息状态结果:{}",updateResult);
        } catch (Exception e) {
            throw new RuntimeException("更新订阅消息状态失败");
        }

        List<InformView> informs = informViewMapper.getInform(email);
        if (informs == null || informs.isEmpty()){
            result.put("status","success");
            result.put("inform",new ArrayList<>());
            return result;
        }

        // 按sendTime进行排序，时间早的在前面：后面put的时候会将最晚的时间排在前面
        Collections.sort(informs, new Comparator<InformView>() {
            @Override
            public int compare(InformView inform1, InformView inform2) {
                // 将时间晚的排在前面，返回负值表示informs1更靠前，正值表示informs2更靠前
                return inform1.getSendTime().compareTo(inform2.getSendTime());
            }
        });

        // 数据结果列表
        List<Map<String, Object>> informList = new ArrayList<>();

        for (InformView inform : informs) {

            Map<String, Object> informItem = new HashMap<>();

            informItem.put("senderUsername",inform.getSenderUsername());
            informItem.put("senderHeadUrl","http://192.168.178.78:8080/images?email="+inform.getSenderEmail());
            informItem.put("content",inform.getContent());

            Timestamp sendTimeFormat  = inform.getSendTime();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // 格式：年-月-日 时:分
            String sendTime = dateTimeFormat.format(sendTimeFormat);
            informItem.put("sendTime",sendTime);

            informList.add(informItem);
        }

        logger.info("订阅消息列表:{}",informList);
        result.put("status","success");
        result.put("inform",informList);
        return result;
    }
}
