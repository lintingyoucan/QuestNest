package com.lt.questnest.mapper;

import com.lt.questnest.entity.History;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HistoryMapper {

    int addHistory(History history);

    List<History> findByUserId(@Param("userId") Integer userId);

    int deleteByUserId(@Param("userId") Integer userId);
}
