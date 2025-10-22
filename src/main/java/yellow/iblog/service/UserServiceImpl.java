package yellow.iblog.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import yellow.iblog.Common.ApiResponse;
import yellow.iblog.model.UpdatePswRequest;
import yellow.iblog.Common.Utils;
import yellow.iblog.exception.PasswordIncorrectException;
import yellow.iblog.exception.UserNotFoundException;
import yellow.iblog.mapper.UserMapper;
import yellow.iblog.jwt.JwtUtils;
import yellow.iblog.model.LoginInfo;
import yellow.iblog.model.LoginResponse;
import yellow.iblog.model.User;

import java.time.LocalDateTime;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

//    @Autowired 依赖注入，会去找有没有userMapper这个类型的bean类，找到了就new一个对象给到userMapper
//    好处就是不用自己new，不用改，坏处是不透明，不知道用了哪一个bean
//    所以又改成了构造函数注入,通过传递的参数就知道是userMapper类，而不是userMapperV2类
    private final UserMapper userMapper;
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper=userMapper;
    }

    //用户登陆
    @Override
    public ApiResponse<LoginResponse> userLogin(LoginInfo loginInfo){
        String userName=loginInfo.getUserName();
        User u=userMapper.findUserByUserName(userName);
        if(u==null){//函数可以抛出异常而不返回值，由捕获的函数处理，捕获的函数声明成什么类型，就返回什么值
            throw new UserNotFoundException("用户名不存在："+userName);
        }
        //校验密码
        String password=loginInfo.getPassword();
        if(!Utils.Match(password,u.getPassword())){
            throw new PasswordIncorrectException("用户密码不正确");
        }
        //生成token
        String token= JwtUtils.generateToken(u.getUid(),u.getUserName(),u.getRole());
        LoginResponse response=new LoginResponse();
        response.setUser(u);
        //token
        response.setToken(token);
        return ApiResponse.success(response);


    }
    @Override
    public User createUser(User u){//虽然接口那个地方没有写public，但是那里默认方法是public，所以这里一定要写public
        u.setPassword(Utils.Encode(u.getPassword()));
//        u.setUid(utils.GenerateIDBySnowFlake());MyBatis-Plus已经实现
//        u.setCreatedAt(LocalDateTime.now());
//        u.setUpdatedAt(LocalDateTime.now());
        if(userMapper.insert(u)>0){
            return u;
        } else{
            return null;
        }
    }
    //缓存user,要记得删除缓存
    @Override
    @Cacheable(value="user",key="#uid",unless="#result==null")
    //表明方法返回值是可缓存的。执行前会检查缓存Key是否存在，存在则直接返回，不执行方法。
    //value是设置缓存的名称，就是一个文件夹，key是下面的文件名
    //缓存Key将是 "user::1001" 这样的形式，真正的Key=缓存名::设置的Key
    //后面表示如果没有缓存，去查找数据库返回的User为null，那么就不缓存，可以解决缓存穿透问题
    public User getUserByUid(Long uid){
        //此时的u包含密码，创建时间，更改时间，这三个不需要的值，所以要设置为null吗？--在controller层会解决
        //service层负责查询就好了，controller可以把这个赋值给UserResponse
        User u=userMapper.selectById(uid);
        if(u==null){
            log.warn("用户{}不存在",uid);
            return null;
        } else{
            log.debug("从数据库获取了用户信息");
            return u;
        }
//        Service层应该专注于业务逻辑和数据获取，而不应该关心最终如何呈现给前端。
//        UserResponse属于表现层（Controller）的职责。
//        同一个User数据可能在不同接口中有不同的响应格式：
//        用户详情接口：需要返回完整信息
//        用户列表接口：可能只需要返回基本信息
//        内部调用：可能需要返回更多敏感信息
//        如果在Service层就固定返回UserResponse，会严重限制复用性。

    }
    @Override
    @CachePut(value="user",key="#u.uid")
    public User updateUser(User u){
        User savedU=userMapper.selectById(u.getUid());
        savedU.setUserName(u.getUserName());
        savedU.setAge(u.getAge());
        savedU.setGender(u.getGender());
        u.setUpdatedAt(LocalDateTime.now());

        if(userMapper.updateById(savedU)>0){
            return savedU;
        } else{ //影响行数为0，就是没有找到user，user没有注册
            return null;
        }
    }
    @Override
    @CacheEvict(value="user", key="#request.uid")//存储的值只会是函数返回的类型，不要存储bool，干脆删掉缓存
    public boolean updateUserPassword(UpdatePswRequest request){
        //oldPsw是用户输入的之前的密码，newPsw是用户输入的新密码
        //获取之前的哈希密码
//        System.out.println("updatePassword");
        User u=userMapper.selectById(request.getUid());
        if(Utils.Match(request.getOldPsw(),u.getPassword())){//如果用户输入的密码加盐哈希之后，和之前存储的密码一样，那么就说明输对了
//            System.out.println("right password");
            u.setPassword(Utils.Encode(request.getNewPsw()));//修改密码为加密过的新密码
            u.setUpdatedAt(LocalDateTime.now());
            return userMapper.updateById(u) > 0;//存储
        }
        return false;

    }
    @Override
    @CacheEvict(value="user",key="#uid")
    public boolean deleteUserByUid(Long uid){
        return userMapper.deleteById(uid) > 0;//如果返回0，就说明没有这个用户，无法删除
    }

    //    @Override
//    public UserDetails userLogin(LoginInfo loginInfo)throws UsernameNotFoundException {
//        String userName=loginInfo.getUserName();
//        User u=userMapper.findUserByUserName(userName);
//        if(u==null){
//            throw new UsernameNotFoundException("用户名不存在");
//        }
//        return org.springframework.security.core.userdetails.User
//                .withUsername(u.getUserName())
//                .password(u.getPassword()) // 已加密的密码
//                .roles(u.getRole().replace("ROLE_", "")) // Spring会自动补ROLE_
//                .build();
//
//    }

}
