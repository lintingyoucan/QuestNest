package com.lt.questnest.entity;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "user")
public class User {

    private Integer id;
    private String email;
    private String username;
    private String password;
    private String gender;
    private Date birthday;
    private String headUrl;
    private boolean state;

}
