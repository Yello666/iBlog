package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.CommentMapper;
import yellow.iblog.model.Comment;

import java.util.List;
import java.util.Objects;

@Service
public class CommentServiceImpl implements CommentService{
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper){
        this.commentMapper=commentMapper;
    }

    @Override
    public Comment publishComment(Comment c) {
        if(commentMapper.insert(c)>0){
            return c;
        }
        return null;
    }

    @Override
    public Boolean deleteCommentByCidAndUid(Long cid, Long uid) {
        //TODO如果是管理员就可以删
        Comment c=commentMapper.selectById(cid);
        if(c==null) return false;//评论不存在
        // 普通用户只能删除自己的评论
        if(Objects.equals(uid, c.getUid())){
            return commentMapper.deleteById(cid) > 0;
        }
        //管理员可以直接删除
//        return commentMapper.deleteById(cid) > 0;
        return false;
    }

    @Override
    public Comment replyCommentByCid(Long cid, Comment c) {
        Comment parent=commentMapper.selectById(cid);
        if(parent==null) return null;//parent不存在
        c.setParentCid(parent.getCid());
        if(commentMapper.insert(c)>0){
            return c;
        }
        return null;

    }

    //查看一个文章的所有评论
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

        return commentMapper.selectPage(commentPage, wrapper);//使用分页查询，传入分页插件和查询对象

    }

    @Override
    public List<Comment> getAllRepliesByCid(Long cid) {
        LambdaQueryWrapper<Comment> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentCid,cid)//找它的所有子回复
                .orderByAsc(Comment::getCreatedAt);//最早回复的先显示
        return commentMapper.selectList(wrapper);
    }
}
