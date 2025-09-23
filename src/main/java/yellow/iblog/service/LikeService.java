package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor //它会为所有 final 字段和 @NonNull 注解的字段生成一个构造函数。
public class LikeService {
    // 获取实时点赞数（Redis里优先）

    private final StringRedisTemplate redisTemplate;
    //Key 和 Value 都强制是 字符串。
    //底层已经设置好序列化器：StringRedisSerializer。
    //适合存放简单的 字符串 / 计数器 / JSON 字符串。


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
