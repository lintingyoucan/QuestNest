package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "inform")
public class Inform {

    private Integer informId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private Timestamp sendTime;
    private boolean read;
}
