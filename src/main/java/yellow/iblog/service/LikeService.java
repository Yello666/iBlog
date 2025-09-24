package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor //它会为所有 final 字段和 @NonNull 注解的字段生成一个构造函数。
@Slf4j
public class LikeService {
    // 获取实时点赞数（Redis里优先）

    private final StringRedisTemplate redisTemplate;
    //Key 和 Value 都强制是 字符串。
    //底层已经设置好序列化器：StringRedisSerializer。
    //适合存放简单的 字符串 / 计数器 / JSON 字符串。

    //取消点赞（redis自增）但后续要减去这个数
    public Long unlikeArticle(Long aid) {
        String key = "article:unlikes:" + aid;//新建一个字段，避免操作原来的字段，可能有并发问题
        try {
            // 存储增量（每次+1）
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 2, TimeUnit.MINUTES); // 设置过期时间
            log.info("取消点赞存入了:{}",redisTemplate.opsForValue().get(key));
            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("取消点赞失败", e);
        }
    }
    //获取取消点赞数
    public Long getArticleUnLikeCount(Long aid) {
        String key = "article:unlikes:" + aid;
        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
        return obj == null ? 0L : Long.parseLong(obj.toString());

    }

    // 点赞评论（Redis自增）
    public Long likeComment(Long cid) {
        String key = "comment:likes:" + cid;
        try {
            // 存储增量（每次+1）
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 1, TimeUnit.DAYS); // 设置过期时间

            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("点赞失败", e);
        }
    }
    //点赞文章
    public Long likeArticle(Long aid) {
        String key = "article:likes:" + aid;
        try {
            // 存储增量（每次+1）
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 2, TimeUnit.MINUTES); // 设置过期时间
            // 记录被点赞过的文章 ID，到时候可以遍历点赞的文章然后同步到mysql
            //在redis设置一个集合，里面存放点赞文章的id（字符串类型）
            redisTemplate.opsForSet().add("article:like:ids", String.valueOf(aid));
            log.info("存入了:{}",redisTemplate.opsForValue().get(key));
            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("点赞失败", e);
        }
    }


    public Long getCommentLikeCount(Long cid) {
        String key = "comment:likes:" + cid;
        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
        return obj == null ? 0L : Long.parseLong(obj.toString());

    }
    public Long getArticleLikeCount(Long aid) {
        String key = "article:likes:" + aid;
        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
        return obj == null ? 0L : Long.parseLong(obj.toString());

    }

}
