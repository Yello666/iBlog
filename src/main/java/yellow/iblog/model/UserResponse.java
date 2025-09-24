package yellow.iblog.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse{
    private Long uid;
    private String userName;
    private Character gender;
    private Integer age;
    private String role;

    public UserResponse( Long uid, String userName, Character gender, Integer age,String role) {
        this.uid = uid;
        this.userName = userName;
        this.gender = gender;
        this.age = age;
        this.role=role;
    }

    public UserResponse FromUser(User u){
        return new UserResponse(u.getUid(),u.getUserName(),u.getGender(),u.getAge(),u.getRole());


    }
}
