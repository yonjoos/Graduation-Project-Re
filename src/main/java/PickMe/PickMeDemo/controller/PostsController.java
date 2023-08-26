package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.PostsDto;
import PickMe.PickMeDemo.dto.PostsFormDto;
import PickMe.PickMeDemo.dto.PostsListDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.PostsService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}

