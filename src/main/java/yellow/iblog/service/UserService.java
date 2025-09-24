package yellow.iblog.service;

import yellow.iblog.Common.ApiResponse;
import yellow.iblog.model.UpdatePswRequest;
import yellow.iblog.model.LoginInfo;
import yellow.iblog.model.LoginResponse;
import yellow.iblog.model.User;

public interface UserService {
    User createUser(User u);
    User getUserByUid(Long uid);
    User updateUser(User u);
    boolean updateUserPassword(UpdatePswRequest request);
    boolean deleteUserByUid(Long uid);
    ApiResponse<LoginResponse> userLogin(LoginInfo loginInfo);

}
