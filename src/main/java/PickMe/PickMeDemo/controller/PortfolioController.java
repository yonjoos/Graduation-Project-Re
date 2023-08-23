package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.PortfolioDto;
import PickMe.PickMeDemo.dto.PortfolioFormDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.service.PortfolioService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class PortfolioController {

    // portfolio controller가 user service에 의존하게 된다는 문제..
    private final UserService userService;
    private final PortfolioService portfolioService;

    @PostMapping("/uploadPortfolio")
    public ResponseEntity<PortfolioDto> uploadPortfolio(@RequestBody @Valid PortfolioFormDto portfolioFormDto, Principal principal) {
        String userEmail = principal.getName(); // Get the email from the JWT token
        //api시 해당 회원의 이메일을 알아와서
        //email기반으로 쿼리를 날리면 됨

        UserDto userDto = userService.findByEmail(userEmail);

        PortfolioDto portfolioDto = portfolioService.uploadPortfolio(portfolioFormDto, userDto);

        return ResponseEntity.ok(portfolioDto);
    }

    @GetMapping("/getPortfolio")
    public ResponseEntity<PortfolioDto> getPortfolio(Principal principal) {
        String userEmail = principal.getName(); // JWT 토큰에서 이메일 가져오기

        PortfolioDto portfolio = portfolioService.getPortfolio(userEmail);

        return ResponseEntity.ok(portfolio);
    }
}
