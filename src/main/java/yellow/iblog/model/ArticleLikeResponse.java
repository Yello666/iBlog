package yellow.iblog.model;

import lombok.Data;

@Data
public class ArticleLikeResponse {
    //用户当前是否点赞了文章
    private Boolean status;
    //文章当前点赞数
    private long crtLikes;

}
