package yellow.iblog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import yellow.iblog.model.User;
import yellow.iblog.service.UserService;

@SpringBootTest
class UserTests {
    @Autowired
    private UserService userService;

    @Test
    public void testCreateUser(){
        System.out.println("开始查询用户");
        User user=new User("emily",'f',16,"123456");
        User u=userService.createUser(user);
        System.out.println(u);
    }

}
