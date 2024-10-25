package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "question_topic")
public class QuestionTopic {
    private Integer questionId;  // 问题ID
    private Integer topicId;     // 话题ID
}
