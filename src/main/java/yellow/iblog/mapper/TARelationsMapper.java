package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import yellow.iblog.model.Article;
import yellow.iblog.model.Tag;
import yellow.iblog.model.TARelations;

import java.util.List;

@Mapper
public interface TARelationsMapper extends BaseMapper<TARelations> {

    @Select("SELECT t.* FROM tags t INNER JOIN iBlog.tag_article_relations r ON t.tag_name=r.tag_name WHERE r.aid=#{aid}")
    List<Tag> FindAllTagsOfOneArticle(Long aid);

    @Select("SELECT a.aid,a.title,a.uid FROM articles a "+
            "INNER JOIN tag_article_relations r ON a.aid=r.aid "+
            "WHERE r.tag_name LIKE CONCAT('%', #{tagName}, '%')")
    List<Article> FindArticlesIncludeTag(String tagName);

    default TARelations getTARelationsByAidAndTagName(Long aid,String tagName){
        LambdaQueryWrapper<TARelations> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(TARelations::getAid,aid)
                .eq(TARelations::getTagName,tagName);
        return this.selectOne(wrapper);
    }
}
