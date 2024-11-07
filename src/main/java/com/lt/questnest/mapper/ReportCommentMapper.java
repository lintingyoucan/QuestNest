package com.lt.questnest.mapper;

import com.lt.questnest.entity.ReportComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportCommentMapper {

    int add(ReportComment reportComment);

    int delete(@Param("reportCommentId") Integer reportCommentId);
}
