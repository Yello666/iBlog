package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.ArticleLikeMapper;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.mapper.CommentLikeMapper;
import yellow.iblog.mapper.CommentMapper;
import yellow.iblog.model.Article;
import yellow.iblog.model.ArticleLike;
import yellow.iblog.model.Comment;
import yellow.iblog.model.CommentLike;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor //它会为所有 final 字段和 @NonNull 注解的字段生成一个构造函数。
@Slf4j
public class LikeService {
    // 获取redis里面的实时点赞数和取消点赞数
    //记录文章点赞数key超时时间：2H，如果访问一次文章点赞数，就刷新2H
    //记录文章点赞用户set的超时时间: 不建议设置访超时时间，如果过期了，查询成本大。
    private final StringRedisTemplate redisTemplate;
    private final ArticleLikeMapper articleLikeMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    //Key 和 Value 都强制是 字符串。
    //底层已经设置好序列化器：StringRedisSerializer。
    //适合存放简单的 字符串 / 计数器 / JSON 字符串。

//    // 取消点赞评论（Redis自增）
//    public Long unlikeComment(Long cid) {
//        String key = "comment:unlikes:" + cid;
//        redisTemplate.opsForSet().add("comment:unlike:ids",String.valueOf(cid));
//        try {
//            // 存储增量（每次+1）
//            Long newCount = redisTemplate.opsForValue().increment(key, 1);
//            redisTemplate.expire(key, 2, TimeUnit.HOURS); // 设置过期时间
//            redisTemplate.opsForSet().add("comment:like:ids",String.valueOf(cid));
//            return newCount;
//        } catch (Exception e) {
//            throw new RuntimeException("点赞失败", e);
//        }
//    }
////    //获取评论取消点赞数
//    public Long getCommentUnLikeCount(Long cid) {
//        String key = "comment:unlikes:" + cid;
//        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
//        return obj == null ? 0L : Long.parseLong(obj.toString());
//
//    }

    // 点赞评论（Redis自增）
    public Boolean likeComment(Long cid,Long uid) {
        //记录该评论点赞用户集合的key--判断uid用户是否点过赞
        String key="comment:"+cid+":likes:users";
        //记录该评论点赞数的key--点赞数的增减
        String countKey="comment:"+cid+":likes:count";
        //记录修改过的key---用于同步的时候遍历，同步后会删除掉这些key
        String dirtyKey="comment:likes:dirty";
        //预加载点赞数（如果不这么做，count 会是deltaLikes，会导致后续同步的点赞数不一致，点赞逻辑变得麻烦，获取点赞逻辑也变得麻烦）
        getCommentLikeCount(cid);
        // 标记这篇评论的点赞数被修改过（会在下面通过mysql关系表修改）
        redisTemplate.opsForSet().add(dirtyKey, String.valueOf(cid));
        //如果集合里面找不到用户，说明用户没有点过赞
        if(!getCommentIsLiked(cid,uid)){
            //增加用户到集合里面
            redisTemplate.opsForSet().add(key, String.valueOf(uid));
            //点赞数+1，点赞成功
            redisTemplate.opsForValue().increment(countKey);
            //关系表插入记录
            // TODO为了高性能，可以放入消息队列异步执行
            CommentLike commentLike=new CommentLike();
            commentLike.setCreatedAt(LocalDateTime.now());
            commentLike.setUid(uid);
            commentLike.setCid(cid);
            commentLikeMapper.insert(commentLike);
            return true;//返回true，代表现在已经点了赞
        } else{
            //如果找到了，就是已经点过赞,从评论点赞集合移除用户，评论点赞数-1
            redisTemplate.opsForSet().remove(key, String.valueOf(uid));
            redisTemplate.opsForValue().decrement(countKey);
            //关系表删除记录
            commentLikeMapper.deleteLikesByCidUid(cid,uid);
            return false;//返回false，代表现在没有点赞。
        }
    }
    //获得评论redis点赞数
    public Integer getCommentLikeCount(Long cid) {
        //先从redis查目前点赞数，如果查不到，说明数据库里存的就是目前点赞数
        String key = "comment:" + cid+":likes:count";
        String likesStr = redisTemplate.opsForValue().get(key);
        if(likesStr!=null){
            //评论如果被访问，就刷新点赞键的存活时长为2H
            redisTemplate.expire(key, Duration.ofHours(2));
            return Integer.parseInt(likesStr);
        } else{
            Comment c= commentMapper.selectById(cid);
            if(c==null){//防止恶意请求不存在的aid
                redisTemplate.opsForValue().set(key,"0",5,TimeUnit.MINUTES);
                return 0;
            }
            // 回填缓存，防止下次再查DB(或者初始化评论初始点赞数)
            redisTemplate.opsForValue().set(key, String.valueOf(c.getLikesCount()), 2, TimeUnit.HOURS);
            return c.getLikesCount();
        }

    }
    //查看用户是否点赞过评论
    public Boolean getCommentIsLiked(Long cid,Long uid){
        String setKey="comment:"+cid+":likes:users";
        Boolean isLiked= redisTemplate.opsForSet().isMember(setKey,String.valueOf(uid));
        log.info("用户{}点赞评论{}:{}",uid,cid,isLiked);
        return isLiked;
    }

    //点赞文章---点赞文章和取消点赞都是一个请求，只需要检查用户有没有点赞过，以此为标准进行点赞的增减
    public Boolean likeArticleV2(Long aid,Long uid){
        //记录该文章点赞用户集合的key--判断uid用户是否点过赞
        String key="article:"+aid+":likes:users";
        //记录该文章点赞数的key--点赞数的增减
        String countKey="article:"+aid+":likes:count";
        //记录修改过的key---用于同步的时候遍历，同步后会删除掉这些key
        String dirtyKey="article:likes:dirty";
        //预加载点赞数（如果不这么做，count 会是deltaLikes，会导致后续同步的点赞数不一致，点赞逻辑变得麻烦，获取点赞逻辑也变得麻烦）
        getArticleLikeCount(aid);
        // 标记这篇文章的点赞数被修改过（会在下面通过mysql关系表修改）
        redisTemplate.opsForSet().add(dirtyKey, String.valueOf(aid));
        //如果集合里面找不到用户，说明用户没有点过赞
        if(!getArticleIsLiked(aid,uid)){
            //增加用户到集合里面
            redisTemplate.opsForSet().add(key, String.valueOf(uid));
            //点赞数+1，点赞成功
            redisTemplate.opsForValue().increment(countKey);
            //关系表插入记录
            // TODO为了高性能，可以放入消息队列异步执行
            ArticleLike articleLike=new ArticleLike();
            articleLike.setCreatedAt(LocalDateTime.now());
            articleLike.setUid(uid);
            articleLike.setAid(aid);
            articleLikeMapper.insert(articleLike);
            return true;//返回true，代表现在已经点了赞
        } else{
            //如果找到了，就是已经点过赞,从文章点赞集合移除用户，文章点赞数-1
            redisTemplate.opsForSet().remove(key, String.valueOf(uid));
            redisTemplate.opsForValue().decrement(countKey);
            //关系表删除记录
            articleLikeMapper.deleteLikesByAidUid(aid,uid);
            return false;//返回false，代表现在没有点赞。
        }
    }
    //获得文章redis点赞数--可用于查询文章点赞数，或者点赞的时候加载文章初始点赞数
    //应该返回目前点赞数
    public Integer getArticleLikeCount(Long aid) {
        //先从redis查目前点赞数，如果查不到，说明数据库里存的就是目前点赞数
        String key = "article:" + aid+":likes:count";
        String likesStr = redisTemplate.opsForValue().get(key);
        if(likesStr!=null){
            //文章如果被访问，就刷新点赞键的存活时长为2H，可以避免冷门数据长期占用内存，但热门文章会“续命”
            redisTemplate.expire(key, Duration.ofHours(2));
            return Integer.parseInt(likesStr);
        } else{
            Article a= articleMapper.selectById(aid);
            if(a==null){//防止恶意请求不存在的aid
                redisTemplate.opsForValue().set(key,"0",5,TimeUnit.MINUTES);
                return 0;
            }
            // 回填缓存，防止下次再查DB(或者初始化文章初始点赞数)
            redisTemplate.opsForValue().set(key, String.valueOf(a.getLikesCount()), 2, TimeUnit.HOURS);
            return a.getLikesCount();
        }
    }
    //查看用户是否点赞过文章
    public Boolean getArticleIsLiked(Long aid,Long uid){
        String setKey="article:"+aid+":likes:users";
        Boolean isLiked= redisTemplate.opsForSet().isMember(setKey,String.valueOf(uid));
        log.info("用户{}点赞文章{}:{}",uid,aid,isLiked);
        return isLiked;
    }
}
