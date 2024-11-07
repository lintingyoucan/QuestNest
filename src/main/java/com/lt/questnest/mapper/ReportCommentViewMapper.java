package com.lt.questnest.mapper;

import com.lt.questnest.entity.ReportCommentView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportCommentViewMapper {

    List<ReportCommentView> getReportComment();
}
