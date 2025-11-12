package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import yellow.iblog.model.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    //更新当前点赞数-用于redis同步
    @Update("UPDATE comment SET likes_count=#{currentLikes} WHERE cid=#{cid}")
    int updateLikesCount(Long cid,int currentLikes);

}
