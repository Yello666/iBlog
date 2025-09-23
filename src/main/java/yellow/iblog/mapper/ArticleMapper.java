package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import yellow.iblog.model.Article;
import yellow.iblog.model.Comment;

import java.util.List;


@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    default int addComments(Long aid){
        UpdateWrapper<Article> wrapper=new UpdateWrapper<>();
        wrapper.setSql("comment_count=comment_count+1")
                .eq("aid",aid);
        return this.update(null,wrapper);//第一个传null，可以动态更新

    }
    default int deleteComments(Long aid){
        UpdateWrapper<Article> wrapper=new UpdateWrapper<>();
        wrapper.setSql("comment_count=comment_count-1")
                .eq("aid",aid);
        return this.update(null,wrapper);//第一个传null，可以动态更新

    }

    // 点赞数增加 delta
    @Update("UPDATE articles SET likes_count = likes_count + #{delta} WHERE aid = #{aid}")
    int incrLikeCount(Long aid,int delta);

    // 收藏数增加 delta
    @Update("UPDATE articles SET favor_count = favor_count + #{delta} WHERE aid = #{aid}")
    int incrFavorCount(Long aid,int delta);


}
