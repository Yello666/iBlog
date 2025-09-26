package yellow.iblog.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value="tag_article_relations")
public class TARelations {
    @TableId(value="ta_relation_id",type= IdType.AUTO)
    private Long taRelationID;
//    @TableField(value="tid")
//    private Long tid;
    @TableField(value="tag_name")
    private String tagName;
    @TableField(value="aid")
    private Long aid;

}
