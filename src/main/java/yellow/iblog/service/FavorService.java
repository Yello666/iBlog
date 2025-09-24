package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavorService {
    // 获取实时点赞数（Redis里优先）

    private final StringRedisTemplate redisTemplate;
    //取消收藏（redis自增）但后续要减去这个数
    public Long unFavorArticle(Long aid) {
        String key = "article:unfavor:" + aid;//新建一个字段，避免操作原来的字段，可能有并发问题
        try {
            // 存储增量（每次+1）
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 2, TimeUnit.MINUTES); // 设置过期时间
            log.info("取消收藏存入了:{}",redisTemplate.opsForValue().get(key));
            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("取消收藏失败", e);
        }
    }

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

    //获取取消收藏数
    public Long getArticleUnFavorCount(Long aid) {
        String key = "article:unfavor:" + aid;
        Object obj = redisTemplate.opsForValue().get(key);//如果这个键不存在的话，那么就返回0
        return obj == null ? 0L : Long.parseLong(obj.toString());

    }
    //获取收藏数
    public Long getArticleFavorCount(Long aid) {
        String key = "article:favor:" + aid;
        Object o = redisTemplate.opsForValue().get(key);
        return o == null ? 0L : Long.parseLong(o.toString());

    }

}
