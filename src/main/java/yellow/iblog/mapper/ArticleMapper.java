package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import yellow.iblog.model.Article;

import java.util.List;


@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
//    @Select("SELECT aid,uid,title,content,created_at from articles WHERE uid=#{uid}")
//    public List<Article> getArticleByUid(Long uid);
// 直接用 queryWrapper 查询 uid也有方法，这里也不用写




}
