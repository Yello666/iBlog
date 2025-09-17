package yellow.iblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import yellow.iblog.model.Article;

import java.util.List;

public interface ArticleService {
    public Article createArticle(Article article);
    public Boolean deleteArticleByAid(Long Aid);
    public Article updateArticle(Article article);
    public Article getArticleByAid(Long Aid);
    public Page<Article> getArticleByUid(Long uid, int page, int size);
}
