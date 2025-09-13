package yellow.iblog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/*、
文章实体类，包括ID
 */
@Entity
@Getter
@Setter
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) //自动增长的主键
    private Long aid;

    @Column(name="articleName",nullable = false)
    private String articleName;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name="content")
    private String content;

    // 新增创建时间字段
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 新增更新时间字段
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate(){
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate(){
        updatedAt=LocalDateTime.now();
    }

    public Article(){}

    public Article(Long ID, String articleName, String content) {
        this.aid = ID;
        this.articleName = articleName;
        this.content = content;
    }
}
