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
    //同步了之后要删除redis对于对象的缓存

    private final StringRedisTemplate redisTemplate;
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    //同步文章点赞数和取消点赞数到数据库的时间（单位是ms）因为schedule只能使用编译时就确定好的量
//    private static final Integer SYNC_ARTICLE_LIKE=;//1h
    //同步评论点赞和取消点赞到数据库的时间
//    private static final Integer SYNC_COMMENTS_LIKE=5400000;//1.5h

    // 同步评论点赞到数据库的时间
    @Scheduled(fixedRate = 50000)//ms
    public void syncCommentsLikesToDB() {
        // 获取所有key用来同步点赞数
        Set<String> commentIds = redisTemplate.opsForSet().members("comment:like:ids");
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }

        for (String cidStr :commentIds) {
            try {
                Long cid = Long.valueOf(cidStr);
                String likeKey="comment:likes:"+cid;
                // 使用原子操作获取并删除，避免并发问题
                String deltaStr = redisTemplate.opsForValue().getAndDelete(likeKey);
                if (deltaStr == null) continue;
                int delta = Integer.parseInt(deltaStr);
                if (delta <= 0) continue;

                // SQL 原子更新点赞数
                if(commentMapper.incrLikeCount(cid, delta)<=0){
                    throw new RuntimeException("同步评论点赞数据到mysql失败");
                }
                //删除评论缓存
                String commentCacheKey="comment::"+cid;
                redisTemplate.delete(commentCacheKey);
                log.info("同步评论 {} 的点赞数 {} 到数据库,并清理缓存{}", cid, delta,commentCacheKey);
            } catch (Exception e) {
                log.error("同步评论点赞数据到mysql失败，评论ID:{}",cidStr,e);
            } finally{
                redisTemplate.opsForSet().remove("comment:like:ids",cidStr);
            }

        }
    }

    // 同步评论取消点赞到数据库的时间
    @Scheduled(fixedRate = 50000)
    public void syncCommentsUnLikesToDB() {
        // 获取所有key
        Set<String> strCids = redisTemplate.opsForSet().members("comment:unlike:ids");
        if (strCids == null || strCids.isEmpty()) {
            return;
        }

        for (String strCid : strCids) {
            try {
                Long cid = Long.valueOf(strCid);
                String unlikeKey="comment:unlikes:"+cid;
                // 使用原子操作获取并删除，避免并发问题
                String deltaStr = redisTemplate.opsForValue().getAndDelete(unlikeKey);

                if (deltaStr == null) continue;

                int delta = Integer.parseInt(deltaStr);
                if (delta <= 0) continue;

                // SQL 原子更新点赞数
                if(commentMapper.decrLikeCount(cid, delta)<=0){
                    throw new RuntimeException("同步评论取消点赞数据到mysql失败");
                }

                log.info("同步评论 {} 的取消点赞数 {} 到数据库", cid, delta);
            } catch (Exception e) {
                log.error("同步评论点赞数据到mysql失败，评论id:{}",strCid,e);

            } finally {
                redisTemplate.opsForSet().remove("comment:unlike:ids",strCid);
            }
        }
    }


    //文章取消点赞同步
    @Scheduled(fixedRate = 10000) //ms
    public void syncArticleUnLikesToDB() {
        // 获取所有key
        Set<String> strAids = redisTemplate.opsForSet().members("article:unlike:aids");
        if (strAids == null || strAids.isEmpty()) {
            return;
        }

        for (String strAid : strAids) {
            try {
                Long aid = Long.valueOf(strAid);
                // 使用原子操作获取并删除，避免并发问题

                String unLikeKey="article:unlikes:"+aid;
                String deltaStr = redisTemplate.opsForValue().getAndDelete(unLikeKey);
                if (deltaStr == null) continue;

                int delta = Integer.parseInt(deltaStr);
                if (delta <= 0) continue;

                // SQL 原子更新点赞数
                if (articleMapper.decrLikeCount(aid, delta) <= 0) {
                    throw new RuntimeException("同步文章取消点赞数据到mysql失败");
                }

                log.info("同步文章 {} 的取消点赞数 {} 到数据库", aid, delta);
            } catch (Exception e) {
                //slf4j中，如果最后一个对象是异常，不用使用占位符
                log.error("同步文章取消点赞数据到mysql失败，key:{}",strAid, e);
            } finally{
                redisTemplate.opsForSet().remove("article:unlike:aids",strAid);
            }
        }
    }

    //同步文章的redis点赞数到mysql，并删除article的缓存
    // 每隔10s执行一次，可以根据需求调整
    @Scheduled(fixedRate = 10000) //ms
    public void syncArticleLikesToDB() {
        // 获取所有被点赞过的文章 ID
        Set<String> articleIds = redisTemplate.opsForSet().members("article:like:aids");
        if (articleIds == null || articleIds.isEmpty()) {
            return;
        }


        for (String aidStr : articleIds) {
            try {
                Long aid = Long.valueOf(aidStr);
                String likeKey = "article:likes:" + aid;
                // 使用原子操作获取并删除，避免并发问题
                String deltaStr = redisTemplate.opsForValue().getAndDelete(likeKey);
                if (deltaStr == null) continue;


                int delta = Integer.parseInt(deltaStr);
                if (delta <= 0) continue;

                // SQL 原子更新点赞数
                if(articleMapper.incrLikeCount(aid, delta)<=0){
                    throw new RuntimeException("同步文章点赞数据到mysql失败");
                }

                //  删除文章缓存，保证前端下次读取的是最新数据
                String articleCacheKey = "article::" + aid;
                redisTemplate.delete(articleCacheKey);
                log.info("同步文章 {} 的点赞数 {} 到数据库，并清理缓存 {}", aid, delta, articleCacheKey);
            } catch (Exception e) {
                log.error("同步文章点赞数据到 mysql 失败，文章ID: {}", aidStr, e);
            } finally {
                // 无论成功失败，都先移除集合里的 ID，避免下次重复处理
                redisTemplate.opsForSet().remove("article:like:aids", aidStr);
            }
        }
    }
}
