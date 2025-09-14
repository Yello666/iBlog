package yellow.iblog.controller;
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

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody User u){
        User savedUser=userService.createUser(u);
        if(savedUser!=null){
            UserResponse r=new UserResponse().FromUser(u);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }

    @GetMapping("/get/{uid}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUid(@PathVariable Long uid){
        User savedUser=userService.getUserByUid(uid);
        if(savedUser!=null){
            UserResponse r=new UserResponse().FromUser(savedUser);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error:cant find user"));
        }
    }

    @PostMapping("/delete/{uid}")
    public ResponseEntity<ApiResponse<Boolean>> deleteUserByUid(@PathVariable Long uid){
        boolean ok=userService.deleteUserByUid(uid);
        if(ok){
            return ResponseEntity.ok(ApiResponse.success(true));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@RequestBody User u){
        User savedUser=userService.updateUser(u);
        if(savedUser!=null){
            UserResponse r=new UserResponse().FromUser(u);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }
    @PostMapping("/update/psw")
    public ResponseEntity<ApiResponse<Boolean>> updatePassword(@RequestBody UpdatePswRequest updatePswRequest){
        boolean ok=userService.updateUserPassword(updatePswRequest);
        if(ok){
            return ResponseEntity.ok(ApiResponse.success(true));
        } else{
            return ResponseEntity.internalServerError().body(ApiResponse.fail("error"));
        }
    }




}
