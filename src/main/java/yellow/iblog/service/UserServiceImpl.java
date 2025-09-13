package yellow.iblog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yellow.iblog.Utils.utils;
import yellow.iblog.mapper.UserMapper;
import yellow.iblog.model.User;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User createUser(User u){//虽然接口那个地方没有写public，但是那里默认方法是public，所以这里一定要写public
        //
        u.setPassword(utils.Encode(u.getPassword()));
        u.setUid(utils.GenerateIDBySnowFlake());
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
//        System.out.println(u);
        if(userMapper.createUser(u)>0){
            return u;
        } else{
            return null;
        }
    }
    @Override
    public User getUserByUid(Integer uid){
        //TODO 搭载redis缓存
        return userMapper.getUserByUid(uid);

    }
    @Override
    public User updateUser(User u){
        //TODO 需要先找到user，再更新
        return userMapper.updateUser(u);

    }
    @Override
    public User updateUserPassword(User u){
        //TODO 校验与之前的密码是否相等，相等的话就哈希加密新密码
        return userMapper.updateUserPassword(u);

    }
    @Override
    public User deleteUserByUid(Integer uid){
        //TODO 需要先找到user，再删除
        return userMapper.deleteUserByUid(uid);

    }


}
