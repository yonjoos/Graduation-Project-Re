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

// 시홍 auth/AuthenticationController
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;
    private final UserAuthenticationProvider userAuthenticationProvider;

    // CredentialsDto (아이디, 비번)이 들어있는 credentialDto로 로그인 시도
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        UserDto userDto = userService.login(credentialsDto);
        
        // 로그인하면, userDto의 아이디를 바탕으로 새로운 토큰을 생성한 후, userDto에 해당 토큰 세팅
        userDto.setToken(userAuthenticationProvider.createToken(userDto.getEmail()));

        // 토큰까지 세팅된 해당 userDto를 반환
        // ResponseEntity.ok : status 200
        return ResponseEntity.ok().body(userDto);
    }

    // SignUpDto (성, 이름, 아이디, 비번)이 들어있는 user로 회원가입.
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid SignUpDto user) {
        // 새로운 사용자 Entity 생성
        UserDto createdUser = userService.register(user);
        
        // userDto 형식을 갖춘 createdUser 필드에 토큰을 세팅
        //createdUser.setToken(userAuthenticationProvider.createToken(user.getEmail()));
        createdUser.setToken(null);

        // 회원가입 완료된 애의 토큰이 담긴 DTO를 반환
         return ResponseEntity.ok().body(createdUser);
    }
}