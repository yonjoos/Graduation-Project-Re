package PickMe.PickMeDemo.controller;


import PickMe.PickMeDemo.dto.SearchResultDto;
import PickMe.PickMeDemo.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // 랜딩페이지에서, 검색어를 입력하는 순간순간마다, 프로젝트, 스터디, 유저 이름 및 제목이 일치하는 것
    // 상위 5개씩을 찾아서 반환하는 컨트롤러
    @GetMapping("/getFilteredSearchLists")
    public ResponseEntity<SearchResultDto> getFilteredSearchLists(
            @RequestParam(name = "searchTerm", required = false) String searchTerm) { //프론트엔드에서 넘어온 검색어 문자열


        SearchResultDto filteredSearchLists = searchService.getFilteredSearchLists(searchTerm);

        return ResponseEntity.ok(filteredSearchLists);
    }
}
