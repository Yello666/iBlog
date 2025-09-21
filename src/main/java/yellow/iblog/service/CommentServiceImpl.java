package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.mapper.CommentMapper;
import yellow.iblog.model.Article;
import yellow.iblog.model.Comment;

import java.util.List;
import java.util.Objects;
import java.util.zip.DataFormatException;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService{
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;


    public CommentServiceImpl(CommentMapper commentMapper, ArticleMapper articleMapper){
        this.commentMapper=commentMapper;
        this.articleMapper=articleMapper;
    }

    @Override
    @Cacheable(value="comment",key="#cid")
    public Comment getCommentByCid(Long cid) {
        Comment c=commentMapper.selectById(cid);
        if(c==null){
            log.error("评论{}不存在",cid);
            return null;
        }
        return c;
    }

    @Override
    @Transactional
    public Boolean LikeComment(Long cid) {
        Comment c=commentMapper.selectById(cid);
        if(c==null){
            log.error("评论{}不存在",cid);
            return false;
        }
        c.setLikesCount(c.getLikesCount()+1);
        if(commentMapper.updateById(c)<=0){
            log.error("数据库操作失败:更新评论{}点赞数失败",cid);
            return false;
        }
        return true;
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
    @Override
    @Cacheable(value="comment",key="#aid + '_' + #page + '_' + #size",unless="#result==null")
    public Page<Comment> getCommentsByAid(Long aid, int page, int size) {
        // 使用分页插件
        Page<Comment> commentPage = new Page<>(page, size);//分页插件，page是第几页，size是页面有多少个对象
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();//这个是查询对象
        //使用wrapper来限制查询条件
        //Comment引用的代表数据库里面的数据
        wrapper.eq(Comment::getAid, aid)  //查询目标aid的数据
                .isNull(Comment::getParentCid) // 只查没有上级评论的评论，即顶级评论
                .orderByDesc(Comment::getCreatedAt);//查询需要按照时间排序降序排序，即越晚发布的越先显示

        return commentMapper.selectPage(commentPage, wrapper);//使用分页查询，传入分页插件和查询对象

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
