package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.mapper.CommentMapper;
import yellow.iblog.model.Comment;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor//自动为final、notnull的变量声明构造器
public class CommentServiceImpl implements CommentService{
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final LikeService likeService; // 注入LikeService
    private final RedisService redisService;
    private final RedisTemplate redisTemplate;

    //点赞数增量从redis获取，redis的点赞数定期相加存储到mysql中
    @Override
    public Comment getCommentByCid(Long cid) {
        //1.先读缓存
        Comment cacheC=(Comment) redisService.checkCache("comment",cid);
        if(cacheC==null){
            //2.缓存未命中，从mysql读，并缓存
            Comment dbComment=commentMapper.selectById(cid);
            if(dbComment==null){
                log.warn("尝试查询不存在的评论,cid:{}",cid);
                return null;
            } else{
                //3.设置当前点赞数
                int redisCacheLikes=Math.toIntExact(likeService.getCommentLikeCount(cid));
                int redisCacheUnlikes=Math.toIntExact(likeService.getCommentUnLikeCount(cid));
                int dbLikes=dbComment.getLikesCount();
                dbComment.setLikesCount(redisCacheLikes+dbLikes-redisCacheUnlikes);
                //4.手动缓存
                if(redisService.addCache("comment",cid,dbComment)){
                    log.info("缓存评论成功");
                } else{
                    log.warn("缓存评论失败");
                }
                return dbComment;
            }
        } else{
            //5.如果存在评论缓存，需要返回点赞数为缓存comment的点赞数+redisLike-redisUnLike
            int redisCacheLikes=Math.toIntExact(likeService.getCommentLikeCount(cid));
            int redisCacheUnlikes=Math.toIntExact(likeService.getCommentUnLikeCount(cid));
            cacheC.setLikesCount(redisCacheLikes+cacheC.getLikesCount()-redisCacheUnlikes);
            log.info("目前redis评论点赞缓存里面的点赞数:{},redisComment缓存里面的点赞数:{}," +
                    "redis取消评论点赞缓存里面的点赞数:{}",redisCacheLikes,cacheC.getLikesCount(),redisCacheUnlikes);
            return cacheC;
        }

    }
    @Transactional
    @Override
    public Integer UnLikeComment(Long cid) {
        Comment c=commentMapper.selectById(cid);
        if(c==null){
            log.error("评论{}不存在",cid);
            throw new RuntimeException("尝试取消点赞不存在的评论");
        }
        Long deltaLikes= likeService.unlikeComment(cid);
        if(deltaLikes<=0) {//将点赞数存到redis中
            log.error("存储取消点赞数到redis失败,cid:{}",cid);
            throw new RuntimeException("点赞失败");
        }
        return Math.toIntExact(deltaLikes);
    }

    @Override
    @Transactional
    public Integer LikeComment(Long cid) {
        Comment c=commentMapper.selectById(cid);//selectById(cid) 基于主键查询，在有索引的情况下是O(1)复杂度
        //所以不需要缓存
        if(c==null){
            log.error("评论{}不存在",cid);
            throw new RuntimeException("尝试点赞不存在的评论");
        }
        Long deltaLikes= likeService.likeComment(cid);
        if(deltaLikes<=0) {//将点赞数存到redis中
            log.error("存储点赞数到redis失败,cid:{}",cid);
            throw new RuntimeException("点赞失败");
        }
        return Math.toIntExact(deltaLikes);
    }

    //写的时候不需要添加缓存，因为会返回data，就等于有缓存了
    @Override
    @Transactional //事务标签
    public Comment publishComment(Comment c) {
        if(commentMapper.insert(c)<=0){
            log.error("数据库操作插入评论失败");
            throw new RuntimeException("数据库操作插入评论失败");//要throw异常，才会被认为事务失败，会回滚
        }
        if(articleMapper.addComments(c.getAid())<=0){
            log.error("数据库操作，添加评论数失败");
            throw new RuntimeException("数据库操作，添加评论数失败");

        }
        return c;
    }

    @Override
    @CacheEvict(value="comment",key="#cid")
    @Transactional
    public Boolean deleteCommentByCid(Long cid) {
        Comment c=commentMapper.selectById(cid);
        if(c==null){
            throw new RuntimeException("尝试删除一条不存在的评论");
        }
        if(commentMapper.deleteById(cid)<=0){
            throw new RuntimeException("数据库操作:删除评论失败");
        }
        if(articleMapper.deleteComments(c.getAid())<=0){
            throw new RuntimeException("数据库操作:减少评论数失败");
        }
        return true;
    }

    //cid:要评论的评论id，c：发表的评论
    @Override
    @Transactional
    public Comment replyCommentByCid(Long cid, Comment c) {
        Comment parent=commentMapper.selectById(cid);
        if(parent==null){
            log.error("尝试评论一条不存在的评论");
            throw new RuntimeException("尝试评论一条不存在的评论");
        }
        c.setAid(parent.getAid());//将发表评论的文章id设置成上级评论的所属文章id
        c.setParentCid(parent.getCid());//将发表的评论的上级评论id设置一下
        if(commentMapper.insert(c)<=0){
            throw new RuntimeException("数据库操作:增加评论失败");
        }
        if(articleMapper.addComments(parent.getAid())<=0){
            throw new RuntimeException("数据库操作:增加评论数失败");
        }
        return c;

    }

    //查看一个文章的所有评论
//    @Cacheable(value="comment",key="#aid + '_' + #page + '_' + #size",unless="#result==null")
    //分页就不要做缓存了，太麻烦了，还要改缓存呢
    @Override
    public Page<Comment> getCommentsByAid(Long aid, int page, int size) {
        // 使用分页插件
        Page<Comment> commentPage = new Page<>(page, size);//分页插件，page是第几页，size是页面有多少个对象
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();//这个是查询对象
        //使用wrapper来限制查询条件
        //Comment引用的代表数据库里面的数据
        wrapper.eq(Comment::getAid, aid)  //查询目标aid的数据
                .isNull(Comment::getParentCid) // 只查没有上级评论的评论，即顶级评论
                .orderByDesc(Comment::getCreatedAt);//查询需要按照时间排序降序排序，即越晚发布的越先显示

        // 获取评论分页数据
        Page<Comment> comments = commentMapper.selectPage(commentPage, wrapper);

        // 更新点赞数(redisLikes+数据库-redisUnLikes）
        for (Comment comment : comments.getRecords()) {
            String likeKey = "comment:likes:" + comment.getCid();
            String unlikeKey="comment:unlikes:" + comment.getCid();
            Integer likeCount =(Integer)redisTemplate.opsForValue().get(likeKey);
            Integer unlikeCount =(Integer) redisTemplate.opsForValue().get(unlikeKey);
            likeCount=likeCount==null?0 :likeCount;
            unlikeCount=unlikeCount==null? 0:unlikeCount;
            comment.setLikesCount(comment.getLikesCount()+likeCount-unlikeCount);  // 设置评论的点赞数
        }

        return comments;
    }

    @Override
    @Cacheable(value="comment",key="#cid",unless="#result==null")
    public List<Comment> getAllRepliesByCid(Long cid) {
        LambdaQueryWrapper<Comment> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentCid,cid)//找它的所有子回复
                .orderByAsc(Comment::getCreatedAt);//最早回复的先显示
        return commentMapper.selectList(wrapper);
    }
}
