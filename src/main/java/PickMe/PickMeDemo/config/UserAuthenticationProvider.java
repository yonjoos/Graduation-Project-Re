package PickMe.PickMeDemo.config;

import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class UserAuthenticationProvider {

    @Value("${security.jwt.token.secret-key:secret-key}")   // JWT를 생성하고 읽으려면 비밀 키가 필요함. application.yml 파일에서 구성하고, 여기에 주입할 수 있음. 그러나 기본 값을 가질 수도 있음.
    private String secretKey;

    private final UserService userService;

    @PostConstruct
    protected void init() {
        // this is to avoid having the raw secret key available in the JVM
        // 일반 텍스트로 된 비밀 키를 피하기 위해 base64로 인코딩
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String login) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3_600_000); // 내 JWT가 한 시간만 유효하기를 원할 때 60 * 60 = 3600초 + 000(?)

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withIssuer(login)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .sign(algorithm);
    }

    public Authentication validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        // JWT를 확인하기 위해 디코딩 진행
        // 유효 기간을 초과하면 예외가 발생함.
        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);

        UserDto user = userService.findByLogin(decoded.getIssuer());

        // 사용자가 내 데이터베이스에 존재하는지 확인
        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }

}