package yellow.iblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ArticleResponse {
    private Long aid;

    private String uid;//uid变成字符串，防止前端溢出

    private String title;

    private String content;

    private int likesCount;

    private int favorCount;

    private int commentsCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ArticleResponse(Article a){
        this.aid=a.getAid();
        this.uid=String.valueOf(a.getUid());
        this.title=a.getTitle();
        this.commentsCount=a.getCommentsCount();
        this.content=a.getContent();
        this.likesCount=a.getLikesCount();
        this.favorCount=a.getFavorCount();
        this.createdAt=a.getCreatedAt();
        this.updatedAt=a.getUpdatedAt();
    }


}
