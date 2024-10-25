package com.lt.questnest.mapper;

import com.lt.questnest.entity.Draft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DraftMapper {

    int addDraft(Draft draft);

    // 发布草稿
    int postDraft(@Param("draftId") Integer draftId);

    // 修改草稿
    int updateDraft(Draft draft);

    // 查找draftId是否存在
    Draft findByDraftId(@Param("draftId") Integer draftId);

    int deleteDraftByDraftId(@Param("draftId") Integer draftId);

    int deleteDraftByUserId(@Param("userId") Integer userId);

    List<Draft> findByUserId(@Param("userId") Integer userId);




}
