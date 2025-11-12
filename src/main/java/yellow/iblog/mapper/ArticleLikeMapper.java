package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import yellow.iblog.model.ArticleLike;

@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

    @Delete("DELETE from article_like WHERE aid=#{aid} AND uid=#{uid}")
    int deleteLikesByAidUid(Long aid,Long uid);
}
