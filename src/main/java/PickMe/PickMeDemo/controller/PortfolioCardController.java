package PickMe.PickMeDemo.controller;


import PickMe.PickMeDemo.dto.PortfolioCardDto;
import PickMe.PickMeDemo.dto.PortfolioCardRecommendationDto;
import PickMe.PickMeDemo.dto.PostsListDto;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.service.PortfolioService;
import PickMe.PickMeDemo.service.RecommendationsService;
import PickMe.PickMeDemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortfolioCardController {

    private final PortfolioService portfolioService;
    private final RecommendationsService recommendationsService;


    @GetMapping("/getCards")
    public ResponseEntity<Page<PortfolioCardDto>> getCards(
            @RequestParam(name = "selectedBanners") List<String> selectedBanners, //프론트엔드에서 넘어온 선택된 배너정보
            @RequestParam(defaultValue = "latestPortfolio") String sortOption, //프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 최신등록순
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "9") int size, //프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수
            @RequestParam(name = "searchTerm", required = false) String searchTerm){

        Page<PortfolioCardDto> result = portfolioService.getCards(selectedBanners, sortOption, searchTerm, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    // 일단은 중간 발표용 코사인 유사도 값이 포함된 PortfolioCardRecommendationDto를 사용.
    // 중간 발표 이후로는 리턴 타입 등을 PortfolioCardDto로 돌려놓기!!
    @GetMapping("/getRecommendation")
    public ResponseEntity<List<PortfolioCardRecommendationDto>> getRecommendation(Principal principal){
        String email = principal.getName();

        //String type = "real-time"; //' real-time' or 'DB'
        String type = "DB";

        // 일단은 중간 발표용 코사인 유사도 값이 포함된 PortfolioCardRecommendationDto를 사용.
        // 중간 발표 이후로는 리턴 타입 등을 PortfolioCardDto로 돌려놓기!!
        List<PortfolioCardRecommendationDto> result = recommendationsService.getRecommend(email, type);

        return ResponseEntity.ok(result);
    }




    /*

    내 포트폴리오 빼줘야함

    */
}
