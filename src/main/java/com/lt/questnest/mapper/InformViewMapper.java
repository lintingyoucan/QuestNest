package com.lt.questnest.mapper;

import com.lt.questnest.entity.InformView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InformViewMapper {

    List<InformView> getInform(@Param("receiverEmail") String receiverEmail);


}
