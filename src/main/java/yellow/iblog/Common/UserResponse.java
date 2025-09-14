package yellow.iblog.Common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yellow.iblog.model.User;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse{
    private Long uid;
    private String userName;
    private Character gender;
    private Integer age;

    public UserResponse( Long uid, String userName, Character gender, Integer age) {
        this.uid = uid;
        this.userName = userName;
        this.gender = gender;
        this.age = age;
    }

    public UserResponse FromUser(User u){
        return new UserResponse(u.getUid(),u.getUserName(),u.getGender(),u.getAge());


    }
}
