package PickMe.PickMeDemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

// 시홍 config/SecurityConfiguration
// JWT requests를 위한 필터를 위한 파일
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    private final UserAuthenticationProvider userAuthenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .exceptionHandling(customizer -> customizer.authenticationEntryPoint(userAuthenticationEntryPoint)) // 보안 문제가 발생하면 내가 커스텀한 메시지를 받음
                .addFilterBefore(new JwtAuthFilter(userAuthenticationProvider), BasicAuthenticationFilter.class)    // Spring Security의 인증 필터 전에 JWT 필터 더하기
                .csrf(AbstractHttpConfigurer::disable)      // complexity를 피하기 위해 csrf를 disable
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Spring에게 나는 무상태(stateless) application이라는 것을 알림. 이렇게 하면 Spring에서 세션과 쿠키를 생성하지 않음
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(HttpMethod.POST, "/login", "/register", "/mailConfirm", "/verifyCode", "/resetPassword").permitAll() // "/login", "/register"은 인증(로그인 여부)이 필요하지 않은 유일한 엔드포인트
                        .requestMatchers(HttpMethod.GET, "/nicknameDuplicate", "/getHotPost", "/getCards", "/getFilteredProjects", "getFilteredStudies").permitAll() // "/nicknameDuplicate"도 회원 가입 시 중복 여부 확인하는 백엔드 처리이므로 인증이 필요하지 않음
                        .anyRequest().authenticated())      // "/login", "/register" 등등을 제외한 나머지 엔드포인트는 모두 인증이 필요
        ;
        return http.build(); //
    }
}