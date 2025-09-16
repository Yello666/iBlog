package yellow.iblog.service;
import org.springframework.stereotype.Service;
import yellow.iblog.Common.UpdatePswRequest;
import yellow.iblog.Common.utils;
import yellow.iblog.mapper.UserMapper;
import yellow.iblog.model.User;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService {

//    @Autowired 依赖注入，会去找有没有userMapper这个类型的bean类，找到了就new一个对象给到userMapper
//    好处就是不用自己new，不用改，坏处是不透明，不知道用了哪一个bean
//    所以又改成了构造函数注入,通过传递的参数就知道是userMapper类，而不是userMapperV2类
    private final UserMapper userMapper;
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper=userMapper;
    }

    @Override
    public User createUser(User u){//虽然接口那个地方没有写public，但是那里默认方法是public，所以这里一定要写public
        u.setPassword(utils.Encode(u.getPassword()));
//        u.setUid(utils.GenerateIDBySnowFlake());MyBatis-Plus已经实现
//        u.setCreatedAt(LocalDateTime.now());
//        u.setUpdatedAt(LocalDateTime.now());
        if(userMapper.insert(u)>0){
            return u;
        } else{
            return null;
        }
    }
    @Override
    public User getUserByUid(Long uid){
        //TODO 搭载redis缓存
        User u=userMapper.selectById(uid);
        //此时的u包含密码，创建时间，更改时间，这三个不需要的值，所以要设置为null吗？--在controller层会解决
        return u;

    }
    @Override
    public User updateUser(User u){
        u.setUpdatedAt(LocalDateTime.now());
        if(userMapper.updateById(u)>0){
            return u;
        } else{ //影响行数为0，就是没有找到user，user没有注册
            return null;
        }
    }
    @Override
    public boolean updateUserPassword(UpdatePswRequest request){
        //oldPsw是用户输入的之前的密码，newPsw是用户输入的新密码
        //获取之前的哈希密码
        System.out.println("updatePassword");
        User u=userMapper.selectById(request.getUid());
        if(utils.Match(request.getOldPsw(),u.getPassword())){//如果用户输入的密码加盐哈希之后，和之前存储的密码一样，那么就说明输对了
            System.out.println("right password");
            u.setPassword(utils.Encode(request.getNewPsw()));//修改密码为加密过的新密码
            u.setUpdatedAt(LocalDateTime.now());
            return userMapper.updateById(u) > 0;//存储
        }
        return false;

    }
    @Override
    public boolean deleteUserByUid(Long uid){
        return userMapper.deleteById(uid) > 0;//如果返回0，就说明没有这个用户，无法删除

    }


}
