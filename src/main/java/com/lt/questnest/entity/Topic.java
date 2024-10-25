package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "topic")
public class Topic {
    private Integer topicId;      // 话题ID
    private String name;      // 话题名称
}
