package PickMe.PickMeDemo.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;

@Configuration
@EnableWebMvc
@SuppressWarnings("unchecked")
public class WebConfig {
    //백엔드는 기본적으로 프론트엔드에서 오는 요청을 신뢰하지 않으므로, cors문제를 해결하기 위한 bean설정이다
    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // 프론트엔드에서 무엇이 오는지 구체화
        config.setAllowCredentials(true); //서버가 CORS 요청에 모든 자격 증명(예: 쿠키 또는 HTTP 인증)을 포함해야 함을 나타냄
        config.addAllowedOrigin("http://localhost:3000"); //백엔드에 요청할 수 있는 프런트엔드 애플리케이션의 허용된 출처를 지정
        config.setAllowedHeaders(Arrays.asList( //실제 요청을 할 때 사용할 수 있는 허용된 요청 헤더를 나열
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT
        ));
        config.setAllowedMethods(Arrays.asList( // 실제 요청을 할 때 사용할 수 있는 허용된 HTTP 메서드를 나열
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name()
        ));
        // request가 수락되는 시간과 관련한 옵션 (30분)
        config.setMaxAge(3600L);
        // 모든 request에 적용하기 위함
        source.registerCorsConfiguration("/**", config); //모든 URL 패턴에 대해 CorsConfiguration을 등록
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source)); //FilterRegistrationBean은 Spring의 필터 체인에 CorsFilter를 등록하는 데 사용
        bean.setOrder(-102); //필터가 적용되는 순서를 설정. 음수 값은 CORS 필터가 다른 필터보다 먼저 적용되도록 하는 데 사용
        return bean;
    }
}
