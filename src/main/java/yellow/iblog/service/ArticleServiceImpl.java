package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.model.Article;
import yellow.iblog.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService{
    private final ArticleMapper articleMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper){
        this.articleMapper=articleMapper;
    }
    @Override
    @Cacheable(value="article",key="#article.aid",unless="#result==null")
    public Article createArticle(Article article) {
        if(articleMapper.insert(article)>0){
            return article;
        }
        return null;
    }

    @Override
    @CacheEvict(value="article",key="#aid")
    public Boolean deleteArticleByAid(Long aid) {
        return articleMapper.deleteById(aid) > 0;
    }

    @Override
    @CachePut(value="article",key="#article.aid")
    public Article updateArticle(Article article) {
        article.setUpdatedAt(LocalDateTime.now());
        if(articleMapper.updateById(article)>0){
            return article;
        }
        return null;
    }

    @Override
    @Cacheable(value="article",key="#aid",unless="#result==null")
    public Article getArticleByAid(Long aid) {
        if (articleMapper.selectById(aid) != null){
            return articleMapper.selectById(aid);
        }
        return null;
    }
    @Override
    public Page<Article> getArticleByUid(Long uid, int page, int size){
        Page<Article> articlePage=new Page<>(page,size);
        LambdaQueryWrapper<Article> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Article::getUid,uid)
                .select(Article::getAid,Article::getTitle,Article::getUpdatedAt)
                .orderByDesc(Article::getUpdatedAt);
        return articleMapper.selectPage(articlePage,wrapper);

    }
}
