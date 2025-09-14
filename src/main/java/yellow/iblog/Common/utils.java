package yellow.iblog.Common;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
通用工具类，包含密码加密，密码验证，雪花算法生成ID的函数
 */
public class utils {

    public static String Encode(String str){
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        return encoder.encode(str);
    }

    public static boolean Match(String rawStr,String encodedStr){
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        return encoder.matches(rawStr,encodedStr);
    }

    public static Long GenerateIDBySnowFlake(){
        Snowflake snowflake= IdUtil.getSnowflake(1,1);
        return snowflake.nextId();

    }


}
