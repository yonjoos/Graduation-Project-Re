package PickMe.PickMeDemo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 시홍 config/JwtAuthenticationFilter
@RequiredArgsConstructor
// 이 JWT 필터가 요청당 한 번만 사용되기를 원하기 때문에, OncePerRequestFilter를 확장함
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserAuthenticationProvider userAuthenticationProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            FilterChain filterChain) throws ServletException, IOException {

        String header = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);    // Authrization의 헤더를 확인

        if (header != null) {
            String[] authElements = header.split(" ");

            // 길이가 정확히 2이고, Bearer 토큰잉야 함.
            if (authElements.length == 2 && "Bearer".equals(authElements[0])) {
                try {
                    // 자격 증명이 유효하면 보안 컨텍스트에 인증 빈을 추가
                    // 보안 컨텍스트에 인증 빈을 추가하면, 모든 컨트롤러에서 @AuthenticationPrincipal 어노테이션을 입력 매개변수로 추가할 수 있음.
                    // 이 어노테이션은 인증된 사용자를 주입함.
                    // 따라서 필터는 인증된 사용자의 객체를 컨트롤러에 제공함.
                    SecurityContextHolder.getContext().setAuthentication(
                            userAuthenticationProvider.validateToken(authElements[1]));
                } catch (RuntimeException e) {
                    // 문제가 발생하면 보안 컨텍스트를 지우고 오류를 발생시킴
                    SecurityContextHolder.clearContext();
                    throw e;
                }
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}