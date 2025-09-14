package yellow.iblog.service;

import yellow.iblog.Common.UpdatePswRequest;
import yellow.iblog.model.User;

public interface UserService {
    User createUser(User u);
    User getUserByUid(Long uid);
    User updateUser(User u);
    boolean updateUserPassword(UpdatePswRequest request);
    boolean deleteUserByUid(Long uid);

}
