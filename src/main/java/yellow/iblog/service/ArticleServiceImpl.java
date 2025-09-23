package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.model.Article;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService{
    private final ArticleMapper articleMapper;
    private final LikeService likeService;
    private final FavorService favorService;
    private final RedisService redisService;

    @Override
    @Transactional
    public Integer favorArticleByAid(Long aid){
        Article a=articleMapper.selectById(aid);
        if(a==null){
            log.error("收藏文章{}失败:文章不存在",aid);
            throw new RuntimeException("尝试收藏不存在的文章");
        }
        int deltaFavors=Math.toIntExact(favorService.favorArticle(aid));
        if(deltaFavors<=0){
            log.error("收藏文章{}失败",aid);
            throw new RuntimeException("收藏失败");
        }
        return Math.toIntExact(deltaFavors);
    }

    @Override
    public Article createArticle(Article article) {
        if(articleMapper.insert(article)>0){
            return article;
        }
        return null;
    }

    //返回的是增长的点赞数
    @Override
    @Transactional
    public Integer likeArticleByAid(Long aid){
        Article a=articleMapper.selectById(aid);
        if(a==null){
            log.error("点赞文章{}失败:文章不存在",aid);
            throw new RuntimeException("尝试点赞不存在的文章");
        }
//        a.setLikesCount(a.getLikesCount()+1); 没有必要做这个操作，因为数据都是从数据库拿出来，
//        临时点赞存放到redis那，获取点赞数是先从数据库查询，再与redis相加，没必要在这里加点赞数
        int deltaLikes=Math.toIntExact(likeService.likeArticle(aid));
        if(deltaLikes<=0){
            log.error("点赞文章{}失败",aid);
            throw new RuntimeException("点赞失败");
        }
        return Math.toIntExact(deltaLikes);

    }
    @Override
    @CacheEvict(value="article",key="#aid")
    public Boolean deleteArticleByAid(Long aid) {
        return articleMapper.deleteById(aid) > 0;
    }

    @Override
    @CachePut(value="article",key="#article.aid")
    public Article updateArticle(Article article) {
        //必须要先将之前的拿出来，再存。
        //因为mybatis-plus是动态修改，传过来的article只包含标题，内容，作者id，其它内容都是空的
        //直接存进去会将创建时间和其它所有东西都置为默认值，创建时间会丢失掉
        Article savedA=articleMapper.selectById(article.getAid());
        savedA.setUpdatedAt(LocalDateTime.now());
        savedA.setTitle(article.getTitle());//只能修改标题和内容
        savedA.setContent(article.getContent());
        if(articleMapper.updateById(savedA)>0){
            return article;
        }
        return null;
    }

    @Override
//    @Cacheable(value="article",key="#aid",unless="#result==null")//要求对象实现serializable接口，或者把 RedisCacheManager 改成 JSON 序列化
    public Article getArticleByAid(Long aid) {
        Article cacheA=(Article) redisService.checkCache("article",aid);
        if(cacheA==null){
            log.warn("缓存未命中或第一次查询");
            Article a=articleMapper.selectById(aid);
            if (a != null){
                a.setLikesCount(Math.toIntExact(likeService.getArticleLikeCount(aid))+a.getLikesCount());
                //手动缓存
                if(redisService.addCache("article",aid,a)){
                    log.info("缓存成功");
                } else{
                    log.warn("缓存失败");
                }
                return a;
            }
        } else{
            return cacheA;
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
