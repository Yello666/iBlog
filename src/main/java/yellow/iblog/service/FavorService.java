package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FavorService {
    // 获取实时点赞数（Redis里优先）

    private final StringRedisTemplate redisTemplate;

    //收藏文章
    public Long favorArticle(Long aid) {
        String key = "article:favor:" + aid;
        try {
            // 存储增量（每次+1）,返回的是当前的个数
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 2, TimeUnit.MINUTES); // 设置过期时间
            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("收藏失败", e);
        }
    }

    //获取收藏数
    public Long getArticleFavorCount(Long aid) {
        String key = "article:favor:" + aid;
        Object o = redisTemplate.opsForValue().get(key);
        return o == null ? 0L : Long.parseLong(o.toString());

    }
}
