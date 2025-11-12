package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.model.Article;
import yellow.iblog.model.ArticleFavorResponse;
import yellow.iblog.model.ArticleLikeResponse;
import yellow.iblog.model.ArticleResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleService{
    private final ArticleMapper articleMapper;
    private final LikeService likeService;
    private final FavorService favorService;
    private final RedisService redisService;

    private final static  Integer DEFAULT_ARTICLES_NUM=5;
    private final StringRedisTemplate redisTemplate;


    //    点赞文章
    //返回的是当前点赞数
    @Override
    @Transactional
    public ArticleLikeResponse likeArticle(Long aid, Long uid){
        Article a=articleMapper.selectById(aid);
        if(a==null){
            log.error("用户{}点赞文章{}失败:文章不存在",uid,aid);
            throw new RuntimeException("尝试点赞不存在的文章");
        }
        Boolean status=likeService.likeArticleV2(aid,uid);
        if(status){
            log.info("用户{}点赞了文章{}",uid,aid);
        } else{
            log.info("用户{}取消了文章{}的点赞",uid,aid);
        }
        int crtLikes=likeService.getArticleLikeCount(aid);
        ArticleLikeResponse response=new ArticleLikeResponse();
        response.setCrtLikes(crtLikes);
        response.setStatus(status);
        return response;

    }

//收藏文章
    @Override
    @Transactional
    public ArticleFavorResponse favorArticleByAid(Long aid, Long uid){
        Article a=articleMapper.selectById(aid);
        if(a==null){
            log.error("用户{}收藏文章{}失败:文章不存在",uid,aid);
            throw new RuntimeException("尝试收藏不存在的文章");
        }
        ArticleFavorResponse response=new ArticleFavorResponse();
        Boolean status=favorService.favorArticle(aid,uid);
        if(status){
            log.info("用户{}收藏了文章{}",uid,aid);
        } else{
            log.info("用户{}取消收藏了文章{}",uid,aid);
        }
        response.setStatus(status);
        Integer crtFavors=favorService.getArticleFavorCount(aid);
        response.setCrtFavors(crtFavors);
        return response;
    }

    @Override
    public Article createArticle(Article article) {
        article.setCreatedAt(LocalDateTime.now());
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

    //获取文章
    @Override
//    @Cacheable(value="article",key="#aid",unless="#result==null")//要求对象实现serializable接口，或者把 RedisCacheManager 改成 JSON 序列化
    public Article getArticleByAid(Long aid) {
        //1.先读缓存
        Article cacheA=(Article) redisService.checkCache("article",aid);
        if(cacheA==null){
            //2.缓存未命中，从DB读数据
            Article dbArticle=articleMapper.selectById(aid);
            if (dbArticle == null){
                log.warn("尝试查询不存在的文章,aid:{}",aid);
                return null;
            }
            else{
                //3.将点赞数设置为当前点赞数
                int crtLikes= likeService.getArticleLikeCount(aid);
                dbArticle.setLikesCount(crtLikes);
                //将收藏数设置为当前收藏数
                int crtFavors=favorService.getArticleFavorCount(aid);
                dbArticle.setFavorCount(crtFavors);
                //4.手动缓存，设置过期时间：2小时
                if(redisService.addCache("article",aid,dbArticle,2)){
                    log.info("缓存文章成功");
                } else{
                    log.warn("缓存文章失败");
                }
                return dbArticle;
            }
        } else{
            //5.有缓存,更新点赞数,更新收藏数
            int crtLikes= likeService.getArticleLikeCount(aid);
            cacheA.setLikesCount(Math.toIntExact(crtLikes));
            int crtFavors=Math.toIntExact(favorService.getArticleFavorCount(aid));
            cacheA.setFavorCount(crtFavors);
            //更新文章的TTL为2小时
            redisTemplate.expire(redisService.getKey("article",aid), 2, TimeUnit.HOURS);
            return cacheA;
        }

    }
    @Override
    public Page<ArticleResponse> getArticleByUid(Long uid, int page, int size){
        Page<Article> articlePage=new Page<>(page,size);
        LambdaQueryWrapper<Article> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Article::getUid,uid)
                .orderByDesc(Article::getUpdatedAt);
//                .select(Article::getAid,Article::getTitle,Article::getUpdatedAt)//值返回aid，title，更新时间，注释掉就返回所有字段

        Page<Article> articleList=articleMapper.selectPage(articlePage,wrapper);
        return (Page<ArticleResponse>) articleList.convert(ArticleResponse::new);

    }
    @Override
    public List<Article> getArticleListOrderedByLikes(Integer num){
        if(num==null||num<=0){
            num=DEFAULT_ARTICLES_NUM;//默认值
        }
        LambdaQueryWrapper<Article> wrapper=new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Article::getLikesCount)
                .last("LIMIT "+num);
        //LIMIT是mysql语句的写法,控制返回的数量
        //SELECT * FROM article ORDER BY LikesCount DESC LIMIT 5;

         return articleMapper.selectList(wrapper);
    }
}
