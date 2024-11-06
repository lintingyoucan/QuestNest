package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
@Data
@Entity
@Table(name = "admin_check_article")
public class AdminCheckArticle {

    private Integer adminCheckArticleId;
    private Integer articleId;
    private boolean check;
}
