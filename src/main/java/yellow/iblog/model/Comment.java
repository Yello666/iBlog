package yellow.iblog.model;

import com.baomidou.mybatisplus.annotation.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data//包含getter和setter，但是不包括构造函数
@TableName("comment")
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @TableId(type= IdType.AUTO)
    private Long cid;

    @TableField("contents")
    private String contents;

    @TableField("aid")
    private Long aid;

    @TableField("uid")
    private Long uid;

    @TableField("parent_id")
    private Long parentCid;//上一级评论的cid

    @TableField(value="created_at",fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value="likes_count")
    private int likesCount;

    public Comment(String contents, Long aid, Long uid, Long parent_cid) {
        this.contents = contents;
        this.aid = aid;
        this.uid = uid;
        this.parentCid = parent_cid;
        this.likesCount=0;
    }

}



//@Data
//@Entity
//
//public class Comment {
//    @Id
//    private Long cid;
//
//    @Column(name="contents",nullable = false)
//    private String contents;
//
//    @Column(name="aid",nullable = false)
//    private Long aid;
//
//    @Column(name="uid",nullable = false)
//    private Long uid;
//
//    @Column(name="parent_id")
//    private Long parentCid;
//
//    @Column(name="created_at",nullable = false)
//    private LocalDateTime createdAt;
//
//
//}
