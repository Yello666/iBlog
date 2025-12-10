package yellow.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String,Object> redisTemplate;


    public Object checkCache(String type,Long id){
        try {
            String key = type + "::" + id;
            Object object=redisTemplate.opsForValue().get(key);
            if(object!=null){
//                log.info("缓存命中");
                return object;
            }
        } catch (Exception e){
            log.error("查找缓存失败",e);
            return null;
        }
//        log.warn("缓存未命中");
        return null;
    }
    public Boolean addCache(String type,Long id,Object value,Integer Hours){
        if(value==null){
            log.error("缓存的value为null");
            return false;
        } else{
            try {
                String key = type + "::" + id;
                redisTemplate.opsForValue().set(key, value, Hours,TimeUnit.HOURS);
                return true;
            } catch (Exception e){
                log.error("缓存失败",e);
                throw new RuntimeException(e.getMessage());
            }
        }


    }
    public String getKey(String type,Long id){
        return type + "::" + id;
    }


}
