package yellow.iblog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void testRedis(){
        redisTemplate.opsForValue().set("nihao","hello");
        String res=redisTemplate.opsForValue().get("nihao").toString();
        System.out.println(res);
    }
}
