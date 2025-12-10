package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.ArticleFavorMapper;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.model.Article;
import yellow.iblog.model.ArticleFavor;
import yellow.iblog.model.ArticleFavorResponse;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavorService {
    // 获取实时点赞数（Redis里优先）
    private final StringRedisTemplate redisTemplate;
    private final ArticleFavorMapper articleFavorMapper;
    private final ArticleMapper articleMapper;
//    //取消收藏（redis自增）但后续要减去这个数
//    public Long unFavorArticle(Long aid,Long uid) {
//        String key = "article:unfavor:" + aid;//新建一个字段，避免操作原来的字段，可能有并发问题
//        try {
//            //删除收藏集合里面的uid
//            String setKey="article:"+aid+"favor:uids:";
//            redisTemplate.opsForSet().remove(setKey,String.valueOf(uid));
//            // 存储增量（每次+1）
//            Long newCount = redisTemplate.opsForValue().increment(key, 1);
//            redisTemplate.expire(key, 2, TimeUnit.MINUTES); // 设置过期时间
//            log.info("取消收藏存入了:{}",redisTemplate.opsForValue().get(key));
//            return newCount;
//        } catch (Exception e) {
//            throw new RuntimeException("取消收藏失败", e);
//        }
//    }

    //收藏文章
    public Boolean favorArticle(Long aid, Long uid) {
        //记录收藏过aid的用户集合
        String userKey = "article:" + aid+":favor:users";
        //记录aid的收藏数量
        String countKey="article:"+aid+":favor:count";
        //记录修改过收藏的aid
        String dirtyKey="article:favor:dirty";
        //加载原始收藏数
        getArticleFavorCount(aid);
        //将aid加入修改过状态的集合
        redisTemplate.opsForSet().add(dirtyKey,String.valueOf(aid));
        try {
            //先查看用户之前是否收藏过aid
            Boolean hasFavored=getArticleIsFavored(aid,uid);
            if(!hasFavored){
                //如果没有收藏过,redis中收藏数+1,user集合加入uid
                redisTemplate.opsForValue().increment(countKey);
                redisTemplate.opsForSet().add(userKey,String.valueOf(uid));
                //记录关系
                ArticleFavor relation=new ArticleFavor();
                relation.setUid(uid);
                relation.setAid(aid);
                relation.setCreatedAt(LocalDateTime.now());
                articleFavorMapper.insert(relation);
                return true;
            } else{
                //如果之前收藏过，这一次会取消收藏,收藏数-1，user集合移出uid
                redisTemplate.opsForValue().decrement(countKey);
                redisTemplate.opsForSet().remove(userKey,String.valueOf(uid));
                //删除数据库关系
                articleFavorMapper.deleteFavorsByAidUid(aid,uid);
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("收藏失败", e);
        }
    }
    //查询用户是否收藏了文章
    public Boolean getArticleIsFavored(Long aid,Long uid){
        String setKey="article:"+aid+":favor:users";
        Boolean isFavored=redisTemplate.opsForSet().isMember(setKey,String.valueOf(uid));
//        log.info("用户{},文章{},收藏{}",uid,aid,isFavored);
        return isFavored;
    }

    //获取收藏数
    public Integer getArticleFavorCount(Long aid) {
        String countKey="article:"+aid+":favor:count";
        String countStr=redisTemplate.opsForValue().get(countKey);
        if(countStr!=null){
            //如果缓存命中
            Integer count=Integer.parseInt(countStr);
            //刷新文章存活时间
            redisTemplate.expire(countKey,2,TimeUnit.HOURS);
            return count;
        } else{
            //缓存没有命中，查询数据库
            Article a=articleMapper.selectById(aid);
            if(a==null){
                redisTemplate.opsForValue().set(countKey,"0",5,TimeUnit.MINUTES);
                return 0;
            } else{
                Integer crtFavors=a.getFavorCount();
                redisTemplate.opsForValue().set(countKey,String.valueOf(crtFavors));//添加缓存
                return crtFavors;
            }

        }
    }

}
