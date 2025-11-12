package yellow.iblog.model;

import lombok.Data;

@Data
public class ArticleFavorResponse {
    //用户当前是否收藏了文章
    private Boolean status;
    //文章当前收藏数
    private int crtFavors;
}
