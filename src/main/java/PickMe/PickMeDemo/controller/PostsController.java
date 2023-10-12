package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.*;
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
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PostsController {

    private final UserService userService;
    private final PostsService postsService;


    /*
    ######################### UPLOAD 함수 ###########################################################
    ######################### UPLOAD 함수 ###########################################################
    ------ @ PostMapping
    */

    // uploadProjectPost
    @PostMapping("/uploadProjectPost")
    public ResponseEntity<String> uploadProjectPost(@RequestBody @Valid PostsFormDto postsFormDto, Principal principal) {
        return uploadPost(postsFormDto, principal, PostType.PROJECT);
    }


    // uploadStudyPost
    @PostMapping("/uploadStudyPost")
    public ResponseEntity<String> uploadStudyPost(@RequestBody @Valid PostsFormDto postsFormDto, Principal principal) {
        return uploadPost(postsFormDto, principal, PostType.STUDY);
    }



    // uploadPost
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



    /*
    ######################### 조회 함수 ###########################################################
    ######################### 조회 함수 ###########################################################
    ------ @ GetMapping
    */

    // 프로젝트 리스트 조회 -> getFilteredProjects, getFilteredStudies로 대체
//    @GetMapping("/getProjectList")
//    public ResponseEntity<List<PostsListDto>> getProjectList() {
//        return getPostsList(PostType.PROJECT);
//    }
//
//    // 스터디 리스트 조회
//    @GetMapping("/getStudyList")
//    public ResponseEntity<List<PostsListDto>> getStudyList(Principal principal) {
//        return getPostsList(PostType.STUDY);
//    }
//
//
//
//    // Project와 Study의 게시물 리스트를 조회하는 코드는 매우 유사하다.
//    // 따라서 공통된 부분을 묶어주고, 둘을 구분할 수 있도록 PostType을 넣어준다.
//    private ResponseEntity<List<PostsListDto>> getPostsList(PostType postType) {
//
//        List<PostsListDto> postsListDtoList;
//
//        try {
//            if (postType == PostType.PROJECT) {
//                postsListDtoList = postsService.getProjectList();
//            } else if (postType == PostType.STUDY) {
//                postsListDtoList = postsService.getStudyList();
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//            }
//
//            return ResponseEntity.ok(postsListDtoList);
//        } catch (AppException ex) {
//            return ResponseEntity.status(ex.getStatus()).body(null);
//        }
//    }



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

    // 게시물 작성자라면, 지원자 조회
    @GetMapping("/getProjectApplicants/{projectId}") // Use path variable to get project ID from URL
    private ResponseEntity<List<ApplicantDto>> getProjectApplicants(@PathVariable Long projectId, Principal principal) {
        String userEmail = principal.getName();

        List<ApplicantDto> applicantDto = postsService.getApplicants(userEmail, projectId);

        return ResponseEntity.ok(applicantDto);
    }

    // 게시물 작성자라면, 지원자 조회
    @GetMapping("/getStudyApplicants/{studyId}") // Use path variable to get project ID from URL
    private ResponseEntity<List<ApplicantDto>> getStudyApplicants(@PathVariable Long studyId, Principal principal) {
        String userEmail = principal.getName();

        List<ApplicantDto> applicantDto = postsService.getApplicants(userEmail, studyId);

        return ResponseEntity.ok(applicantDto);
    }

    /*
    ######################### GET FORM 함수 ###########################################################
    ######################### GET FORM 함수 ###########################################################
    ------ @ GetMapping
    */

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



    /*
    ###################### UPDATE 함수 ###############################################################
    ###################### UPDATE 함수 ###############################################################

    ------ @ PutMapping
     */

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



    /*
    ###################### DELETE 함수 ###############################################################
    ###################### DELETE 함수 ###############################################################
    ------ @ PostMapping
     */

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


    /*
    ############################# SORTING / FILTER 관련 함수 #############################################
    ############################# SORTING / FILTER 관련 함수 #############################################
    ------ @ PutMapping
     */

    // 프로젝트 페이지에서, 동적 쿼리를 활용해 선택된 배너와 선택한 페이지, 정렬 옵션, 검색어에 따라 게시물을 페이징해서 프런트에 반환하는 컨트롤러
    @GetMapping("/getFilteredProjects")
    public ResponseEntity<Page<PostsListDto>> getFilteredProjects(
            @RequestParam(name = "selectedBanners") List<String> selectedBanners, //프론트엔드에서 넘어온 선택된 배너정보
            @RequestParam(defaultValue = "latestPosts") String sortOption, //프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 최신등록순
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size, //프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수
            @RequestParam(name = "searchTerm", required = false) String searchTerm) { //프론트엔드에서 넘어온 검색어 문자열


        // 페이지 넘버, 페이지 사이즈를 통해 PageRequest,
        // 선택된 배너 정보,
        // 정렬 옵션
        // 검색어 문자열
        // 을 parameter로 넣어서 서비스 계층 수행
        Page<PostsListDto> filteredProjects = postsService.getFilteredProjects(selectedBanners, sortOption, searchTerm, PageRequest.of(page, size));

        return ResponseEntity.ok(filteredProjects);
    }

    @GetMapping("/getUsersPosts")
    public ResponseEntity<List<PostsListDto>> getUsersPosts(Principal principal) {
        String email = principal.getName();
        String nickName = userService.findByEmail(email).getNickName();
        List<PostsListDto> usersPosts = postsService.getUsersPosts(nickName);
        return ResponseEntity.ok(usersPosts);
    }


    // 스터디 페이지에서, 동적 쿼리를 활용해 선택된 배너와 선택한 페이지, 정렬 옵션, 검색어에 따라 게시물을 페이징해서 프런트에 반환하는 컨트롤러
    @GetMapping("/getFilteredStudies")
    public ResponseEntity<Page<PostsListDto>> getFilteredStudies(
            @RequestParam(name = "selectedBanners") List<String> selectedBanners, //프론트엔드에서 넘어온 선택된 배너정보
            @RequestParam(defaultValue = "latestPosts") String sortOption, //프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 최신등록순
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size, //프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수
            @RequestParam(name = "searchTerm", required = false) String searchTerm) { //프론트엔드에서 넘어온 검색어 문자열

        // 페이지 넘버, 페이지 사이즈를 통해 PageRequest,
        // 선택된 배너 정보,
        // 정렬 옵션
        // 검색어 문자열
        // 을 parameter로 넣어서 서비스 계층 수행
        Page<PostsListDto> filteredStudies = postsService.getFilteredStudies(selectedBanners, sortOption, searchTerm, PageRequest.of(page, size));

        return ResponseEntity.ok(filteredStudies);
    }



    // Group 페이지에서, 동적 쿼리를 활용해 선택된 배너와 선택한 페이지, 정렬 옵션, 검색어에 따라 게시물을 페이징해서 프런트에 반환하는 컨트롤러
    @GetMapping("/getGroupPosts")
    public ResponseEntity<Page<GroupPostsDto>> getGroupPosts(
            @RequestParam(defaultValue = "writer") String postsOption, //프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 글쓴이
            @RequestParam(defaultValue = "latestPosts") String sortOption, //프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 최신등록순
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size, //프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수
            Principal principal) {      // 본인이 쓴 글인지, 남이 쓴 글인지 구분하기 위해 현재 유저의 정보 가져오기

        // Email 찾기
        String userEmail = principal.getName();

        Page<GroupPostsDto> groupPostsDtos;

        // 게시물 작성자인 경우, 다음의 코드를 실행
        if ("writer".equals(postsOption)) {
            groupPostsDtos = postsService.getWriterPosts(userEmail, sortOption, PageRequest.of(page, size));
        }
        // 게시물 지원자인 경우, 다음의 코드를 실행
        else {
            groupPostsDtos = postsService.getApplicantPosts(userEmail, sortOption, PageRequest.of(page, size));
        }

        return ResponseEntity.ok(groupPostsDtos);
    }



    // 특정 프로젝트 지원
    @PostMapping("/project/apply/{projectId}") // Use path variable to get project ID from URL
    private ResponseEntity<PostsDto> applyProject(@PathVariable Long projectId, Principal principal) {
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.applyPosts(userEmail, projectId);

        return ResponseEntity.ok(postsDto);
    }

    // 특정 스터디 지원
    @PostMapping("/study/apply/{studyId}") // Use path variable to get study ID from URL
    private ResponseEntity<PostsDto> applyStudy(@PathVariable Long studyId, Principal principal) {
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.applyPosts(userEmail, studyId);

        return ResponseEntity.ok(postsDto);
    }



    // 그룹 페이지에서 지원 승인
    @PutMapping("/posts/approve")
    private ResponseEntity<Page<GroupPostsDto>> approveUserWithPosts(
            @RequestParam String nickName,  // 프론트엔드에서 넘어온 지원한 유저의 닉네임
            @RequestParam Long postsId,   // 프론트엔드에서 넘어온 프로젝트 ID
            @RequestParam(defaultValue = "latestPosts") String sortOption, // 프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 최신등록순
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size, // 프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수
            Principal principal) {      // 본인이 쓴 글인지, 남이 쓴 글인지 구분하기 위해 현재 유저의 정보 가져오기


        // Email 찾기
        String userEmail = principal.getName();

        Page<GroupPostsDto> groupPosts = postsService.approveUser(userEmail, nickName, postsId, sortOption, PageRequest.of(page, size));

        return ResponseEntity.ok(groupPosts);
    }

    // 디테일 페이지에서 지원 승인
    @PutMapping("/posts/detail/approve")
    private ResponseEntity<List<ApplicantDto>> approveUserWithPostsInDetail(
            @RequestParam String nickName,  // 프론트엔드에서 넘어온 지원한 유저의 닉네임
            @RequestParam Long postsId,   // 프론트엔드에서 넘어온 프로젝트 ID
            Principal principal) {      // 본인이 쓴 글인지, 남이 쓴 글인지 구분하기 위해 현재 유저의 정보 가져오기


        // Email 찾기
        String userEmail = principal.getName();

        List<ApplicantDto> applicantDtoList = postsService.approveUserInDetail(userEmail, nickName, postsId);

        return ResponseEntity.ok(applicantDtoList);
    }



    // 프로젝트 지원 취소
    @PostMapping("/project/cancelApply/{projectId}")
    public ResponseEntity<PostsDto> cancelProjectApply(
            @PathVariable Long projectId,
            Principal principal,
            @RequestBody Map<String, String> requestBody     // Map으로 json 형식인 {"action":"approved"}을 스트링으로 파싱
    ) {

        // "action" 필드의 값을 추출. action 변수에는 "approved" 또는 "applying"이 들어있게 됨.
        String action = requestBody.get("action");

        // Email 찾기
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.cancelApply(userEmail, projectId, action);

        return ResponseEntity.ok(postsDto);
    }

    // 스터디 지원 취소
    @PostMapping("/study/cancelApply/{studyId}")
    public ResponseEntity<PostsDto> cancelStudyApply(
            @PathVariable Long studyId,
            Principal principal,
            @RequestBody Map<String, String> requestBody     // Map으로 json 형식인 {"action":"approved"}을 스트링으로 파싱
    ) {

        // "action" 필드의 값을 추출. action 변수에는 "approved" 또는 "applying"이 들어있게 됨.
        String action = requestBody.get("action");

        // Email 찾기
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.cancelApply(userEmail, studyId, action);

        return ResponseEntity.ok(postsDto);
    }



    // 그룹 페이지에서 승인 취소
    @PutMapping("/posts/cancelApprove")
    private ResponseEntity<Page<GroupPostsDto>> cancelApproveUserWithPost(
            @RequestParam String nickName,  // 프론트엔드에서 넘어온 지원한 유저의 닉네임
            @RequestParam Long postsId,   // 프론트엔드에서 넘어온 프로젝트 ID
            @RequestParam(defaultValue = "latestPosts") String sortOption, // 프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 최신등록순
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size, // 프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수
            Principal principal) {      // 본인이 쓴 글인지, 남이 쓴 글인지 구분하기 위해 현재 유저의 정보 가져오기

        // Email 찾기
        String userEmail = principal.getName();

        Page<GroupPostsDto> groupPosts = postsService.cancelApproveUser(userEmail, nickName, postsId, sortOption, PageRequest.of(page, size));

        return ResponseEntity.ok(groupPosts);
    }

    // 디테일 페이지에서 지원 승인
    @PutMapping("/posts/detail/cancelApprove")
    private ResponseEntity<List<ApplicantDto>> cancelApproveUserWithPostInDetail(
            @RequestParam String nickName,  // 프론트엔드에서 넘어온 지원한 유저의 닉네임
            @RequestParam Long postsId,   // 프론트엔드에서 넘어온 프로젝트 ID
            Principal principal) {      // 본인이 쓴 글인지, 남이 쓴 글인지 구분하기 위해 현재 유저의 정보 가져오기

        // Email 찾기
        String userEmail = principal.getName();

        List<ApplicantDto> applicantDtoList = postsService.cancelApproveUserInDetail(userEmail, nickName, postsId);

        return ResponseEntity.ok(applicantDtoList);
    }



    // 프로젝트 스크랩
    @PostMapping("/project/scrap/{projectId}") // Use path variable to get project ID from URL
    private ResponseEntity<PostsDto> projectScrap(@PathVariable Long projectId, Principal principal) {
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.postsScrap(userEmail, projectId);

        return ResponseEntity.ok(postsDto);
    }

    // 스터디 스크랩
    @PostMapping("/study/scrap/{studyId}") // Use path variable to get project ID from URL
    private ResponseEntity<PostsDto> studyScrap(@PathVariable Long studyId, Principal principal) {
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.postsScrap(userEmail, studyId);

        return ResponseEntity.ok(postsDto);
    }



    // 프로젝트 스크랩 취소
    @PostMapping("/project/cancelScrap/{projectId}")
    public ResponseEntity<PostsDto> cancelProjectScrap(@PathVariable Long projectId, Principal principal) {

        // Email 찾기
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.cancelPostsScrap(userEmail, projectId);

        return ResponseEntity.ok(postsDto);
    }

    // 스터디 스크랩 취소
    @PostMapping("/study/cancelScrap/{studyId}")
    public ResponseEntity<PostsDto> cancelStudyScrap(@PathVariable Long studyId, Principal principal) {

        // Email 찾기
        String userEmail = principal.getName();

        PostsDto postsDto = postsService.cancelPostsScrap(userEmail, studyId);

        return ResponseEntity.ok(postsDto);
    }



    // Scrap 페이지에서, 동적 쿼리를 활용해 선택된 배너와 선택한 페이지, 정렬 옵션, 검색어에 따라 게시물을 페이징해서 프런트에 반환하는 컨트롤러
    @GetMapping("/getScrapPosts")
    public ResponseEntity<Page<ScrapPostsDto>> getScrapPosts(
            @RequestParam(defaultValue = "project") String postsOption, //프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 글쓴이
            @RequestParam(defaultValue = "latestPosts") String sortOption, //프론트엔드에서 넘어온 선택된 옵션정보: 디폴트는 최신등록순
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size, //프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수
            Principal principal) {      // 본인이 쓴 글인지, 남이 쓴 글인지 구분하기 위해 현재 유저의 정보 가져오기

        // Email 찾기
        String userEmail = principal.getName();

        Page<ScrapPostsDto> scrapPostsDtos;

        // 프로젝트 버튼을 클릭한 경우, 다음의 코드를 실행
        if ("project".equals(postsOption)) {
            scrapPostsDtos = postsService.getProjectScrapPosts(userEmail, sortOption, PageRequest.of(page, size));
        }
        // 스터디 버튼을 클릭한 경우, 다음의 코드를 실행
        else {
            scrapPostsDtos = postsService.getStudyScrapPosts(userEmail, sortOption, PageRequest.of(page, size));
        }

        return ResponseEntity.ok(scrapPostsDtos);
    }
}

