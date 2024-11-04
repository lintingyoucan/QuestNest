package com.lt.questnest.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubMapper {

    int add(@Param("channel") String channel);

    List<String> getChannel();
}
