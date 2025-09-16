package yellow.iblog.model;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type= IdType.ASSIGN_ID)
    private Long uid;
    @TableField("user_name")
    private String userName;
    @TableField("gender")
    private Character gender;

    @TableField("age")
    private Integer age;

    @TableField("password") //存储hash值，需要长一点
    private String password;

    // 新增创建时间字段
    @TableField(value = "created_at",fill=FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 新增更新时间字段
    @TableField(value="updated_at",fill=FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    //构造函数
    public User( String userName, Character gender, Integer age, String password) {
        this.userName = userName;
        this.gender = gender;
        this.age = age;
        this.password = password;
    }

    public User(String userName, Character gender, Integer age) {
        this.userName = userName;
        this.gender = gender;
        this.age = age;
    }

    public User(){}

}







//虽然JPA可以自动化更改数据库，并且一键生成数据库图表，一键生成sql文件，着实方便，但是不支持MyBatis-PLUS。。。
//建议拿JPA来建库
////Java Persistence API 就是操作持久化存储的API
//@Entity//JPA，可以使用Table，Column等注解来操作数据库结构
////就是要使用JPA，因为它提供了自动修改数据库的接口，可以自动创建数据库，还有主键注解Id，列名注解Column
//@Data//MyBatis注解
//@Table(name="users")
////@Setter
////@Getter//JPA注解，可以为所有类添加getter和setter，但是Mybatis的Data注解已经包括
//public class User {
//
//
//    @Id//id注解是主键
//    private Long uid;
//
//    @Column(name="user_name",length=20,nullable = false)
//    private String userName;
//
//    @Column(name="gender",length=1)
//    private Character gender;
//
//    @Column(name="age")
//    private Integer age;
//
//    @Column(name="password",nullable = false) //存储hash值，需要长一点
//    private String password;
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
//    //因为插入数据的时候是使用MyBatis的@Mapper注解来插入，所以这个JPA的注解是不会运行的，需要手动在service层插入
////    @PrePersist
////    protected void onCreate(){
////        createdAt=LocalDateTime.now();
////        updatedAt=LocalDateTime.now();
////    }
////    @PreUpdate
////    protected void onUpdate(){
////        updatedAt=LocalDateTime.now();
////    }
//
//    //构造函数
//    public User( String userName, Character gender, Integer age, String password) {
//        this.userName = userName;
//        this.gender = gender;
//        this.age = age;
//        this.password = password;
//    }
//    public User(){}
//}
