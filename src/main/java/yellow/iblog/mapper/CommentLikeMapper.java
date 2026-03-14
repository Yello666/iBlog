package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import yellow.iblog.model.CommentLike;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    @Delete("DELETE from comment_like WHERE cid=#{cid} AND uid=#{uid}")
    int deleteLikesByCidUid(Long cid, Long uid);

    /** 重复 (cid,uid) 时忽略插入，避免并发或重试导致 Duplicate key 报错 */
    @Insert("INSERT IGNORE INTO comment_like (uid, cid, created_at) VALUES (#{uid}, #{cid}, #{createdAt})")
    int insertIgnore(CommentLike commentLike);
}
