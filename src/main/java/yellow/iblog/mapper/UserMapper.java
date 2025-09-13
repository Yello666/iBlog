package yellow.iblog.mapper;

import org.apache.ibatis.annotations.*;
import yellow.iblog.model.User;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO users (uid,user_name, gender, age, password) VALUES (#{uid},#{userName},#{gender},#{age},#{password})")
    int createUser(User user);//这个mybatis的Insert注解默认返回的是影响的行数

    @Select("")
    User getUserByUid(Integer uid);

   @Update("")
    User updateUser(User u);

    @Update("")
    User updateUserPassword(User u);

    @Delete("")
    User deleteUserByUid(Integer uid);

}
