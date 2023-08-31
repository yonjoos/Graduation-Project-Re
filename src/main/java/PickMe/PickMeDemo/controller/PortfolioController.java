package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.PortfolioDto;
import PickMe.PickMeDemo.dto.PortfolioFormDto;
import PickMe.PickMeDemo.service.PortfolioService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequiredArgsConstructor
public class PortfolioController {

    // portfolio controller가 user service에 의존하게 된다는 문제..
    private final UserService userService;
    private final PortfolioService portfolioService;

    // 포트폴리오 생성하기
    @PostMapping("/uploadPortfolio")
    public ResponseEntity<PortfolioDto> uploadPortfolio(@RequestBody @Valid PortfolioFormDto portfolioFormDto, Principal principal) {
        String userEmail = principal.getName(); // Get the email from the JWT token

        // portfolioFormDto : Portfolio 테이블에 저장하기 위해 필요
        // userEmail : User와 Portfolio를 연결하기 위해 User 테이블의 PK를 얻기 위해 필요
        PortfolioDto portfolioDto = portfolioService.uploadPortfolio(portfolioFormDto, userEmail);

        return ResponseEntity.ok(portfolioDto);
    }

    // 나의 포트폴리오 정보 가져오기
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
    public ResponseEntity<String> updatePortfolioInfo(@RequestBody PortfolioFormDto portfolioFormDto, Principal principal) {
        String userEmail = principal.getName();         // JWT 토큰으로부터 이메일 파싱

        try {
            portfolioService.updatePortfolio(userEmail, portfolioFormDto);          // userEmail을 가지고 업데이트 로직 진행
            return ResponseEntity.ok("Portfolio information has been successfully updated.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update portfolio information.");
        }
    }


    // 포트폴리오 삭제
    @PostMapping("/deletePortfolio")
    public ResponseEntity<String> deletePortfolioInfo(Principal principal) {
        String userEmail = principal.getName();         // JWT 토큰으로부터 이메일 파싱

        try {
            portfolioService.deletePortfolio(userEmail);        // userEmail을 가지고 삭제 로직 진행
            return ResponseEntity.ok("Portfolio has been successfully withdrawn.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to withdraw portfolio.");
        }
    }


    // 닉네임으로 상대방 포트폴리오 정보 가져오기
    @GetMapping("/getUserPortfolio")
    public ResponseEntity<PortfolioDto> getPortfolioByNickName(@RequestParam String nickName) {

        // getUserPortfolio : 유저의 닉네임을 통해 해당 유저의 포트폴리오를 가져오는 함수
        PortfolioDto portfolio = portfolioService.getUserPortfolio(nickName);

        return ResponseEntity.ok(portfolio);
    }
}
