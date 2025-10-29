package yellow.iblog.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse{
    private String uid;
    private String userName;
    private Character gender;
    private Integer age;
    private String role;

    public UserResponse( User u) {
        this.uid = String.valueOf(u.getUid());
        this.userName = u.getUserName();
        this.gender = u.getGender();
        this.age = u.getAge();
        this.role=u.getRole();
    }

}
