package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import yellow.iblog.model.CommentLike;

public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    @Delete("DELETE from comment_like WHERE cid=#{cid} AND uid=#{uid}")
    int deleteLikesByCidUid(Long cid,Long uid);


}
