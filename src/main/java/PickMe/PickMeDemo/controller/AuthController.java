package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.config.UserAuthenticationProvider;
import PickMe.PickMeDemo.dto.CredentialsDto;
import PickMe.PickMeDemo.dto.SignUpDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;
    private final UserAuthenticationProvider userAuthenticationProvider;

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        UserDto userDto = userService.login(credentialsDto);
        
        // 로그인하면, 새로운 JWT를 반환함
        userDto.setToken(userAuthenticationProvider.createToken(userDto.getLogin()));

        // ResponseEntity.ok : status 200
        //return ResponseEntity.ok(userDto);
        // 동일한 코드. 가독성 좋게 변경.
        return ResponseEntity.ok().body(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid SignUpDto user) {
        // 새로운 사용자 Entity 생성
        UserDto createdUser = userService.register(user);
        
        // 로그인과 동일하게 회원가입할 때에도, 새로 생성된 JWT를 반환
        // 생성된 사용자 Entity에 JWT를 설정
        // 이 JWT는 클라이언트가 나중에 로그인 상태를 유지하기 위해 사용
        // 회원가입과 로그인을 분리할 때에는 토큰을 생성할 필요 없는듯?
        createdUser.setToken(userAuthenticationProvider.createToken(user.getLogin()));

        // 엔티티를 생성할 때 새로운 엔티티를 찾을 수 있는 URL과 함께 201 HTTP 코드를 리턴하는 것이 가장 좋음.
        // return ResponseEntity.created(URI.create("/users/" + createdUser.getId())).body(createdUser);

        // Return success response
        return ResponseEntity.ok().body(createdUser);
    }

}