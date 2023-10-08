package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.UserRecommendationDto;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.service.PortfolioService;
import PickMe.PickMeDemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Controller
@ResponseBody
@RequiredArgsConstructor
public class RecommendationController {

    private final PortfolioService portfolioService;
    private final UserService userService;


    @GetMapping("/recommend")
    public ResponseEntity<List<UserRecommendationDto>> getRecommendation(Principal principal) {
        System.out.println("===========111111111111111111=================");
        System.out.println("==============================================");
        System.out.println("==============================================");
        System.out.println("==============================================");
        if (principal == null) {
            // Handle unauthenticated user
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getName();
        String name = userService.findByEmail(email).getNickName();
        List<UserRecommendationDto> userRecommendationDto = userService.getUserForRecommendation2(name);
        System.out.println("==========22222222222222222222222=============");
        System.out.println("==============================================");
        System.out.println("==============================================");
        System.out.println("==============================================");

        if (!userRecommendationDto.isEmpty()) {
            System.out.println("============333333333333333333333=============");
            System.out.println("==============================================");
            System.out.println("==============================================");
            System.out.println("==============================================");
            System.out.println(userRecommendationDto.toString());
            return ResponseEntity.ok(userRecommendationDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
