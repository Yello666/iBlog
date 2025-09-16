package yellow.iblog.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("users")
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
    private Long parent_cid;//上一级评论的cid

    @TableField("created_at")
    private LocalDateTime createdAt;


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
//    private Long parent_cid;
//
//    @Column(name="created_at",nullable = false)
//    private LocalDateTime createdAt;
//
//
//}
