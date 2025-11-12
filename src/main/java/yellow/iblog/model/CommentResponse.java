package yellow.iblog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import yellow.iblog.mapper.UserMapper;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long cid;
    private String uid;//uid变成字符串，防止前端溢出
    private String contents;
    private int likesCount;
    private Long parentCid;//上一级评论的cid
    private LocalDateTime createdAt;
    private boolean isLiked;
    private String userName;//加上用户名


    public CommentResponse( Comment c){
        this.cid=c.getCid();
        this.uid=String.valueOf(c.getUid());
        this.contents=c.getContents();
        this.parentCid=c.getParentCid();
        this.createdAt=c.getCreatedAt();
        this.likesCount=c.getLikesCount();
    }

}
