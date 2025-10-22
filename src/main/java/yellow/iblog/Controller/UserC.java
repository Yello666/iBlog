package yellow.iblog.Controller;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import yellow.iblog.Common.ApiResponse;
import yellow.iblog.model.UpdatePswRequest;
import yellow.iblog.model.UserResponse;
import yellow.iblog.model.LoginInfo;
import yellow.iblog.model.LoginResponse;
import yellow.iblog.model.User;
import yellow.iblog.service.UserServiceImpl;

@Slf4j
@RestController
//@RequestMapping("/user")
public class UserC {

    private final UserServiceImpl userService;
    // 推荐：构造函数注入
    public UserC(UserServiceImpl userService) {
        this.userService = userService;
    }

    //用户注册
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody User u){
        User savedUser=userService.createUser(u);
        if(savedUser!=null){
            log.info("{}用户{}成功注册",savedUser.getRole(),savedUser.getUid());
            UserResponse r=new UserResponse().FromUser(u);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.badRequest().body(ApiResponse.fail("error"));
        }
    }
    //用户登陆,使用POST更加安全
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> userLogin(@RequestBody LoginInfo loginInfo){
        ApiResponse<LoginResponse> result=userService.userLogin(loginInfo);
        if(result.IsSuccess()){
            log.info("用户{}登陆成功",loginInfo.getUserName());
            return ResponseEntity.ok(result);

        } else{
            log.warn("用户{}登陆失败",loginInfo.getUserName());
            return ResponseEntity.badRequest().body(result);
        }
    }
    //用户获取某个用户的信息
    @GetMapping("/user/{uid}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUid(@PathVariable Long uid){
        User savedUser=userService.getUserByUid(uid);
        if(savedUser!=null){
            UserResponse r=new UserResponse().FromUser(savedUser);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户未注册"));
        }
    }

    //用户注销自己的账号
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/user")
    public ResponseEntity<ApiResponse<Boolean>> deleteUserByUid(Authentication authentication){
        Long uid = Long.valueOf(authentication.getName());// 从 token (SecurityContext) 中拿 uid
        boolean ok=userService.deleteUserByUid(uid);
        if(ok){
            log.info("用户{}注销了",uid);
            return ResponseEntity.ok(ApiResponse.success(true));
        } else{
            return ResponseEntity.badRequest().body(ApiResponse.fail("error"));
        }
    }
    //管理员注销别人的账号
    @DeleteMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteUserByAdmin(@RequestParam Long uid) {
        boolean ok = userService.deleteUserByUid(uid);

        if (ok) {
            return ResponseEntity.ok(ApiResponse.success(true));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("删除失败"));
        }
    }

//    @PreAuthorize("#u.uid == authentication.name")
//     时间顺序问题：Spring Security在执行权限检查时，方法参数还没有被解析和绑定，在这里写是不行的

     //用户修改自己的个人信息
    @PutMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@RequestBody User u){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        Long crtUid=Long.valueOf(authentication.getName());
        if(!crtUid.equals(u.getUid())){
            log.warn("用户{}试图修改用户{}的信息",crtUid,u.getUid());
            throw new AccessDeniedException("用户"+crtUid+"试图修改其它用户的信息");
        }
        User savedUser=userService.updateUser(u);
        if(savedUser!=null){
            UserResponse r=new UserResponse().FromUser(u);
            return ResponseEntity.ok(ApiResponse.success(r));
        } else{
            return ResponseEntity.badRequest().body(ApiResponse.fail("error"));
        }
    }

    //用户修改自己的密码
    @PutMapping("/user/psw")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> updatePassword(@RequestBody UpdatePswRequest updatePswRequest){
        boolean ok=userService.updateUserPassword(updatePswRequest);
        if(ok){
            return ResponseEntity.ok(ApiResponse.success(true));
        } else{
            return ResponseEntity.badRequest().body(ApiResponse.fail("原始密码不正确，请重新输入"));
        }
    }




}
