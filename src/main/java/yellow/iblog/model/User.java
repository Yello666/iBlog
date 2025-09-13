package yellow.iblog.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;


//Java Persistence API 就是操作持久化存储的API
@Entity//JPA，可以使用Table，Column等注解来操作数据库结构
//就是要使用JPA，因为它提供了自动修改数据库的接口，可以自动创建数据库，还有主键注解Id，列名注解Column
@Data//MyBatis注解
@Table(name="users")
//@Setter
//@Getter//JPA注解，可以为所有类添加getter和setter，但是Mybatis的Data注解已经包括
public class User {


    @Id//id注解是主键
    private Long uid;

    @Column(name="user_name",length=20,nullable = false)
    private String userName;

    @Column(name="gender",length=1)
    private Character gender;

    @Column(name="age")
    private Integer age;

    @Column(name="password",nullable = false) //存储hash值，需要长一点
    private String password;

    // 新增创建时间字段
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 新增更新时间字段
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    //因为插入数据的时候是使用MyBatis的@Mapper注解来插入，所以这个JPA的注解是不会运行的，需要手动在service层插入
//    @PrePersist
//    protected void onCreate(){
//        createdAt=LocalDateTime.now();
//        updatedAt=LocalDateTime.now();
//    }
//    @PreUpdate
//    protected void onUpdate(){
//        updatedAt=LocalDateTime.now();
//    }

    //构造函数
    public User( String userName, Character gender, Integer age, String password) {
        this.userName = userName;
        this.gender = gender;
        this.age = age;
        this.password = password;
    }
    public User(){}
}
