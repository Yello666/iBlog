package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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

    //取消收藏
    @Override
    @Transactional
    public Boolean undoArticleFavor(Long aid, Long uid){
        Article a=articleMapper.selectById(aid);
        if(a==null){
            log.error("用户{}取消收藏文章{}失败:文章不存在",uid,aid);
            throw new RuntimeException("尝试取消收藏不存在的文章");
        }
        a.setFavorCount(a.getFavorCount()-1);
        if(articleMapper.updateById(a)<=0){
            log.error("用户{}取消收藏aid:{}失败,mysql数据库操作失败",uid,aid);
            return false;
        }
        return true;
    }
    //取消点赞（有待完善）
    @Override
    @Transactional
    public Integer undoArticleLike(Long aid, Long uid){
        Article a=articleMapper.selectById(aid);
        if(a==null){
            log.error("用户{}取消点赞文章{}失败:文章不存在",uid,aid);
            throw new RuntimeException("尝试取消点赞不存在的文章");
        }
        a.setLikesCount(a.getLikesCount()-1);
        if(articleMapper.updateById(a)<=0){
            log.error("用户{}取消点赞aid:{}失败,mysql数据库操作失败",uid,aid);
            return -1;
        }
        int redisLikes=Math.toIntExact(likeService.getArticleLikeCount(aid));
        log.info("设置的点赞数为:{}",a.getLikesCount());
        return redisLikes+a.getLikesCount();
    }


    @Override
    @Transactional
    public Integer favorArticleByAid(Long aid,Long uid){
        Article a=articleMapper.selectById(aid);
        if(a==null){
            log.error("用户{}收藏文章{}失败:文章不存在",uid,aid);
            throw new RuntimeException("尝试收藏不存在的文章");
        }
        int deltaFavors=Math.toIntExact(favorService.favorArticle(aid));
        if(deltaFavors<=0){
            log.error("用户{}收藏文章{}失败",uid,aid);
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
    public Integer likeArticleByAid(Long aid,Long uid){
        Article a=articleMapper.selectById(aid);
        if(a==null){
            log.error("用户{}点赞文章{}失败:文章不存在",uid,aid);
            throw new RuntimeException("尝试点赞不存在的文章");
        }
//        a.setLikesCount(a.getLikesCount()+1); 没有必要做这个操作，因为数据都是从数据库拿出来，
//        临时点赞存放到redis那，获取点赞数是先从数据库查询，再与redis相加，没必要在这里加点赞数
        int deltaLikes=Math.toIntExact(likeService.likeArticle(aid));
        if(deltaLikes<=0){
            log.error("用户{}点赞文章{}失败",uid,aid);
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
            return savedA;
        }
        return null;
    }

    @Override
//    @Cacheable(value="article",key="#aid",unless="#result==null")//要求对象实现serializable接口，或者把 RedisCacheManager 改成 JSON 序列化
    public Article getArticleByAid(Long aid) {
        //1.先读缓存（只包含静态/DB数据？）
        Article cacheA=(Article) redisService.checkCache("article",aid);
        if(cacheA==null){
            log.warn("缓存未命中或第一次查询");
            //2.缓存未命中，从DB读并缓存
            Article dbArticle=articleMapper.selectById(aid);
            if (dbArticle != null){
                //获取点赞数和收藏数需要将redis和mysql加起来
                dbArticle.setLikesCount(Math.toIntExact(likeService.getArticleLikeCount(aid))+dbArticle.getLikesCount());
                dbArticle.setFavorCount(Math.toIntExact(favorService.getArticleFavorCount(aid))+dbArticle.getFavorCount());
                //手动缓存
                if(redisService.addCache("article",aid,dbArticle)){
                    log.info("缓存成功");
                } else{
                    log.warn("缓存失败");
                }

                return dbArticle;
            }
        } else{
            //缓存的A可能点赞数不一致
            cacheA.setLikesCount(Math.toIntExact(likeService.getArticleLikeCount(aid))+cacheA.getLikesCount());
            cacheA.setFavorCount(Math.toIntExact(favorService.getArticleFavorCount(aid))+cacheA.getFavorCount());
            log.info("目前缓存里面的点赞数:{},mysql里面的点赞数:{}",likeService.getArticleLikeCount(aid),cacheA.getLikesCount());
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
