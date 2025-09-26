package yellow.iblog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yellow.iblog.mapper.TagMapper;
import yellow.iblog.model.Tag;

@Service
@Slf4j
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper; // 自动注入

    @Override
    public Tag createTag(String tagName) {
        // 先判断是否已存在
        Tag exist = getTagByTagName(tagName);
        if (exist != null) {
            log.warn("tag 已存在: {}", tagName);
            return exist;
        }

        Tag t = new Tag(tagName);
        if (tagMapper.insert(t) <= 0) {
            log.error("创建 tag 失败: {}", tagName);
            return null;
        }
        log.info("创建了 tag: {}", tagName);
        return t;
    }

    @Override
    public Boolean deleteTagByTagName(String tagName) {
        int rows = tagMapper.delete(
                new QueryWrapper<Tag>().eq("tag_name", tagName)
        );
        if (rows > 0) {
            log.info("删除了 tag: {}", tagName);
            return true;
        } else {
            log.warn("未找到 tag: {}", tagName);
            return false;
        }
    }

    @Override
    public Boolean deleteTagByTid(Long tid) {
        int rows = tagMapper.deleteById(tid);
        if (rows > 0) {
            log.info("删除了 tag tid={}", tid);
            return true;
        } else {
            log.warn("未找到 tag tid={}", tid);
            return false;
        }
    }

    @Override
    public Tag getTagByTagName(String tagName) {
        return tagMapper.selectOne(
                new QueryWrapper<Tag>().eq("tag_name", tagName)
        );
    }

    @Override
    public Tag getTagByTID(Long tid) {
        return tagMapper.selectById(tid);
    }
}
