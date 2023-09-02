package PickMe.PickMeDemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

// redis를 빈으로 등록하기 위한 코드
// redis는 간단하게 말하면 관계형 db인데, in-memory형태로 동작, <key-value> 형태로 데이터 저장
// 따라서 우리는 <email-인증코드> 방식으로 redis에 저장할 거고,
// redis를 쓰면 좋은 점은 특정 시간을 설정한 후, 시간이 지나면 key에 해당하는 데이터를 지울 수 있다는 것임
@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.host}") // yaml에 저장한 redis의 host
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort; // yaml에 저장한 redis의 port번호 -> 기본포트번호는 6379임

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        System.out.println("redisHost = " + redisHost);
        System.out.println("redisPort = " + redisPort);
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory((redisConnectionFactory()));
        return redisTemplate;
    }
}
