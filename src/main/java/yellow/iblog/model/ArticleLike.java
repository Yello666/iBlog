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
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_like")
public class ArticleLike {
    @TableId(value="alid",type= IdType.AUTO)
    private Long alid;

    @TableField("uid")
    private Long uid;

    @TableField("aid")
    private Long aid;

    @TableField("created_at")
    private LocalDateTime createdAt;


}
