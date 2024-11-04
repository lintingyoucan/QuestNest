package com.lt.questnest.mapper;

import com.lt.questnest.entity.Inform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InformMapper {

    int add(Inform inform);

    int getRead(@Param("receiver") String receiver);

    List<Inform> getInform(@Param("receiver") String receiver);

    int updateRead(@Param("informId") Integer informId);


}
