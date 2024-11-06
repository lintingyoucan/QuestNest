package com.lt.questnest.mapper;

import com.lt.questnest.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {

    int addArticle(Article article);

    int addLike(@Param("articleId") int articleId);

    int reduceLike(@Param("articleId") int articleId);

    int addDislike(@Param("articleId") int articleId);

    int reduceDislike(@Param("articleId") int articleId);

    // 返回点赞人数
    int like(@Param("articleId") int articleId);

    // 找出文章对应的问题
    Integer findQuestionId(@Param("articleId") int articleId);

    // 找出文章作者
    int findAuthor(@Param("articleId") int articleId);

    String findContent(@Param("articleId") int articleId);

    int deleteByArticleId(@Param("articleId") int articleId);

    int updateArticle(Article article);

    int updateArticleState(@Param("articleId") int articleId);

    List<Article> getIllegalArticle(@Param("userId") Integer userId);


}
