package yellow.iblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import yellow.iblog.model.Comment;

import java.util.List;

public interface CommentService {
    //发布评论
    Comment publishComment(Comment c);
    //删除评论（要确定uid是不是管理员或者发布这个评论的user，如果不是的话，不能删除）
    Boolean deleteCommentByCid(Long cid);
    //回复评论
    Comment replyCommentByCid(Long cid,Comment c);
    //查看某一条评论
    Comment getCommentByCid(Long cid);
    //获得某个文章的所有回复（按时间排序）
    Page<Comment> getCommentsByAid(Long aid, int page, int size);//分页功能
    //获得某个评论的所有回复（暂时是获得一层回复，不能获取所有的回复。）
    List<Comment> getAllRepliesByCid(Long cid);
    //给评论点赞,返回当前点赞数
    Integer LikeComment(Long cid);

    //举报评论（举报之后将评论设置为不可见，并交给管理员，让管理员审核删除）

}
