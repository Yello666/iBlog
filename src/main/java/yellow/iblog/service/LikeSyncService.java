package yellow.iblog.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.mapper.CommentMapper;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeSyncService {

    private final StringRedisTemplate redisTemplate;
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    // 每隔30秒执行一次，可以根据需求调整
    @Scheduled(fixedRate = 30000)
    public void syncLikesToDB() {
        // 获取所有key
        Set<String> keys = redisTemplate.keys("comment:likes:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                Long cid = Long.valueOf(key.replace("comment:likes:", ""));
                // 使用原子操作获取并删除，避免并发问题
                String deltaStr = redisTemplate.opsForValue().getAndDelete(key);

                if (deltaStr == null) continue;

                int delta = Integer.parseInt(deltaStr);
                if (delta <= 0) continue;

                // SQL 原子更新点赞数
                if(commentMapper.incrLikeCount(cid, delta)<=0){
                    throw new RuntimeException("同步点赞数据到mysql失败");
                }
                //获取之后不能删除，不然后面获取点赞数的时候要将两个地方的加起来，redis就找不到键了
                //获取完之后要设置为0
                //不用这样，要设置redis找不到键返回0，就可以了，还是要删除的
//                redisTemplate.opsForValue().set(key,String.valueOf(0));
                log.info("同步评论 {} 的点赞数 {} 到数据库", cid, delta);
            } catch (Exception e) {
                //slf4j中，如果最后一个对象是异常，不用使用占位符
                log.error("同步点赞数据到mysql失败，key:{}",key,e);
                throw new RuntimeException(e);
            }
        }
    }
    // 每隔30秒执行一次，可以根据需求调整
    @Scheduled(fixedRate = 30000) //ms
    public void syncArticleLikesToDB() {
        // 获取所有key
        Set<String> keys = redisTemplate.keys("article:likes:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                Long aid = Long.valueOf(key.replace("article:likes:", ""));
                // 使用原子操作获取并删除，避免并发问题
                String deltaStr = redisTemplate.opsForValue().getAndDelete(key);
                if (deltaStr == null) continue;

                int delta = Integer.parseInt(deltaStr);
                if (delta <= 0) continue;

                // SQL 原子更新点赞数
                if(articleMapper.incrLikeCount(aid, delta)<=0){
                    throw new RuntimeException("同步点赞数据到mysql失败");
                }
//                redisTemplate.opsForValue().set(key,String.valueOf(0));
                log.info("同步文章 {} 的点赞数 {} 到数据库", aid, delta);
            } catch (Exception e) {
                //slf4j中，如果最后一个对象是异常，不用使用占位符
                log.error("同步点赞数据到mysql失败，key:{}",key,e);
                throw new RuntimeException(e);
            }
        }
    }
}
