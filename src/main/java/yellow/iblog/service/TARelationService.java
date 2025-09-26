package yellow.iblog.service;

import yellow.iblog.model.Article;
import yellow.iblog.model.Tag;

import java.util.List;

public interface TARelationService {
    Integer addTagsToArticle(List<String> tags, Long aid);
    Integer deleteTagsFromArticle(List<String> tags,Long aid);
    List<Tag> getAllTagsOfOneArticle(Long aid);
    List<Article> getArticlesIncludeTag(String tagName);
    List<Article> getArticlesIncludeTagList(List<Tag> tagList);
}
