package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.PostsDto;
import PickMe.PickMeDemo.dto.PostsFormDto;
import PickMe.PickMeDemo.dto.PostsListDto;
import PickMe.PickMeDemo.dto.PostsUpdateFormDto;
import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.PostsService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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


    // 프로젝트 수정 시 사용할 프로젝트 폼 정보만 가져오기
    // 여기서 본인이 아닌 사람은 이 페이지로 접근 불가능
    @GetMapping("/getProjectForm/{projectId}")
    public ResponseEntity<PostsUpdateFormDto> getPortfolioForm(@PathVariable Long projectId, Principal principal) {
        String userEmail = principal.getName(); // JWT 토큰에서 이메일 가져오기

        // getPortfolio : 이메일을 통해 포트폴리오를 가져오는 함수
        // ** 중요 **
        // postType을 Boolean 리스트로 받아오는 PostsUpdateFormDto 사용!
        PostsUpdateFormDto projectUpdateForm = postsService.getProjectForm(userEmail, projectId);

        return ResponseEntity.ok(projectUpdateForm);
    }


    // 스터디 수정 시 사용할 프로젝트 폼 정보만 가져오기
    // 여기서 본인이 아닌 사람은 이 페이지로 접근 불가능
    @GetMapping("/getStudyForm/{studyId}")
    public ResponseEntity<PostsUpdateFormDto> getStudyForm(@PathVariable Long studyId, Principal principal) {
        String userEmail = principal.getName(); // JWT 토큰에서 이메일 가져오기

        // getPortfolio : 이메일을 통해 포트폴리오를 가져오는 함수
        // ** 중요 **
        // postType을 Boolean 리스트로 받아오는 PostsUpdateFormDto 사용!
        PostsUpdateFormDto projectUpdateForm = postsService.getStudyForm(userEmail, studyId);

        return ResponseEntity.ok(projectUpdateForm);
    }



    // 프로젝트 수정.
    // 어차피 프로젝트 수정 페이지는 바로 위의 메서드에서 /getProjectForm/{projectId}를 거치면서, 본인만 수정할 수 있는 페이지에 들어가므로 해당 유저가 누구인지 알 필요가 없음.
    // 즉, 본인 확인이 get 메서드를 통해 이미 확인되므로, 무조건 수정 가능함.
    // ** 중요 **
    // postType을 String 리스트로 받아오는 PostsFormDto 사용!
    @PutMapping("/project/update/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId, @RequestBody PostsFormDto postsFormDto) {

        try {
            postsService.updateProject(projectId, postsFormDto);
            return ResponseEntity.ok("프로젝트가 성공적으로 업데이트 되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로젝트 업데이트에 실패했습니다.");
        }
    }


    // 스터디 수정
    @PutMapping("/study/update/{studyId}")
    public ResponseEntity<String> updateStudy(@PathVariable Long studyId, @RequestBody PostsFormDto postsFormDto) {

        try {
            postsService.updateStudy(studyId, postsFormDto);
            return ResponseEntity.ok("스터디가 성공적으로 업데이트 되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("스터디 업데이트에 실패했습니다.");
        }

    }



    // 프로젝트 삭제
    // 삭제는 디테일페이지에서 진행되므로, 본인만 삭제할 수 있다. 따라서 Principal이 필요 없음
    @PostMapping("/project/delete/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        try {
            postsService.deleteProject(projectId);
            return ResponseEntity.ok("프로젝트가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로젝트 삭제에 실패했습니다.");
        }
    }


    // 스터디 삭제
    // 삭제는 디테일페이지에서 진행되므로, 본인만 삭제할 수 있다. 따라서 Principal이 필요 없음
    @PostMapping("/study/delete/{studyId}")
    public ResponseEntity<String> deleteStudy(@PathVariable Long studyId) {
        try {
            postsService.deleteStudy(studyId);
            return ResponseEntity.ok("스터디가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("스터디 삭제에 실패했습니다.");
        }
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

