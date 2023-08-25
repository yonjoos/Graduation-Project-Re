package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.PostFormDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.PostsService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class PostsController {

    private final UserService userService;
    private final PostsService postsService;

    @PostMapping("/uploadProjectPost")
    public ResponseEntity<String> uploadProjectPost(@RequestBody @Valid PostFormDto postFormDto, Principal principal) {
        return uploadPostInternal(postFormDto, principal, PostType.PROJECT);
    }

    @PostMapping("/uploadStudyPost")
    public ResponseEntity<String> uploadStudyPost(@RequestBody @Valid PostFormDto postFormDto, Principal principal) {
        return uploadPostInternal(postFormDto, principal, PostType.STUDY);
    }

    // Project와 Study의 새로운 게시물을 업로드하는 코드는 매우 유사하다.
    // 따라서 공통된 부분을 묶어주고, 둘을 구분할 수 있도록 PostType을 넣어준다.
    private ResponseEntity<String> uploadPostInternal(@Valid PostFormDto postFormDto, Principal principal, PostType postType) {
        // Email로 userDto 찾기
        String userEmail = principal.getName();
        UserDto userDto = userService.findByEmail(userEmail);

        try {
            if (postType == PostType.PROJECT) {
                postsService.uploadProjectPost(postFormDto, userDto);
            } else if (postType == PostType.STUDY) {
                postsService.uploadStudyPost(postFormDto, userDto);
            }

            return ResponseEntity.ok("Post has been successfully uploaded.");
        } catch (AppException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }
}

