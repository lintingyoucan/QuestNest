package com.lt.questnest.mapper;

import com.lt.questnest.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface UserMapper {

    int addUser(User user);

    User getUserByEmail(String email);

    User getUserById(@Param("id") Integer id);

    User getUserByIdIgnoreState(@Param("id") Integer id); // 如果是需要显示全部数据（显示问题，显示回答，显示评论，显示粉丝列表，显示关注列表等），无论用户是否注销，使用这个接口

    int updateUserState(String email);

    int updatePasswd(@Param("email") String email, @Param("password") String password);

    int updateUser(@Param("email") String email,@Param("username") String username,
                   @Param("gender") String gender,@Param("birthday") Date birthday);

    int uploadPicture(@Param("email") String email,@Param("headUrl") String headUrl);// @Param("headUrl") 指定 SQL 中的 #{headUrl}

    String findUsername(@Param("userId") int userId);
}
