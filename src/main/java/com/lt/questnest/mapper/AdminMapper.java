package com.lt.questnest.mapper;

import com.lt.questnest.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {

    Admin findByAccount(@Param("account") String account);

}
