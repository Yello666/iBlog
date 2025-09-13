package yellow.iblog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yellow.iblog.model.User;
import yellow.iblog.service.UserServiceImpl;

@RestController
@RequestMapping("/user")
public class UserC {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping
    public ResponseEntity<User> createUser(User u){
        User savedUser=userService.createUser(u);
        return ResponseEntity.ok(savedUser);

    }




}
