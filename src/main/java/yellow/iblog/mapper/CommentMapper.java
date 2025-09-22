package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import yellow.iblog.model.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
//    default void LikesComment(Long cid){
//        UpdateWrapper<Comment> wrapper=new UpdateWrapper<>();
//        wrapper.setSql("likes_count=likes_count+1")
//                .eq("cid",cid);
//    }
    // 点赞数增加 delta
    @Update("UPDATE comments SET likes_count = likes_count + #{delta} WHERE cid = #{cid}")
    int incrLikeCount(Long cid,int delta);


}
