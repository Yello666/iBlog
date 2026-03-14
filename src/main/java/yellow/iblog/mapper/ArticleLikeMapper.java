package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import yellow.iblog.model.ArticleLike;

@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

    @Delete("DELETE from article_like WHERE aid=#{aid} AND uid=#{uid}")
    int deleteLikesByAidUid(Long aid, Long uid);

    /** 重复 (aid,uid) 时忽略插入，避免并发或重试导致 Duplicate key 报错 */
    @Insert("INSERT IGNORE INTO article_like (uid, aid, created_at) VALUES (#{uid}, #{aid}, #{createdAt})")
    int insertIgnore(ArticleLike articleLike);
}
