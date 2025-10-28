package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor //它会为所有 final 字段和 @NonNull 注解的字段生成一个构造函数。
@Slf4j
public class LikeService {
    // 获取redis里面的实时点赞数和取消点赞数
    // TODO后续可以将代码简洁化，传入"article","comment"参数即可

    private final StringRedisTemplate redisTemplate;
    //Key 和 Value 都强制是 字符串。
    //底层已经设置好序列化器：StringRedisSerializer。
    //适合存放简单的 字符串 / 计数器 / JSON 字符串。


    // 取消点赞评论（Redis自增）
    public Long unlikeComment(Long cid) {
        String key = "comment:unlikes:" + cid;
        redisTemplate.opsForSet().add("comment:unlike:ids",String.valueOf(cid));
        try {
            // 存储增量（每次+1）
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 2, TimeUnit.HOURS); // 设置过期时间
            redisTemplate.opsForSet().add("comment:like:ids",String.valueOf(cid));
            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("点赞失败", e);
        }
    }
    //获取评论取消点赞数
    public Long getCommentUnLikeCount(Long cid) {
        String key = "comment:unlikes:" + cid;
        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
        return obj == null ? 0L : Long.parseLong(obj.toString());

    }

    // 点赞评论（Redis自增）
    public Long likeComment(Long cid) {
        String key = "comment:likes:" + cid;
        try {
            // 存储增量（每次+1）
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 1, TimeUnit.HOURS); // 设置过期时间
            redisTemplate.opsForSet().add("comment:like:ids",String.valueOf(cid));
            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("点赞失败", e);
        }
    }
    //获得评论redis点赞数
    public Long getCommentLikeCount(Long cid) {
        String key = "comment:likes:" + cid;
        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
        return obj == null ? 0L : Long.parseLong(obj.toString());

    }

    //点赞文章
    public Long likeArticle(Long aid,Long uid) {
        String key = "article:likes:" + aid;
        log.info("正在点赞中");
        try {
            // 存储增量（每次+1）
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 1, TimeUnit.HOURS); // 设置过期时间
            // 记录被点赞过的文章 ID，到时候可以遍历点赞的文章然后同步点赞数到mysql
            //在redis设置一个集合，里面存放点赞文章的id（字符串类型）
//            redisTemplate.opsForSet().add("article:like:ids", String.valueOf(aid));
            redisTemplate.opsForSet().add("article:like:aids", String.valueOf(aid));
            //存入为文章点赞的用户uid，记录用户是否有为文章点过赞
            //TODO 写一个mysql数据结构articlelikes记录哪些用户点赞了哪些文章（多对多），
            String setKey="article:"+aid+"like:uids:";
            redisTemplate.opsForSet().add(setKey, String.valueOf(uid));
            redisTemplate.expire(setKey, 1, TimeUnit.HOURS);

            log.info("点赞存入了:{}",redisTemplate.opsForValue().get(key));
            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("点赞失败", e);
        }
    }
    //查看用户一小时内是否点过赞
    //TODO 改为后面查询redis+mysql
    public Boolean getArticleIsLiked(Long aid,Long uid){
        String setKey="article:"+aid+"like:uids:";
        Boolean isLiked= redisTemplate.opsForSet().isMember(setKey,String.valueOf(uid));
        log.info("用户{}点赞{}:{}",uid,aid,isLiked);
        return isLiked;
    }

    //取消点赞（redis自增）但后续要减去这个数
    public Long unlikeArticle(Long aid,Long uid) {
        String key = "article:unlikes:" + aid;//新建一个字段，避免操作原来的字段，可能有并发问题
        //放到redis集合里面统计取消点赞的aid
        redisTemplate.opsForSet().add("article:unlike:aids",String.valueOf(aid));
        //删除掉点赞集合的uid
        String setKey="article:"+aid+"like:uids:";
        redisTemplate.opsForSet().remove(setKey,String.valueOf(uid));
        try {
            // 存储增量（每次+1）
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 3, TimeUnit.MINUTES); // 设置过期时间
            log.info("取消点赞存入了:{}",redisTemplate.opsForValue().get(key));
            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("取消点赞失败", e);
        }
    }

    //获取文章取消点赞数
    public Long getArticleUnLikeCount(Long aid) {
        String key = "article:unlikes:" + aid;
        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
        return obj == null ? 0L : Long.parseLong(obj.toString());

    }
    //获得文章redis点赞数
    public Long getArticleLikeCount(Long aid) {
        String key = "article:likes:" + aid;
        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
        return obj == null ? 0L : Long.parseLong(obj.toString());

    }



}
