package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Long unFavorArticle(Long aid,Long uid) {
        String key = "article:unfavor:" + aid;//新建一个字段，避免操作原来的字段，可能有并发问题
        try {
            //删除收藏集合里面的uid
            String setKey="article:"+aid+"favor:uids:";
            redisTemplate.opsForSet().remove(setKey,String.valueOf(uid));
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
    public Long favorArticle(Long aid,Long uid) {
        String key = "article:favor:" + aid;
        try {
            // 存储增量（每次+1）,返回的是当前的个数
            Long newCount = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, 2, TimeUnit.MINUTES); // 设置过期时间

            //记录收藏过这篇文章的user//TODO 持久化存储
            String setKey="article:"+aid+"favor:uids:";
            redisTemplate.opsForSet().add(key,String.valueOf(uid));
            redisTemplate.expire(setKey, 2, TimeUnit.MINUTES); // 设置过期时间

            return newCount;
        } catch (Exception e) {
            throw new RuntimeException("收藏失败", e);
        }
    }
    //查询用户是否收藏了文章
    public Boolean getArticleIsFavored(Long aid,Long uid){
        String setKey="article:"+aid+"favor:uids:";
        Boolean isFavored=redisTemplate.opsForSet().isMember(setKey,String.valueOf(uid));
        log.info("用户{},文章{},收藏{}",uid,aid,isFavored);
        return isFavored;
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
