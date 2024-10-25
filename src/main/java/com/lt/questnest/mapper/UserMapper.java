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

    int updateUserState(String email);

    int updatePasswd(@Param("email") String email, @Param("password") String password);

    int updateUser(@Param("email") String email,@Param("username") String username,
                   @Param("gender") String gender,@Param("birthday") Date birthday);

    int uploadPicture(@Param("email") String email,@Param("headUrl") String headUrl);// @Param("headUrl") 指定 SQL 中的 #{headUrl}

    String findUsername(@Param("userId") int userId);
}
