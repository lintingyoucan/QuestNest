package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "user_question_article_view")
public class UserQuestionArticleView {

    private Integer questionId;
    private String questionTitle;
    private String questionContent;
    private Integer articleId;
    private String articleContent;
    private String articleUsername;
    private String articleEmail;
}
