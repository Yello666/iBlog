package yellow.iblog.service;

import yellow.iblog.model.Tag;

public interface TagService {
    Tag createTag(String tagName);
    Boolean deleteTagByTagName(String tagName);
    Boolean deleteTagByTid(Long tid);
    Tag getTagByTagName(String tagName);
    Tag getTagByTID(Long tid);

}
