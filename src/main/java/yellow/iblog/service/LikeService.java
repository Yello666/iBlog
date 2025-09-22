package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor //它会为所有 final 字段和 @NonNull 注解的字段生成一个构造函数。
public class LikeService {
    // 获取实时点赞数（Redis里优先）

    private final RedisTemplate redisTemplate;


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

    public Long getLikeCount(Long cid) {
        String key = "comment:likes:" + cid;
        String val = redisTemplate.opsForValue().get(key).toString();
        return val == null ? 0L : Long.parseLong(val);

    }
}
