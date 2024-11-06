package com.lt.questnest.mapper;

import com.lt.questnest.entity.Inform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InformMapper {

    int add(Inform inform);

    int getUnreadNumber(@Param("receiverId") Integer receiverId);

    int updateRead(@Param("receiverId") Integer receiverId);




}
