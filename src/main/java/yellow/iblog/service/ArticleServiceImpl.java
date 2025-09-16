package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.model.Article;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService{
    private final ArticleMapper articleMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper){
        this.articleMapper=articleMapper;
    }
    @Override
    public Article createArticle(Article article) {
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        if(articleMapper.insert(article)>0){
            return article;
        }
        return null;
    }

    @Override
    public Boolean deleteArticleByAid(Long aid) {
        return articleMapper.deleteById(aid) > 0;
    }

    @Override
    public Article updateArticle(Article article) {
        article.setUpdatedAt(LocalDateTime.now());
        if(articleMapper.updateById(article)>0){
            return article;
        }
        return null;
    }

    @Override
    public Article getArticleByAid(Long aid) {
        if (articleMapper.selectById(aid) != null){
            return articleMapper.selectById(aid);
        }
        return null;
    }
    @Override
    public List<Article> getArticleByUid(Long uid){
        return articleMapper.selectList(//？
                new QueryWrapper<Article>().eq("uid", uid)
        );
        //相当于@Select("SELECT aid,uid,title,content,created_at from articles WHERE uid=#{uid}")
        //public List<Article> getArticleByUid(Long uid);
    }
}
