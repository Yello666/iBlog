package yellow.iblog.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="au_relations")
@Getter
@Setter
@NoArgsConstructor
public class AURelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //设计自增主键，双主键很麻烦
    private Long aurid;

    @Column(name="aid",nullable = false)
    private Long aid;

    @Column(name="uid",nullable = false)
    private Long uid;
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

//    public AURelation(){} 可以通过注解来写

    public AURelation(Long aid, Long uid) {
        this.aid = aid;
        this.uid = uid;
    }

}
