package PickMe.PickMeDemo.controller;


import PickMe.PickMeDemo.dto.PortfolioCardDto;
import PickMe.PickMeDemo.service.PortfolioService;
import PickMe.PickMeDemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortfolioCardController {

    private final UserService userService;
    private final PortfolioService portfolioService;
    @GetMapping("/getPortfolioCards")
    public ResponseEntity<List<PortfolioCardDto>> getPortfolioCards(){

        return ResponseEntity.ok(portfolioService.getPortfolioCard());

    }


    /*

    내 포트폴리오 빼줘야함

    */
}
