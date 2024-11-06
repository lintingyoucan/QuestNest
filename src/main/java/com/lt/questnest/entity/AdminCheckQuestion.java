package com.lt.questnest.entity;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "admin_check_question")
public class AdminCheckQuestion {

    private Integer adminCheckQuestionId;
    private Integer questionId;
    private boolean check;
}
