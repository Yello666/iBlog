package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.mapper.CommentMapper;
import yellow.iblog.mapper.UserMapper;
import yellow.iblog.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor//自动为final、notnull的变量声明构造器
public class CommentServiceImpl implements CommentService{
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final LikeService likeService; // 注入LikeService
    private final RedisService redisService;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;

    //点赞数增量从redis获取，redis的点赞数定期相加存储到mysql中
    @Override
    public CommentResponse getCommentByCid(Long cid,Long watcherUid) {
        //1.先读缓存
        Comment cacheC=(Comment) redisService.checkCache("comment",cid);
        if(cacheC==null){
            //2.缓存未命中，从mysql读，并缓存
            Comment dbComment=commentMapper.selectById(cid);
            if(dbComment==null){
                log.warn("尝试查询不存在的评论,cid:{}",cid);
                return null;
            } else{
                //3.设置当前点赞数和状态
                int crtLikes=likeService.getCommentLikeCount(cid);
                dbComment.setLikesCount(crtLikes);
                //4.手动缓存,设置评论过期时间2H
                if(redisService.addCache("comment",cid,dbComment,2)){
//                    log.info("缓存评论成功");
                } else{
                    log.warn("缓存评论失败");
                }
                //5.查询用户名，返回增加用户名
                Long uid= dbComment.getUid();
                User u=userMapper.selectById(uid);
                CommentResponse response=new CommentResponse(dbComment);
                if(u!=null){
                    response.setUserName(u.getUserName());

                } else{
                    response.setUserName("用户已注销");
                }
                if(watcherUid!=-1){
                    response.setLiked(likeService.getCommentIsLiked(cid,watcherUid));
                }
                return response;
            }
        } else{
            //如果存在评论缓存
            //5.查询用户名，返回增加用户名
            Long uid= cacheC.getUid();
            User u=userMapper.selectById(uid);
            CommentResponse response=new CommentResponse(cacheC);
            if(u!=null){
                response.setUserName(u.getUserName());
            } else{
                response.setUserName("用户已注销");
            }
            //6.更新点赞数和点赞状态
            int crtLikes=likeService.getCommentLikeCount(cid);
            response.setLikesCount(crtLikes);
            if(watcherUid!=-1){
                response.setLiked(likeService.getCommentIsLiked(cid,watcherUid));
            }
            return response;
        }

    }

    @Override
    @Transactional
    public CommentLikeResponse LikeComment(Long cid, Long uid) {
        Comment c=commentMapper.selectById(cid);//selectById(cid) 基于主键查询，在有索引的情况下是O(1)复杂度
        if(c==null){
            log.error("评论{}不存在",cid);
            throw new RuntimeException("尝试点赞不存在的评论");
        }
        Boolean status = likeService.likeComment(cid,uid);
        if(status){
            log.info("用户{}点赞了评论{}",uid,cid);
        } else{
            log.info("用户{}取消了评论{}的点赞",uid,cid);
        }
        int crtLikes=likeService.getCommentLikeCount(cid);
        CommentLikeResponse response=new CommentLikeResponse();
        response.setCrtLikes(crtLikes);
        response.setStatus(status);
        return response;

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
        //评论给热度+0.2
        String rankKey="article:likes:rank";
        redisTemplate.opsForZSet().incrementScore(rankKey,c.getAid().toString(),0.2);
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
        //删除评论给热度-0.2
        String rankKey="article:likes:rank";
        redisTemplate.opsForZSet().incrementScore(rankKey,c.getAid().toString(),-0.2);
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
    public Page<CommentResponse> getCommentsByAid(Long aid, Long watcherUid,int page, int size) {
        // 使用分页插件
        Page<Comment> commentPage = new Page<>(page, size);//分页插件，page是第几页，size是页面有多少个对象
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();//这个是查询对象
        //使用wrapper来限制查询条件
        //Comment引用的代表数据库里面的数据
        wrapper.eq(Comment::getAid, aid)  //查询目标aid的数据
                .isNull(Comment::getParentCid) // 只查没有上级评论的评论，即顶级评论
                .orderByDesc(Comment::getCreatedAt);//查询需要按照时间排序降序排序，即越晚发布的越先显示

        // 1.获取评论分页数据
        Page<Comment> comments = commentMapper.selectPage(commentPage, wrapper);

        // 2.更新点赞数
        for (Comment comment : comments.getRecords()) {
            int crtLikes=likeService.getCommentLikeCount(comment.getCid());
            comment.setLikesCount(crtLikes);  // 设置评论的点赞数
        }
        // 3. 批量查询用户名
        // 3.1 提取所有评论的uid，放到list里面
        List<Long> uidList = comments.getRecords().stream()
                .map(Comment::getUid)
                .distinct() // 去重，减少查询次数
                .collect(Collectors.toList());

        // 3.2 使用Map将list里面的uid和userName对应起来
        Map<Long, String> uidToNameMap = new HashMap<>();
        if (!uidList.isEmpty()) {
            List<User> userList = userMapper.selectBatchIds(uidList);
            for (User user : userList) {
                uidToNameMap.put(user.getUid(), user.getUserName());
            }
        }
        // 4. 转换为CommentResponse并设置用户名，convert里面是一个匿名函数，comment是输入的参数，即for循环的comment，{}里是函数逻辑
        //返回值赋值给commentList
        Page<CommentResponse> commentList=(Page<CommentResponse>)comments.convert(comment -> {
            CommentResponse response = new CommentResponse(comment);
            // 从map中获取用户名（若用户不存在，可设为默认值）
            response.setUserName(uidToNameMap.getOrDefault(comment.getUid(), "匿名用户"));
            //看当前用户是否点赞过该评论
            Boolean isLiked = false;
            if(watcherUid!=-1L){
                isLiked=likeService.getCommentIsLiked(response.getCid(),watcherUid);
            }
//            log.info("用户{}点赞过评论吗？:{}",watcherUid,isLiked);
            response.setLiked(isLiked);
            return response;
        });

        return commentList;
    }

    //获取一条评论的所有第一层回复
    @Override
    @Cacheable(value="comment",key="#cid",unless="#result==null")
    public List<Comment> getAllRepliesByCid(Long cid) {
        LambdaQueryWrapper<Comment> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentCid,cid)//找它的所有子回复
                .orderByAsc(Comment::getCreatedAt);//最早回复的先显示
        return commentMapper.selectList(wrapper);
    }
}
