package com.lt.questnest.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "comment_area_mute")
public class CommentAreaMute {

    private int commentAreaMuteId; // admin_comment_area_mute_id
    private int articleId;               // article_id
    private Timestamp muteTime;          // mute_time
}
