package yellow.iblog.service;

import yellow.iblog.model.User;

public interface UserService {
    User createUser(User u);
    User getUserByUid(Integer uid);
    User updateUser(User u);
    User updateUserPassword(User u);
    User deleteUserByUid(Integer uid);

}
