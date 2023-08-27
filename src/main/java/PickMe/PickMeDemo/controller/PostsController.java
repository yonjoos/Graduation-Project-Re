package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.InquirePostConditionDto;
import PickMe.PickMeDemo.dto.PostsDto;
import PickMe.PickMeDemo.dto.PostsFormDto;
import PickMe.PickMeDemo.dto.PostsListDto;
import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.PostsService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostsController {

    private final UserService userService;
    private final PostsService postsService;

    @PostMapping("/uploadProjectPost")
    public ResponseEntity<String> uploadProjectPost(@RequestBody @Valid PostsFormDto postsFormDto, Principal principal) {
        return uploadPost(postsFormDto, principal, PostType.PROJECT);
    }

    @PostMapping("/uploadStudyPost")
    public ResponseEntity<String> uploadStudyPost(@RequestBody @Valid PostsFormDto postsFormDto, Principal principal) {
        return uploadPost(postsFormDto, principal, PostType.STUDY);
    }

    // Project와 Study의 새로운 게시물을 업로드하는 코드는 매우 유사하다.
    // 따라서 공통된 부분을 묶어주고, 둘을 구분할 수 있도록 PostType을 넣어준다.
    private ResponseEntity<String> uploadPost(@Valid PostsFormDto postsFormDto, Principal principal, PostType postType) {
        // Email로 userDto 찾기
        String userEmail = principal.getName();

        try {
            if (postType == PostType.PROJECT) {
                postsService.uploadProjectPost(postsFormDto, userEmail);
            } else if (postType == PostType.STUDY) {
                postsService.uploadStudyPost(postsFormDto, userEmail);
            }

            return ResponseEntity.ok("Post has been successfully uploaded.");
        } catch (AppException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }




    // 프로젝트 리스트 조회
    @GetMapping("/getProjectList")
    public ResponseEntity<List<PostsListDto>> getProjectList() {
        return getPostsList(PostType.PROJECT);
    }

    // 스터디 리스트 조회
    @GetMapping("/getStudyList")
    public ResponseEntity<List<PostsListDto>> getStudyList(Principal principal) {
        return getPostsList(PostType.STUDY);
    }

    // Project와 Study의 게시물 리스트를 조회하는 코드는 매우 유사하다.
    // 따라서 공통된 부분을 묶어주고, 둘을 구분할 수 있도록 PostType을 넣어준다.
    private ResponseEntity<List<PostsListDto>> getPostsList(PostType postType) {

        List<PostsListDto> postsListDtoList;

        try {
            if (postType == PostType.PROJECT) {
                postsListDtoList = postsService.getProjectList();
            } else if (postType == PostType.STUDY) {
                postsListDtoList = postsService.getStudyList();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            return ResponseEntity.ok(postsListDtoList);
        } catch (AppException ex) {
            return ResponseEntity.status(ex.getStatus()).body(null);
        }
    }



    // 특정 프로젝트 조회
    @GetMapping("/getProject/{projectId}") // Use path variable to get project ID from URL
    private ResponseEntity<PostsDto> getProject(@PathVariable Long projectId, Principal principal) {
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.getProject(userEmail, projectId);

        return ResponseEntity.ok(postsDto);
    }


    // 특정 스터디 조회
    @GetMapping("/getStudy/{studyId}") // Use path variable to get project ID from URL
    private ResponseEntity<PostsDto> getStudy(@PathVariable Long studyId, Principal principal) {
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.getStudy(userEmail, studyId);

        return ResponseEntity.ok(postsDto);
    }

    // 프로젝트 페이지에서, 동적 쿼리를 활용해 선택된 배너와 선택한 페이지에 따라 게시물을 페이징해서 프런트에 반환하는 컨트롤러
    // 추후에 Study쪽 페이징 동적쿼리 할 때, 이를 재활용할지, 별도로 api를 분리할 지 결정해야할 듯 함
    @GetMapping("/getFilteredPosts")
    public ResponseEntity<Page<PostsListDto>> getFilteredPosts(
            @RequestParam(name = "selectedBanners") List<String> selectedBanners, //프론트엔드에서 넘어온 선택된 배너정보
            @RequestParam(defaultValue = "latestPosts") String sortOption, //프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 최신등록순
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size) { //프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수

        // 페이지 넘버, 페이지 사이즈를 통해 PageRequest,
        // 선택된 배너 정보를 parameter로 넣어서 서비스 계층 수행
        Page<PostsListDto> filteredPosts = postsService.getFilteredPosts(selectedBanners, sortOption, PageRequest.of(page, size));

        return ResponseEntity.ok(filteredPosts);
    }
}

