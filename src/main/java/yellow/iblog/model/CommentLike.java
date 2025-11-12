package yellow.iblog.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("comment_like")
public class CommentLike {
    @TableId(value="clid",type= IdType.AUTO)
    private Long clid;

    @TableField("uid")
    private Long uid;

    @TableField("cid")
    private Long cid;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
