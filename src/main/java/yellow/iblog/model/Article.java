package yellow.iblog.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
/*、
文章实体类，包括ID
 */
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("articles")
public class Article{
//    @Serial 这样序列化之后在redis看是二进制，不方便查看
//    private static final long serialVersionUID = 1L;
    // 插入数据库成功后，MyBatis-Plus 会把数据库生成的自增ID回填到 article.aid
    @TableId(type= IdType.AUTO)
    private Long aid;

    @TableField(value="uid")
    private Long uid;

    @TableField(value = "title")
    private String title;

    @TableField(value="content")
    private String content;

    @TableField(value="likes_count")//点赞数
    private int likesCount;//如果是int的话默认为0，如果是Integer的话默认为null

    @TableField(value="favor_count")//收藏数
    private int favorCount;

    @TableField(value="comments_count")//评论数
    private int commentsCount;

    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value="updated_at",fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Article(){}

    public Article(Long uid,String title, String content) {
        this.uid=uid;
        this.title = title;
        this.content = content;
        this.favorCount=0;
        this.commentsCount=0;
        this.likesCount=0;
    }
    public Article(Long aid,Long uid,String title, String content) {
        this.uid=uid;
        this.aid=aid;
        this.title = title;
        this.content = content;
        this.favorCount=0;
        this.commentsCount=0;
        this.likesCount=0;
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
