package yellow.iblog.controller;
import org.apache.ibatis.annotations.Delete;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;
import yellow.iblog.Common.UpdatePswRequest;
import yellow.iblog.Common.UserResponse;
import yellow.iblog.model.User;
import yellow.iblog.service.UserServiceImpl;

@RestController
@RequestMapping("/user")
public class UserC {

    private final UserServiceImpl userService;
    // 推荐：构造函数注入
    public UserC(UserServiceImpl userService) {
        this.userService = userService;
    }

    //用户注册
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody User u){
        User savedUser=userService.createUser(u);
        if(savedUser!=null){
            UserResponse r=new UserResponse().FromUser(u);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }

    //用户获取某个用户的信息
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUid(@PathVariable Long uid){
        User savedUser=userService.getUserByUid(uid);
        if(savedUser!=null){
            UserResponse r=new UserResponse().FromUser(savedUser);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error:cant find user"));
        }
    }

    //用户注销自己的账号
    @DeleteMapping("/{uid}")
    public ResponseEntity<ApiResponse<Boolean>> deleteUserByUid(@PathVariable Long uid){
        boolean ok=userService.deleteUserByUid(uid);
        if(ok){
            return ResponseEntity.ok(ApiResponse.success(true));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }

    //用户修改自己的个人信息
    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@RequestBody User u){
        User savedUser=userService.updateUser(u);
        if(savedUser!=null){
            UserResponse r=new UserResponse().FromUser(u);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }

    //用户修改自己的密码
    @PutMapping("/psw")
    public ResponseEntity<ApiResponse<Boolean>> updatePassword(@RequestBody UpdatePswRequest updatePswRequest){
        boolean ok=userService.updateUserPassword(updatePswRequest);
        if(ok){
            return ResponseEntity.ok(ApiResponse.success(true));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }




}
