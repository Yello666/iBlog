package yellow.iblog.service;

import yellow.iblog.model.User;

public interface UserService {
    User createUser(User u);
    User getUserByUid(Long uid);
    User updateUser(User u);
    User updateUserPassword(User u);
    boolean deleteUserByUid(Long uid);

}
