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
@TableName("article_favor")
public class ArticleFavor {
    @TableId(value="afid",type= IdType.AUTO)
    private Long afid;

    @TableField("aid")
    private Long aid;
    @TableField("uid")
    private Long uid;
    @TableField("created_at")
    private LocalDateTime createdAt;

}
