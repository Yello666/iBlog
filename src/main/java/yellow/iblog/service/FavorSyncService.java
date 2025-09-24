package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.ArticleMapper;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavorSyncService {

    private final StringRedisTemplate redisTemplate;
    private final ArticleMapper articleMapper;
    @Scheduled(fixedRate = 50000)
    public void syncUnFavorToDB() {
        // 获取所有key
        Set<String> keys = redisTemplate.keys("article:favor:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {//如果有文章很久没有被点收藏的，那么reids里就没有key，不会统计进去
            try {
                Long aid = Long.valueOf(key.replace("article:favor:", ""));
                // 使用原子操作获取并删除，避免并发问题
                String deltaStr = redisTemplate.opsForValue().getAndDelete(key);
                if (deltaStr == null) continue;

                int delta = Integer.parseInt(deltaStr);
                if (delta <= 0) continue;

                // SQL 原子更新收藏数
                if(articleMapper.incrFavorCount(aid, delta)<=0){
                    throw new RuntimeException("同步收藏数据到mysql失败");
                }
                log.info("同步评论 {} 的收藏数 {} 到数据库", aid, delta);
            } catch (Exception e) {
                log.error("同步收藏数据到mysql失败，key:{}",key,e);
                throw new RuntimeException(e);
            }
        }
    }
    // 每隔50秒执行一次，可以根据需求调整
    @Scheduled(fixedRate = 50000)
    public void syncFavorToDB() {
        // 获取所有key
        Set<String> keys = redisTemplate.keys("article:favor:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {//如果有文章很久没有被点收藏的，那么reids里就没有key，不会统计进去
            try {
                Long aid = Long.valueOf(key.replace("article:favor:", ""));
                // 使用原子操作获取并删除，避免并发问题
                String deltaStr = redisTemplate.opsForValue().getAndDelete(key);
                if (deltaStr == null) continue;

                int delta = Integer.parseInt(deltaStr);
                if (delta <= 0) continue;

                // SQL 原子更新收藏数
                if(articleMapper.incrFavorCount(aid, delta)<=0){
                    throw new RuntimeException("同步收藏数据到mysql失败");
                }
                log.info("同步评论 {} 的收藏数 {} 到数据库", aid, delta);
            } catch (Exception e) {
                log.error("同步收藏数据到mysql失败，key:{}",key,e);
                throw new RuntimeException(e);
            }
        }
    }
}
