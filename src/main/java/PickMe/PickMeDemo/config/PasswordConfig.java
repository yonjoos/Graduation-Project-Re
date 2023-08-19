package PickMe.PickMeDemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 암호화에 대한 인코딩 알고리즘을 BCryptPasswordEncoder로 선택하였음.
        return new BCryptPasswordEncoder();
    }
}