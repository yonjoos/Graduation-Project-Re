package PickMe.PickMeDemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RedisUtil {

    // redisTemplate이 제공하는 기본 crud를 활용할 것
    private final StringRedisTemplate redisTemplate;

    // key에 해당하는 data(인증번호)값 꺼내기
    public String getData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    // duration 동안만 (key, value) 저장하기
    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    // redis에 저장된 key-value 삭제하기
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    // key에 해당하는 value가 있는지 확인하기
    public boolean existData(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
