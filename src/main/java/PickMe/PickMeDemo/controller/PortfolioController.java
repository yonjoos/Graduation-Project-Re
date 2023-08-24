package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.PortfolioDto;
import PickMe.PickMeDemo.dto.PortfolioFormDto;
import PickMe.PickMeDemo.dto.UserBaseInfoUpdateDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.service.PortfolioService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class PortfolioController {

    // portfolio controller가 user service에 의존하게 된다는 문제..
    private final UserService userService;
    private final PortfolioService portfolioService;

    // 포트폴리오 생성하기
    @PostMapping("/uploadPortfolio")
    public ResponseEntity<PortfolioDto> uploadPortfolio(@RequestBody @Valid PortfolioFormDto portfolioFormDto, Principal principal) {
        String userEmail = principal.getName(); // Get the email from the JWT token

        // Email로 UserDTO 찾기
        UserDto userDto = userService.findByEmail(userEmail);

        // portfolioFormDto : Portfolio 테이블에 저장하기 위해 필요
        // userDto : User와 Portfolio를 연결하기 위해 User 테이블의 PK를 얻기 위해 필요
        PortfolioDto portfolioDto = portfolioService.uploadPortfolio(portfolioFormDto, userDto);

        return ResponseEntity.ok(portfolioDto);
    }

    // 포트폴리오 전체 정보 가져오기
    @GetMapping("/getPortfolio")
    public ResponseEntity<PortfolioDto> getPortfolio(Principal principal) {
        String userEmail = principal.getName(); // JWT 토큰에서 이메일 가져오기

        // getPortfolio : 이메일을 통해 포트폴리오를 가져오는 함수
        PortfolioDto portfolio = portfolioService.getPortfolio(userEmail);

        return ResponseEntity.ok(portfolio);
    }


    // 포트폴리오 수정 시 사용할 포트폴리오 폼 정보만 가져오기
    @GetMapping("/getPortfolioForm")
    public ResponseEntity<PortfolioFormDto> getPortfolioForm(Principal principal) {
        String userEmail = principal.getName(); // JWT 토큰에서 이메일 가져오기

        // getPortfolio : 이메일을 통해 포트폴리오를 가져오는 함수
        PortfolioFormDto portfolioForm = portfolioService.getPortfolioForm(userEmail);

        return ResponseEntity.ok(portfolioForm);
    }


    // 포트폴리오 수정
    @PutMapping("/updatePortfolio")
    public ResponseEntity<String> updateUserInfo(@RequestBody PortfolioFormDto portfolioFormDto, Principal principal) {
        String userEmail = principal.getName();

        try {
            // Call a method in your userService to handle the user base info update logic
            portfolioService.updatePortfolio(userEmail, portfolioFormDto);
            return ResponseEntity.ok("Portfolio information has been successfully updated.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update portfolio information.");
        }
    }
}
