package com.lt.questnest.mapper;

import com.lt.questnest.entity.AdminUserMute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminUserMuteMapper {

    AdminUserMute findByUserId(@Param("userId") Integer userId);

    int add(AdminUserMute adminUserMute);

    int delete(@Param("userId") Integer userId);
}
