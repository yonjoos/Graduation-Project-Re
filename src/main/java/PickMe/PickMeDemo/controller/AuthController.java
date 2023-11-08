package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.config.UserAuthenticationProvider;
import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.service.PortfolioService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PortfolioService portfolioService;
    private final UserAuthenticationProvider userAuthenticationProvider;

    // CredentialsDto (아이디, 비번)이 들어있는 credentialDto로 로그인 시도
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        UserDto userDto = userService.login(credentialsDto);

        // Email로 포트폴리오 DTO 반환
        Boolean has = portfolioService.hasPortfolio(userDto.getEmail());

        if (has == true) {
            userDto.setIsCreated(true);
        } else {
            userDto.setIsCreated(false);
        }
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

    // 닉네임 중복인 지에 대한 여부를 확인하는 컨트롤러 (회원가입 관련이므로 여기에 넣었음)
    @GetMapping("/nicknameDuplicate")
    public ResponseEntity<NickNameDuplicateDto> checkNicknameAvailability(Principal principal, @RequestParam String nickname) {
        // 로그인 한 사람의 이메일 가져오기
        String email = principal.getName();

        // userService로부터 닉네임이 사용 가능한지를 알아옴 (isAvailable==true: 중복 아니어서 사용 가능 / isAvailable==false: 중복이어서 사용 불가능)
        String isAvailable = userService.isNicknameAvailable(nickname, email);

        //NickNameDuplicateDto에 isAvailable값을 실어서 반환
        return ResponseEntity.ok(new NickNameDuplicateDto(isAvailable));
    }
}