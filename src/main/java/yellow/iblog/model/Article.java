package yellow.iblog.model;

import java.time.LocalDateTime;
/*、
文章实体类，包括ID
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("articles")
public class Article {
    // 插入数据库成功后，MyBatis-Plus 会把数据库生成的自增ID回填到 article.aid
    @TableId(type= IdType.AUTO)
    private Long aid;

    @TableField(value="uid")
    private Long uid;

    @TableField(value = "title")
    private String title;

    @TableField(value="content")
    private String content;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value="updated_at")
    private LocalDateTime updatedAt;

    public Article(){}

    public Article(Long uid,String title, String content) {
        this.uid=uid;
        this.title = title;
        this.content = content;
    }
    public Article(Long aid,Long uid,String title, String content) {
        this.aid=aid;
        this.title = title;
        this.content = content;
    }
}

/*、
文章实体类，包括ID
// */
//@Entity
//@Getter
//@Setter
//@Table(name = "articles")
//public class Article {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO) //自动增长的主键
//    private Long aid;
//
//    @Column(name="articleName",nullable = false)
//    private String articleName;
//
//    @Column(name = "title",nullable = false)
//    private String title;
//
//    @Column(name="content")
//    private String content;
//
//    // 新增创建时间字段
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    // 新增更新时间字段
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;
//
//
//
//    public Article(){}
//
//    public Article(Long ID, String articleName, String content) {
//        this.aid = ID;
//        this.articleName = articleName;
//        this.content = content;
//    }
//}
