package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import yellow.iblog.model.Comment;

public interface CommentMapper extends BaseMapper<Comment> {
    default void LikesComment(Long cid){
        UpdateWrapper<Comment> wrapper=new UpdateWrapper<>();
        wrapper.setSql("likes_count=likes_count+1")
                .eq("cid",cid);
    }
}
