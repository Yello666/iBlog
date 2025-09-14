package yellow.iblog.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import yellow.iblog.model.User;

//由于MyBatis修改字段是全部都修改，选择性修改是要使用XML，并且要手写sql语句，时间浪费且麻烦
//所以使用MyBatis-Plus，可以动态修改字段值
//只需要继承BaseMapper就可以

public interface UserMapper extends BaseMapper<User> {

}




//一旦继承了BaseMapper立刻得道成佛，下面写的方法全部都有，哈哈哈！！！


//public interface UserMapper {
//    @Insert("INSERT INTO users (uid,user_name, gender, age, password,created_at,updated_at) VALUES (#{uid},#{userName},#{gender},#{age},#{password},#{createdAt},#{updateAt})")
//    int createUser(User user);//这个mybatis的Insert注解默认返回的是影响的行数
//
//    @Select("SELECT uid,user_name, gender, age, password FROM users WHERE uid=#{uid}")
//    User getUserByUid(Integer uid);
//
//   @Update("UPDATE users SET ")
//    User updateUser(User u);
//
//    @Update("")
//    User updateUserPassword(User u);
//
//    @Delete("")
//    User deleteUserByUid(Integer uid);
//
//}
