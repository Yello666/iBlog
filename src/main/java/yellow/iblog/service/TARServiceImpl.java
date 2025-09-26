package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.TARelationsMapper;
import yellow.iblog.mapper.TagMapper;
import yellow.iblog.mapper.ArticleMapper;
import yellow.iblog.model.Article;
import yellow.iblog.model.TARelations;
import yellow.iblog.model.Tag;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TARServiceImpl implements TARelationService {

    private final TARelationsMapper taRelationsMapper;
    private final TagMapper tagMapper;
    private final ArticleMapper articleMapper;

    /**
     * 给文章添加标签
     */
    @Override
    public Integer addTagsToArticle(List<String> tags, Long aid) {
        Integer count=0;
        for(String tagName:tags){
            // 先查 tag 是否存在
            Tag tag = tagMapper.getTagByTagName(tagName);
            if (tag == null) {
                log.warn("标签不存在，无法绑定: {}", tagName);
                continue;
            }

            // 检查是否已经存在关系，避免重复插入
            TARelations exist = taRelationsMapper.getTARelationsByAidAndTagName(aid,tagName);
            if (exist != null) {
                log.info("文章 {} 已经绑定过标签 {}", aid, tagName);
                continue;
            }

            // 插入关系
            TARelations relation = new TARelations();
            relation.setAid(aid);

            relation.setTagName(tag.getTagName());


            if (taRelationsMapper.insert(relation) > 0) {
                log.info("文章 {} 添加标签 {}", aid, tagName);
                count++;
            } else{
                log.error("文章 {} 添加标签 {} 失败", aid, tagName);
            }

        }
        return count;

    }

    /**
     * 移除文章上的标签
     */
    @Override
    public Integer deleteTagsFromArticle(List<String> tags, Long aid) {
        Integer count=0;
        for(String tagName:tags){
            Tag tag = tagMapper.getTagByTagName(tagName);
            if (tag == null) {
                log.warn("标签不存在，无法解绑: {}", tagName);
                continue;
            }

            int rows = taRelationsMapper.delete(
                    new LambdaQueryWrapper<TARelations>()
                            .eq(TARelations::getAid, aid)
                            .eq(TARelations::getTagName,tagName)
            );
            if (rows > 0) {
                log.info("文章 {} 移除了标签 {}", aid, tagName);
                TARelations relation = new TARelations();
                relation.setAid(aid);
                relation.setTagName(tagName);
                count++;
            } else {
                log.warn("文章 {} 没有绑定过标签 {}", aid, tagName);
            }
        }
        return count;

    }

    /**
     * 查询某篇文章的所有标签
     */
    @Override
    public List<Tag> getAllTagsOfOneArticle(Long aid) {
        return taRelationsMapper.FindAllTagsOfOneArticle(aid);
    }

    /**
     * 查询包含某个标签的所有文章
     */
    @Override
    public List<Article> getArticlesIncludeTag(String tagName) {
        return taRelationsMapper.FindArticlesIncludeTag(tagName);
    }


    /**
     * 查询包含一组标签的文章（交集/并集）
     * 这里先给你一个并集的实现（任意一个标签命中的文章都会返回）
     */
   //先不实现这个
    @Override
    public List<Article> getArticlesIncludeTagList(List<Tag> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return List.of();
        }
        // 并集：收集所有文章
        return articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .inSql(Article::getAid,
                                "SELECT DISTINCT aid FROM tag_article_relations WHERE tid IN (" +
                                        tagList.stream().map(t -> t.getTid().toString()).reduce((a, b) -> a + "," + b).orElse("0")
                                        + ")"
                        )
        );
    }
}
