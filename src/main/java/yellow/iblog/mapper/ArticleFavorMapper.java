package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import yellow.iblog.model.ArticleFavor;

@Mapper
public interface ArticleFavorMapper extends BaseMapper<ArticleFavor> {
    @Delete("DELETE from article_favor WHERE aid=#{aid} AND uid=#{uid}")
    int deleteFavorsByAidUid(Long aid,Long uid);
}
