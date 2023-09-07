package PickMe.PickMeDemo.controller;


import PickMe.PickMeDemo.dto.PortfolioCardDto;
import PickMe.PickMeDemo.dto.PostsListDto;
import PickMe.PickMeDemo.service.PortfolioService;
import PickMe.PickMeDemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/getCards")
    public ResponseEntity<Page<PortfolioCardDto>> getCards(
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size
    ){
        Page<PortfolioCardDto> result = portfolioService.getCards(searchTerm, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }




    /*

    내 포트폴리오 빼줘야함

    */
}
