package yellow.iblog;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import yellow.iblog.Common.UpdatePswRequest;
import yellow.iblog.model.User;
import yellow.iblog.service.UserService;

@Slf4j
//使用这个注解可以直接使用log记录日志，它会包含下面那句代码
@SpringBootTest
class UserTests {
//    private static final Logger log= LoggerFactory.getLogger(UserTests.class);


    @Autowired
    private UserService userService;

    private final Long testID=1967172287496998913L;//需要在后面标注L，不然会以为是int型

    @Test
    public void testCreateUser(){
        log.info("开始创建用户");
        User user=new User("emily",'f',16,"123456");
        User u=userService.createUser(user);
        log.info("创建了用户{}",u);
    }

    @Test
    public void testGetUserByUid(){
        System.out.println("开始根据uid查询用户");
        User u=userService.getUserByUid(testID);
        System.out.println(u);
    }

    @Test
    public void testUpdateUser(){
        System.out.println("开始修改用户信息");
        User u=new User("下雨了yu",'f',22);
        u.setUid(testID);
        User updated=userService.updateUser(u);
        System.out.println(updated);
    }
    @Test
    public void testUpdateUserPassword(){
        System.out.println("开始修改用户密码");
        UpdatePswRequest request=new UpdatePswRequest(testID,"123456","654321");
        System.out.println(userService.updateUserPassword(request));
    }
    @Test
    public void testDeleteUserByUid(){
        System.out.println("开始删除用户");
        User user=new User("emily",'f',16,"123456");
        User u=userService.createUser(user);
        System.out.println(userService.deleteUserByUid(u.getUid()));
    }


}
