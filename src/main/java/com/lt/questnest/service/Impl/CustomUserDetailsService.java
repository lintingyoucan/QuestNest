package com.lt.questnest.service.Impl;

import com.lt.questnest.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.lt.questnest.entity.User user = userMapper.getUserByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        // Spring Security 自带的 User 类，用于将用户数据转换为 UserDetails
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER") // 可根据需要设置用户角色
                .build();
    }
}

