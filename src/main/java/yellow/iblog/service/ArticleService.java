package yellow.iblog.service;

import yellow.iblog.model.Article;

import java.util.List;

public interface ArticleService {
    public Article createArticle(Article article);
    public Boolean deleteArticleByAid(Long Aid);
    public Article updateArticle(Article article);
    public Article getArticleByAid(Long Aid);
    public List<Article> getArticleByUid(Long uid);
}
