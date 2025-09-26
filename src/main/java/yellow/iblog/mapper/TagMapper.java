package yellow.iblog.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import yellow.iblog.model.Tag;


public interface TagMapper extends BaseMapper<Tag> {

    default Tag getTagByTagName(String tagName){
        LambdaQueryWrapper<Tag> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getTagName,tagName);
        return this.selectOne(wrapper);
    }
}
