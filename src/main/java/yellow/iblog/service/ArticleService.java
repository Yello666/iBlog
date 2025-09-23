package yellow.iblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import yellow.iblog.model.Article;

public interface ArticleService {
    Boolean undoArticleFavor(Long aid,Long uid);
    Integer undoArticleLike(Long aid,Long uid);
    Integer likeArticleByAid(Long aid,Long uid);
    Integer favorArticleByAid(Long aid,Long uid);
    Article createArticle(Article article);
    Boolean deleteArticleByAid(Long Aid);
    Article updateArticle(Article article);
    Article getArticleByAid(Long Aid);
    Page<Article> getArticleByUid(Long uid, int page, int size);
}
