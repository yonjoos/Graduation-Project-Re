package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.*;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostsService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final CategoryRepository categoryRepository;
    private final UserApplyPostsRepository userApplyPostsRepository;
    private final ScrapPostsRepository scrapPostsRepository;
    private final NotificationsRepository notificationsRepository;
    private final ViewCountPostsRepository viewCountPostsRepository;
    private final NotificationService notificationService;
    private final JPAQueryFactory queryFactory;

    public void uploadProjectPost(PostsFormDto postsFormDto, String userEmail) {
        uploadPost(postsFormDto, userEmail, PostType.PROJECT);
    }

    public void uploadStudyPost(PostsFormDto postsFormDto, String userEmail) {
        uploadPost(postsFormDto, userEmail, PostType.STUDY);
    }


    // uploadProjectPost 함수와 uploadStudyPost 함수는 구조가 거의 동일하다.
    // 디비에 저장할 때 다른 부분은 오직 PostType이다.
    // 따라서 두 함수의 저장 로직 중 겹치는 부분을 따로 함수로 떼어냈다.
    public void uploadPost(PostsFormDto postsFormDto, String userEmail, PostType postType) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts posts = Posts.builder()
                .user(user)
                .postType(postType)
                .title(postsFormDto.getTitle())
                .recruitmentCount(postsFormDto.getRecruitmentCount())
                //.counts(1)      // 맨 처음 지원자 수는 1명 (본인 포함)
                .content(postsFormDto.getContent().replace("<br>", "\n"))
                .promoteImageUrl(postsFormDto.getPromoteImageUrl())
                .fileUrl(postsFormDto.getFileUrl())
                .endDate(postsFormDto.getEndDate())
                .build();

        Posts savedPosts = postsRepository.save(posts);

        Category category = Category.builder()
                .posts(savedPosts)
                .web(postsFormDto.getPostType().contains("Web"))
                .app(postsFormDto.getPostType().contains("App"))
                .game(postsFormDto.getPostType().contains("Game"))
                .ai(postsFormDto.getPostType().contains("AI"))
                .build();

        // Web, App, Game, AI 중 3개 이상 체크했는지 확인. 3개 이상 체크했으면 에러가 발생함.
        category.validateFieldCount();

        categoryRepository.save(category);
    }



//    // 프로젝트 게시물 리스트 조회 -> getFilteredProjects, getFilteredStudies로 대체
//    @Transactional(readOnly = true)
//    @EntityGraph(attributePaths = {"user", "category"})     // 페치 조인으로 조회할 대상 테이블
//    public List<PostsListDto> getProjectList() {
//        return getPostsList(PostType.PROJECT);
//    }
//
//    // 스터디 게시물 리스트 조회
//    @Transactional(readOnly = true)
//    @EntityGraph(attributePaths = {"user", "category"})     // 페치 조인으로 조회할 대상 테이블
//    public List<PostsListDto> getStudyList() {
//        return getPostsList(PostType.STUDY);
//    }
//
//    // 프로젝트 리스트 조회와 스터디 리스트 조회의 공통 코드를 분리하여 메서드로 만듦
//    private List<PostsListDto> getPostsList(PostType postType) {
//        List<Posts> postsList = postsRepository.findByPostType(postType);   // 게시물의 타입에 맞는 게시물 찾기
//
//        List<PostsListDto> postsListDtoList = new ArrayList<>();        // 빈 컬렉션 생성
//
//        for (Posts posts : postsList) {
//            Category category = posts.getCategory();        // posts라는 연결고리를 통해 연결고리로 접근
//            User user = posts.getUser();                    // posts라는 연결고리를 통해 연결고리로 접근
//
//            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
//            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(posts);
//            Integer applyCount;
//
//            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
//            // null이라면, 인원 = 1(본인)
//            if (applyCountOptional.isPresent()) {
//                applyCount = applyCountOptional.get() + 1;
//            } else {
//                applyCount = 1;
//            }
//
//            PostsListDto postsListDto = PostsListDto.builder()
//                    .id(posts.getId())
//                    .nickName(user.getNickName())   // user = posts.getUser()
//                    .title(posts.getTitle())
//                    .web(category.getWeb())     // category = posts.getCategory()
//                    .app(category.getApp())
//                    .game(category.getGame())
//                    .ai(category.getAi())
//                    .counts(applyCount)
//                    .recruitmentCount(posts.getRecruitmentCount())
//                    .endDate(posts.getEndDate())
//                    .build();
//
//            postsListDtoList.add(postsListDto);     // 컬렉션에 추가
//        }
//
//        return postsListDtoList;
//    }



    // 프로젝트 단건 조회
    // @Transactional(readOnly = true) // 조회 함수지만, viewCount를 저장하려면, readOnly = true이면 안됨.
    @EntityGraph(attributePaths = {"user", "category"})
    public PostsDto getProject(String userEmail, Long projectId) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        Posts posts = postsRepository.findByIdAndPostType(projectId, PostType.PROJECT)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        PostsDto postsDto;

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(posts);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // 프로젝트 게시물 작성자라면, viewCount를 저장하지 않고
        // 프로젝트 게시물 작성자가 아니라면 viewCount 저장
        if (!userEmail.equals(posts.getUser().getEmail())) {
            // 단, 해당 유저가 해당 게시물을 방문한 적 없을 때에만 viewCount를 새로 만들어 저장.
            if (viewCountPostsRepository.findByPosts_IdAndUser_Id(projectId, user.getId()).isEmpty()) {
                ViewCountPosts viewCountPosts = ViewCountPosts.builder()
                        .posts(posts)
                        .user(user)
                        .build();

                viewCountPostsRepository.save(viewCountPosts);
            }
        }

        Optional<Integer> viewCountOptional = viewCountPostsRepository.countByPosts_Id(projectId);

        Integer viewCount = viewCountOptional.orElse(0); // 조회수 값이 없으면 0을 사용

        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())와 동일하다면 (게시물 작성자라면)
        if (posts.getUser().getEmail().equals(userEmail)) {
             postsDto = PostsDto.builder()
                    .writer(true)       // writer에 true를 리턴
                    .scrap(false)       // 작성자는 자신의 게시물에 스크랩 X
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    //.counts(posts.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .viewCount(viewCount)
                    .build();
        }
        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())가 아니라면
        else {
            boolean isScrapped = false;     // 스크랩 여부
            boolean hasApplied = false;     // 지원 여부
            boolean isConfirmed = false;    // 승인 여부

            for (ScrapPosts scrap : posts.getScrapPosts()) {
                // userEmail을 가진 사람이 스크랩한 사람 중 한 명이라면,
                if (scrap.getUser().getEmail().equals(userEmail)) {
                    isScrapped = true;      // 스크랩 여부는 true
                    break;
                }
            }

            for (UserApplyPosts apply : posts.getUserApplyPosts()) {
                // userEmail을 가진 사람이 지원한 사람 중 한 명이라면,
                if (apply.getUser().getEmail().equals(userEmail)) {
                    hasApplied = true;      // 지원 여부는 true
                    isConfirmed = apply.getConfirm();   // 승인 여부는 직접 가져오기
                    break; // Exit the loop since we found a matching entry
                }
            }

            // 게시물에 지원 안한 사람
            if (!hasApplied) {
                // 스크랩 한 사람
                if (isScrapped) {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(true)        // 스크랩 한 사람
                            .applying(false)    // 지원중이지도 않고
                            .applied(false)     // 지원 승인되지도 않았음
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
                // 스크랩 안한 사람
                else {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(false)        // 스크랩 안한 사람
                            .applying(false)    // 지원중이지도 않고
                            .applied(false)     // 지원 승인되지도 않았음
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
            }
            // 게시물에 지원했으나, 승인이 안난 사람
            else if (!isConfirmed) {
                // 스크랩 한 사람
                if (isScrapped) {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(true)        // 스크랩 한 사람
                            .applying(true)     // 지원은 했으나 (지원 중이지만),
                            .applied(false)     // 지원이 승인된 것은 아님.
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
                // 스크랩 안한 사람
                else {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(false)       // 스크랩 안한 사람
                            .applying(true)     // 지원은 했으나 (지원 중이지만),
                            .applied(false)     // 지원이 승인된 것은 아님.
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
            }
            // writer로부터 승인이 난 사람
            else {
                // 스크랩 한 사람
                if (isScrapped) {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(true)        // 스크랩 한 사람
                            .applying(false)     // 지원 중은 아니고,
                            .applied(true)     // 지원이 승인되었음.
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
                // 스크랩 안한 사람
                else {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(false)       // 스크랩 안한 사람
                            .applying(false)     // 지원 중은 아니고,
                            .applied(true)     // 지원이 승인되었음.
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
            }
        }

        return postsDto;
    }


    // 스터디 단건 조회
    // @Transactional(readOnly = true) // 조회 함수지만, viewCount를 저장하려면, readOnly = true이면 안됨.
    @EntityGraph(attributePaths = {"user", "category"})
    public PostsDto getStudy(String userEmail, Long studyId) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        Posts posts = postsRepository.findByIdAndPostType(studyId, PostType.STUDY)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        PostsDto postsDto;

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(posts);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // 스터디 게시물 작성자라면, viewCount를 저장하지 않고
        // 스터디 게시물 작성자가 아니라면 viewCount 저장
        if (!userEmail.equals(posts.getUser().getEmail())) {
            // 단, 해당 유저가 해당 게시물을 방문한 적 없을 때에만 viewCount를 새로 만들어 저장.
            if (viewCountPostsRepository.findByPosts_IdAndUser_Id(studyId, user.getId()).isEmpty()) {
                ViewCountPosts viewCountPosts = ViewCountPosts.builder()
                        .posts(posts)
                        .user(user)
                        .build();

                viewCountPostsRepository.save(viewCountPosts);
            }
        }

        Optional<Integer> viewCountOptional = viewCountPostsRepository.countByPosts_Id(studyId);

        Integer viewCount = viewCountOptional.orElse(0); // 조회수 값이 없으면 0을 사용

        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())와 동일하다면
        if (posts.getUser().getEmail().equals(userEmail)) {
            postsDto = PostsDto.builder()
                    .writer(true)      // writer에 true를 리턴
                    .scrap(false)       // 작성자는 자신의 게시물에 스크랩 X
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    //.counts(posts.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .viewCount(viewCount)
                    .build();
        }
        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())가 아니라면
        else {
            boolean isScrapped = false;     // 스크랩 여부
            boolean hasApplied = false;     // 지원 여부
            boolean isConfirmed = false;    // 승인 여부

            for (ScrapPosts scrap : posts.getScrapPosts()) {
                // userEmail을 가진 사람이 스크랩한 사람 중 한 명이라면,
                if (scrap.getUser().getEmail().equals(userEmail)) {
                    isScrapped = true;      // 스크랩 여부는 true
                    break;
                }
            }

            for (UserApplyPosts apply : posts.getUserApplyPosts()) {
                // userEmail을 가진 사람이 지원한 사람 중 한 명이라면,
                if (apply.getUser().getEmail().equals(userEmail)) {
                    hasApplied = true;      // 지원 여부는 true
                    isConfirmed = apply.getConfirm();   // 승인 여부는 직접 가져오기
                    break; // Exit the loop since we found a matching entry
                }
            }

            // 게시물에 지원 안한 사람
            if (!hasApplied) {
                // 스크랩 한 사람
                if (isScrapped) {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(true)        // 스크랩 한 사람
                            .applying(false)    // 지원중이지도 않고
                            .applied(false)     // 지원 승인되지도 않았음
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
                // 스크랩 안한 사람
                else {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(false)        // 스크랩 안한 사람
                            .applying(false)    // 지원중이지도 않고
                            .applied(false)     // 지원 승인되지도 않았음
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
            }
            // 게시물에 지원했으나, 승인이 안난 사람
            else if (!isConfirmed) {
                // 스크랩 한 사람
                if (isScrapped) {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(true)        // 스크랩 한 사람
                            .applying(true)     // 지원은 했으나 (지원 중이지만),
                            .applied(false)     // 지원이 승인된 것은 아님.
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
                // 스크랩 안한 사람
                else {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(false)       // 스크랩 안한 사람
                            .applying(true)     // 지원은 했으나 (지원 중이지만),
                            .applied(false)     // 지원이 승인된 것은 아님.
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
            }
            // writer로부터 승인이 난 사람
            else {
                // 스크랩 한 사람
                if (isScrapped) {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(true)        // 스크랩 한 사람
                            .applying(false)     // 지원 중은 아니고,
                            .applied(true)     // 지원이 승인되었음.
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
                // 스크랩 안한 사람
                else {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
                            .scrap(false)       // 스크랩 안한 사람
                            .applying(false)     // 지원 중은 아니고,
                            .applied(true)     // 지원이 승인되었음.
                            .nickName(posts.getUser().getNickName())
                            .title(posts.getTitle())
                            .web(posts.getCategory().getWeb())
                            .app(posts.getCategory().getApp())
                            .game(posts.getCategory().getGame())
                            .ai(posts.getCategory().getAi())
                            .content(posts.getContent())
                            .promoteImageUrl(posts.getPromoteImageUrl())
                            .fileUrl(posts.getFileUrl())
                            //.counts(posts.getCounts())
                            .counts(applyCount)
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .viewCount(viewCount)
                            .build();
                }
            }
        }

        return postsDto;
    }


    // 게시물에 지원한 사람 모두 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public List<ApplicantDto> getApplicants(String userEmail, Long postId) {

        // postId와 userEmail로 게시물 찾기
        Posts findPost = postsRepository.findByIdAndUser_Email(postId, userEmail)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴. (그 중 승인 받은 애들의 개수만 가져옴)
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPost);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // List<ApplicantDto> 객체 생성
        List<ApplicantDto> applicantDtoList = new ArrayList<>();

        // 검색된 모든 지원자 정보를 createdDate를 기준으로 오름차순으로 가져와서 ApplicantDto로 변환하여 리스트에 추가
        List<UserApplyPosts> userApplyPostsList = userApplyPostsRepository.findByPosts_IdOrderByCreatedDateAsc(findPost.getId());

        for (UserApplyPosts userApplyPosts : userApplyPostsList) {
            User applicantUser = userApplyPosts.getUser();
            ApplicantDto applicantDto = ApplicantDto.builder()
                    .nickName(applicantUser.getNickName()) // 지원자의 닉네임
                    .confirm(userApplyPosts.getConfirm()) // 승인 여부
                    .count(applyCount)                      // 현재 지원자 수
                    .build();
            applicantDtoList.add(applicantDto);
        }

        return applicantDtoList;
    }

    // ** 중요 **
    // postType을 Boolean 리스트로 받아오는 PostsUpdateFormDto 사용!
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public PostsUpdateFormDto getProjectForm(String userEmail, Long projectId) {

        // projectId와 userEmail로 Project 찾기
        Posts findProject = postsRepository.findByIdAndUser_Email(projectId, userEmail)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findProject);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // postUpdateFormDto에 맞는 postType을 위해 List로 변환한다.
        List<Boolean> postTypeList = Arrays.asList(
                findProject.getCategory().getWeb(),
                findProject.getCategory().getApp(),
                findProject.getCategory().getGame(),
                findProject.getCategory().getAi()
        );

        // Create and populate PostsFormDto from project
        PostsUpdateFormDto formDto = PostsUpdateFormDto.builder()
                .title(findProject.getTitle())
                .postType(postTypeList)     // 리스트로 변환된 postType을 반환
                //.counts(findProject.getCounts())
                .counts(applyCount)
                .recruitmentCount(findProject.getRecruitmentCount())
                .endDate(findProject.getEndDate())
                .content(findProject.getContent())
                .promoteImageUrl(findProject.getPromoteImageUrl())
                .fileUrl(findProject.getFileUrl())
                .build();

        return formDto;
    }

    // ** 중요 **
    // postType을 Boolean 리스트로 받아오는 PostsUpdateFormDto 사용!
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public PostsUpdateFormDto getStudyForm(String userEmail, Long studyId) {

        // projectId와 userEmail로 Study 찾기
        Posts findStudy = postsRepository.findByIdAndUser_Email(studyId, userEmail)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findStudy);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // postUpdateFormDto에 맞는 postType을 위해 List로 변환한다.
        List<Boolean> postTypeList = Arrays.asList(
                findStudy.getCategory().getWeb(),
                findStudy.getCategory().getApp(),
                findStudy.getCategory().getGame(),
                findStudy.getCategory().getAi()
        );

        // Create and populate PostsFormDto from study
        PostsUpdateFormDto formDto = PostsUpdateFormDto.builder()
                .title(findStudy.getTitle())
                .postType(postTypeList)     // 리스트로 변환된 postType을 반환
                //.counts(findStudy.getCounts())
                .counts(applyCount)
                .recruitmentCount(findStudy.getRecruitmentCount())
                .endDate(findStudy.getEndDate())
                .content(findStudy.getContent())
                .promoteImageUrl(findStudy.getPromoteImageUrl())
                .fileUrl(findStudy.getFileUrl())
                .build();

        return formDto;
    }



    // ** 중요 **
    // postType을 String 리스트로 받아오는 PostsFormDto 사용!
    @EntityGraph(attributePaths = {"category"})
    public void updateProject(Long projectId, PostsFormDto postsFormDto) {

        // projectId로 Project 찾기
        Posts project = postsRepository.findById(projectId)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // 변경 감지를 통한 업데이트
        project.setTitle(postsFormDto.getTitle());
        project.setRecruitmentCount(postsFormDto.getRecruitmentCount());
        project.setContent(postsFormDto.getContent().replace("<br>", "\n"));
        project.setPromoteImageUrl(postsFormDto.getPromoteImageUrl());
        project.setFileUrl(postsFormDto.getFileUrl());
        project.setEndDate(postsFormDto.getEndDate());

        project.getCategory().setWeb(postsFormDto.getPostType().contains("Web"));
        project.getCategory().setApp(postsFormDto.getPostType().contains("App"));
        project.getCategory().setGame(postsFormDto.getPostType().contains("Game"));
        project.getCategory().setAi(postsFormDto.getPostType().contains("AI"));

        // 카운트 검증 (recruitmentCount의 개수가 2개 이하인가?를 검증)
        project.getCategory().validateFieldCount();

        // 저장
        postsRepository.save(project);
    }


    // ** 중요 **
    // postType을 String 리스트로 받아오는 PostsFormDto 사용!
    @EntityGraph(attributePaths = {"category"})
    public void updateStudy(Long studyId, PostsFormDto postsFormDto) {

        // projectId로 Project 찾기
        Posts study = postsRepository.findById(studyId)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // 변경 감지를 통한 업데이트
        study.setTitle(postsFormDto.getTitle());
        study.setRecruitmentCount(postsFormDto.getRecruitmentCount());
        study.setContent(postsFormDto.getContent().replace("<br>", "\n"));
        study.setPromoteImageUrl(postsFormDto.getPromoteImageUrl());
        study.setFileUrl(postsFormDto.getFileUrl());
        study.setEndDate(postsFormDto.getEndDate());

        study.getCategory().setWeb(postsFormDto.getPostType().contains("Web"));
        study.getCategory().setApp(postsFormDto.getPostType().contains("App"));
        study.getCategory().setGame(postsFormDto.getPostType().contains("Game"));
        study.getCategory().setAi(postsFormDto.getPostType().contains("AI"));

        // 카운트 검증 (recruitmentCount의 개수가 2개 이하인가?를 검증)
        study.getCategory().validateFieldCount();

        // 저장
        postsRepository.save(study);
    }


    // 프로젝트 삭제
    @EntityGraph(attributePaths = {"category"})
    public void deleteProject(Long projectId) {
        // projectId로 해당 게시물 찾기
        Posts posts = postsRepository.findById(projectId)
                .orElseThrow(() -> new AppException("Project not found", HttpStatus.NOT_FOUND));

        // 프로젝트 게시물과 연관된 카테고리 찾기
        Category category = posts.getCategory();

        if (category != null) { // 카테고리 먼저 지워야
            // Delete the category
            categoryRepository.delete(category);
        }

        // 프로젝트도 삭제 가능
        postsRepository.delete(posts);
    }

    // 스터디 삭제
    @EntityGraph(attributePaths = {"category"})
    public void deleteStudy(Long studyId) {
        // studyId로 해당 게시물 찾기
        Posts posts = postsRepository.findById(studyId)
                .orElseThrow(() -> new AppException("Study not found", HttpStatus.NOT_FOUND));

        // 스터디 게시물과 연관된 카테고리 찾기
        Category category = posts.getCategory();

        // 카테고리 먼저 지워야
        if (category != null) { // 카테고리 먼저 지워야
            // Delete the category
            categoryRepository.delete(category);
        }
        // 스터디도 삭제 가능
        postsRepository.delete(posts);
    }



    //게시물 조회 동적쿼리 + 페이징 in 프로젝트 게시물
    @Transactional(readOnly = true) //읽기 전용
    public Page<PostsListDto> getFilteredProjects(List<String> selectedBanners, String sortOption, String searchTerm, Pageable pageable) {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QViewCountPosts viewCountPosts = QViewCountPosts.viewCountPosts;

        //System.out.println("pageable.getOffset() = " + pageable.getOffset());
        //System.out.println("pageable.getPageSize() = " + pageable.getPageSize());
        //System.out.println("searchTerm = " + searchTerm);
        //System.out.println("searchTerm = " + searchTerm.getClass());

        // buildBannerConditionsInProjects 메서드를 사용하여 선택한 배너를 기반으로 BooleanExpression을 구성
        BooleanExpression bannerConditions = buildBannerConditionsInProjects(category, selectedBanners);

        // 검색어 기반으로 필터링할 때 쓰는 BooleanExpression 조건
        BooleanExpression titleOrContentConditions = null;

//        아래 주석은 공백 기호 and조건 없이 그냥 단순하게 찾는 기법
//        if (!searchTerm.isEmpty()) {
//            String lowerSearchTerm = searchTerm.toLowerCase();
//            titleOrContentConditions = posts.title.lower().contains(lowerSearchTerm )
//                    .or(posts.content.lower().contains(lowerSearchTerm));
//        }

//      검색어 문자열을 공백 기호 기준으로 다 split해서 배열로 만들고, 각 배열 요소에 담긴 키워드 조각들을 and한 결과가 게시물에 있으면 해당 게시물이 추출됨
        if (!searchTerm.isEmpty()) { // 만약 문자열이 공백이 아니라면
            String[] keywords = searchTerm.split("\\s+"); // 공백으로 분리한 검색어 배열 생성

            // 각 단어를 처리하여 BooleanExpression 조건 생성
            List<BooleanExpression> keywordConditions = Arrays.stream(keywords)
                    .map(keyword -> posts.title.lower().like("%" + keyword.toLowerCase() + "%") // keyword가 포함된 게시물 title이 있으면 추출될 게시물로 선정
                            .or(posts.content.lower().like("%" + keyword.toLowerCase() + "%"))) // keyword가 포함된 게시물 content가 있으면 추출될 게시물로 선정
                    .collect(Collectors.toList()); // 각 키워드 조각들에 대해 title에 포함됨? content에 포함됨? 에 대한 조건을 모두 만들어 list로 만든다.

            titleOrContentConditions = keywordConditions.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(null); // 모든 조건이 없을 경우 null로 설정


//             앞서 만든 검색어에 대한 모든 조건들을 and 연산하여 검색어 조건 생성 완료
//             ex: 오늘 김밥 먹음 -> ('오늘'을 포함한 게시물 제목 or '오늘'을 포함한 게시물 컨텐츠)
//                                    &&
//                                  ('김밥'을 포함한 게시물 제목 or '김밥'을 포함한 게시물 컨텐츠)
//                                    &&
//                                  ('먹음'을 포함한 게시물 제목 or '먹음'을 포함한 게시물 컨텐츠)
//            -----> 따라서 순서에 상관 없이 게시물 내용이나 제목에 '오늘', '김밥', '먹음'이 모두 포함된 게시물만 필터링되는 조건 완성
        }

        // 현재 날짜를 가져옴
        LocalDate currentDate = LocalDate.now();

        // endDate가 현재 날짜 이후인 게시물을 필터링하기 위한 조건
        BooleanExpression notExpiredCondition = posts.endDate.goe(currentDate);

        // 다른 조건과 연결
        BooleanExpression finalConditions = bannerConditions.and(notExpiredCondition);

        // '데이터'를 가져오는 쿼리
        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물을 추출할 건데,
                .join(posts.category, category).fetchJoin() // 게시물을 카테고리와 조인한 형태로 가져올거임
                .where(finalConditions, posts.postType.eq(PostType.valueOf("PROJECT")));
                // (where로 조건 추가 1.) 근데 조건은 이러하고 (밑에 있음)
                // (where로 조건 추가 2.) 게시물의 TYPE이 프로젝트인 것만 가져옴

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (titleOrContentConditions != null) {
            query = query.where(titleOrContentConditions);
        }

        // 정렬 옵션에 따른 조건 추가
        if (sortOption.equals("nearDeadline")) {    // 가까운 마감일 순 정렬
            query = query.orderBy(posts.endDate.asc());
        } else if (sortOption.equals("byViewCount")) {  // 조회수 순 정렬
            query = query
                    .leftJoin(viewCountPosts).on(posts.id.eq(viewCountPosts.posts.id))
                    .groupBy(posts, category)
                    .orderBy(viewCountPosts.count().intValue().desc(), posts.lastModifiedDate.desc());
        } else {    // 최신 등록 순 정렬
            query = query.orderBy(posts.lastModifiedDate.desc());
        }

        // '카운트 쿼리' 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<Posts> countQuery = queryFactory.selectFrom(posts)
                .join(posts.category, category) // Join with category
                .where(bannerConditions,posts.postType.eq(PostType.valueOf("PROJECT")), notExpiredCondition);

//              .orderBy(posts.lastModifiedDate.desc()); 카운트 쿼리에선 정렬 필요없음

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (titleOrContentConditions != null) {
            countQuery = countQuery.where(titleOrContentConditions);
        }


        long total = countQuery.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄

        // 데이터를 가져오는 쿼리를 실제로 offset, limit까지 설정해서 쿼리 날림
        List<Posts> filteredPosts = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        List<PostsListDto> postsListDtoList = new ArrayList<>(); // 빈 컬렉션 생성

        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (Posts post : filteredPosts) {
            Category postCategory = post.getCategory();        // posts를 통해 카테고리로 접근한 것을 postCategory로 명명
            User user = post.getUser();                         // posts를 통해 유저 접근한 것을 user로 명명

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);
            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            // ViewCountPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> viewCountOptional = viewCountPostsRepository.countByPosts_Id(post.getId());
            Integer viewCount;

            // viewCountOptional에 값이 존재한다면, 해당 값 가져오기
            // null이라면 조회수는 0으로 세팅
            if (viewCountOptional.isPresent()) {
                viewCount = viewCountOptional.get();
            } else {
                viewCount = 0;
            }

            PostsListDto postsListDto = PostsListDto.builder()
                    .id(post.getId())
                    .nickName(user.getNickName())   // user = posts.getUser()
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    //.counts(post.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    .briefContent(post.getContent())
                    .viewCount(viewCount)
                    .build();

            postsListDtoList.add(postsListDto);     // 컬렉션에 추가
        }

        return new PageImpl<>(postsListDtoList, pageable, total); // 동적쿼리의 결과를 반환
    }

    // 프로젝트 게시물 조회에서 선택된 배너[app,web,ai,game] 에 따라 동적 쿼리의 where절에 들어갈 조건 생성하기
    // 스터디는 카테고리에 추가 필드가 생길 여지가 있으므로 별도로 필터링 조건을 프로젝트 필터링과 구분하였음
    private BooleanExpression buildBannerConditionsInProjects(QCategory category, List<String> selectedBanners) {

        // 카테고리가 null상태가 아닌 게시물만 쿼리에서 고려하기 위해 세팅
        // 즉 해당 condition이 반환되면 카테고리가 널이 아닌 모든 게시물에 대해 조회됨
        BooleanExpression condition = category.isNotNull();

//        System.out.println("condition = " + condition);
//        System.out.println("selectedBanners = " + selectedBanners);

//        선택한 배너에 "all"이 포함되어 있으면
//        사용자가 모든 카테고리에서 게시물을 검색하기를 원한다는 의미이므로 메서드는 단순히 초기 조건을 반환
        if (selectedBanners.contains("all")) {
            return condition;
        }

        // 앞선 condition에 별도로 더 추가할 condition인 bannerExpression
        // 여기서 각 배너가 어떻게 선택되었는지에 따라 where절에 들어갈 조건이 결정됨
        BooleanExpression bannerExpression = null;

        // bannerExpression에 이미 어떤 조건이 들어가있다면 현재 bannerExpression에 web필드가 true인 걸 찾으라는 조건을 추가
        // bannerExpression에 어떠한 조건도 아직 들어가있지 않다면 bannerExpression에 web필드가 true인 걸 찾으라는 조건을 처음 세팅함
        if (selectedBanners.contains("web")) {
            bannerExpression = bannerExpression != null
                    ? bannerExpression.and(category.web.isTrue())
                    : category.web.isTrue();
        }

        // bannerExpression에 이미 어떤 조건이 들어가있다면 현재 bannerExpression에 app필드가 true인 걸 찾으라는 조건을 추가
        // bannerExpression에 어떠한 조건도 아직 들어가있지 않다면 bannerExpression에 app필드가 true인 걸 찾으라는 조건을 처음 세팅함
        if (selectedBanners.contains("app")) {
            bannerExpression = bannerExpression != null
                    ? bannerExpression.and(category.app.isTrue())
                    : category.app.isTrue();
        }

        // bannerExpression에 이미 어떤 조건이 들어가있다면 현재 bannerExpression에 game필드가 true인 걸 찾으라는 조건을 추가
        // bannerExpression에 어떠한 조건도 아직 들어가있지 않다면 bannerExpression에 game필드가 true인 걸 찾으라는 조건을 처음 세팅함
        if (selectedBanners.contains("game")) {
            bannerExpression = bannerExpression != null
                    ? bannerExpression.and(category.game.isTrue())
                    : category.game.isTrue();
        }

        // bannerExpression에 이미 어떤 조건이 들어가있다면 현재 bannerExpression에 ai필드가 true인 걸 찾으라는 조건을 추가
        // bannerExpression에 어떠한 조건도 아직 들어가있지 않다면 bannerExpression에 ai필드가 true인 걸 찾으라는 조건을 처음 세팅함
        if (selectedBanners.contains("ai")) {
            bannerExpression = bannerExpression != null
                    ? bannerExpression.and(category.ai.isTrue())
                    : category.ai.isTrue();
        }

        // 최종적으로 where절에 들거갈 조건 완성해서 반환
        return condition.and(bannerExpression);
    }

    //게시물 조회 동적쿼리 + 페이징 in 스터디 게시물
    @Transactional(readOnly = true) //읽기 전용
    public Page<PostsListDto> getFilteredStudies(List<String> selectedBanners, String sortOption, String searchTerm, Pageable pageable) {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QViewCountPosts viewCountPosts = QViewCountPosts.viewCountPosts;

//        System.out.println("pageable.getOffset() = " + pageable.getOffset());
//        System.out.println("pageable.getPageSize() = " + pageable.getPageSize());
//        System.out.println("searchTerm = " + searchTerm);
//        System.out.println("searchTerm = " + searchTerm.getClass());

        // buildBannerConditionsInStudies 메서드를 사용하여 선택한 배너를 기반으로 BooleanExpression을 구성
        BooleanExpression bannerConditions = buildBannerConditionsInStudies(category, selectedBanners);

        // 검색어 기반으로 필터링할 때 쓰는 BooleanExpression 조건
        BooleanExpression titleOrContentConditions = null;

//      검색어 문자열을 공백 기호 기준으로 다 split해서 배열로 만들고, 각 배열 요소에 담긴 키워드 조각들을 and한 결과가 게시물에 있으면 해당 게시물이 추출됨
        if (!searchTerm.isEmpty()) { // 만약 문자열이 공백이 아니라면
            String[] keywords = searchTerm.split("\\s+"); // 공백으로 분리한 검색어 배열 생성

            // 각 단어를 처리하여 BooleanExpression 조건 생성
            List<BooleanExpression> keywordConditions = Arrays.stream(keywords)
                    .map(keyword -> posts.title.lower().like("%" + keyword.toLowerCase() + "%") // keyword가 포함된 게시물 title이 있으면 추출될 게시물로 선정
                            .or(posts.content.lower().like("%" + keyword.toLowerCase() + "%"))) // keyword가 포함된 게시물 content가 있으면 추출될 게시물로 선정
                    .collect(Collectors.toList()); // 각 키워드 조각들에 대해 title에 포함됨? content에 포함됨? 에 대한 조건을 모두 만들어 list로 만든다.

            titleOrContentConditions = keywordConditions.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(null); // 모든 조건이 없을 경우 null로 설정


//             앞서 만든 검색어에 대한 모든 조건들을 and 연산하여 검색어 조건 생성 완료
//             ex: 오늘 김밥 먹음 -> ('오늘'을 포함한 게시물 제목 or '오늘'을 포함한 게시물 컨텐츠)
//                                    &&
//                                  ('김밥'을 포함한 게시물 제목 or '김밥'을 포함한 게시물 컨텐츠)
//                                    &&
//                                  ('먹음'을 포함한 게시물 제목 or '먹음'을 포함한 게시물 컨텐츠)
//            -----> 따라서 순서에 상관 없이 게시물 내용이나 제목에 '오늘', '김밥', '먹음'이 모두 포함된 게시물만 필터링되는 조건 완성
        }

        // 현재 날짜를 가져옴
        LocalDate currentDate = LocalDate.now();

        // endDate가 현재 날짜 이후인 게시물을 필터링하기 위한 조건
        BooleanExpression notExpiredCondition = posts.endDate.goe(currentDate);

        // 다른 조건과 연결
        BooleanExpression finalConditions = bannerConditions.and(notExpiredCondition);

        // 데이터를 가져오는 쿼리
        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물을 추출할 건데,
                .join(posts.category, category).fetchJoin() // 게시물을 카테고리와 조인한 형태로 가져올거임
                .where(finalConditions, posts.postType.eq(PostType.valueOf("STUDY")));
                // (where로 조건 추가 1.) 근데 조건은 이러하고 (밑에 있음)
                // (where로 조건 추가 2.) 게시물의 TYPE이 스터디인 것만 가져옴

                // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임

        if (titleOrContentConditions != null) {
            query = query.where(titleOrContentConditions);
        }

        // 정렬 옵션에 따른 조건 추가
        if (sortOption.equals("nearDeadline")) {    // 가까운 마감일 순 정렬
            query = query.orderBy(posts.endDate.asc());
        } else if (sortOption.equals("byViewCount")) {  // 조회수 순 정렬
            query = query
                    .leftJoin(viewCountPosts).on(posts.id.eq(viewCountPosts.posts.id))
                    .groupBy(posts, category)
                    .orderBy(viewCountPosts.count().intValue().desc(), posts.lastModifiedDate.desc());
        } else {    // 최신 등록 순 정렬
            query = query.orderBy(posts.lastModifiedDate.desc());
        }

        // 카운트 쿼리 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<Posts> countQuery = queryFactory.selectFrom(posts)
                .join(posts.category, category) // Join with category
                .where(bannerConditions,posts.postType.eq(PostType.valueOf("STUDY")), notExpiredCondition);

        // .orderBy(posts.lastModifiedDate.desc()); 카운트 쿼리에선 정렬 필요없음

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한 번 더 엮임
        if (titleOrContentConditions != null) {
            countQuery = countQuery.where(titleOrContentConditions);
        }

        long total = countQuery.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄

        // 데이터를 가져오는 쿼리를 실제로 offset, limit까지 설정해서 쿼리 날림
        List<Posts> filteredPosts = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        List<PostsListDto> postsListDtoList = new ArrayList<>(); // 빈 컬렉션 생성

        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (Posts post : filteredPosts) {
            Category postCategory = post.getCategory();        // posts를 통해 카테고리로 접근한 것을 postCategory로 명명
            User user = post.getUser();                         // posts를 통해 유저 접근한 것을 user로 명명

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);
            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            // ViewCountPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> viewCountOptional = viewCountPostsRepository.countByPosts_Id(post.getId());
            Integer viewCount;

            // viewCountOptional에 값이 존재한다면, 해당 값 가져오기
            // null이라면 조회수는 0으로 세팅
            if (viewCountOptional.isPresent()) {
                viewCount = viewCountOptional.get();
            } else {
                viewCount = 0;
            }

            PostsListDto postsListDto = PostsListDto.builder()
                    .id(post.getId())
                    .nickName(user.getNickName())   // user = posts.getUser()
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    //.counts(post.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    .briefContent(post.getContent())
                    .viewCount(viewCount)
                    .build();

            postsListDtoList.add(postsListDto);     // 컬렉션에 추가
        }

        return new PageImpl<>(postsListDtoList, pageable, total); // 동적쿼리의 결과를 반환

    }

    // 스터디 게시물 조회에서 동적 쿼리의 where절에 들어갈 조건 생성하기
    private BooleanExpression buildBannerConditionsInStudies(QCategory category, List<String> selectedBanners) {

        // 카테고리가 null상태가 아닌 게시물만 쿼리에서 고려하기 위해 세팅
        // 즉 해당 condition이 반환되면 카테고리가 널이 아닌 모든 게시물에 대해 조회됨
        BooleanExpression condition = category.isNotNull();

//        System.out.println("condition = " + condition);
//        System.out.println("selectedBanners = " + selectedBanners);

//        선택한 배너에 "all"이 포함되어 있으면
//        사용자가 모든 카테고리에서 게시물을 검색하기를 원한다는 의미이므로 메서드는 단순히 초기 조건을 반환
        if (selectedBanners.contains("all")) {
            return condition;
        }

        // 앞선 condition에 별도로 더 추가할 condition인 bannerExpression
        // 여기서 각 배너가 어떻게 선택되었는지에 따라 where절에 들어갈 조건이 결정됨
        BooleanExpression bannerExpression = null;

        // bannerExpression에 이미 어떤 조건이 들어가있다면 현재 bannerExpression에 web필드가 true인 걸 찾으라는 조건을 추가
        // bannerExpression에 어떠한 조건도 아직 들어가있지 않다면 bannerExpression에 web필드가 true인 걸 찾으라는 조건을 처음 세팅함
        if (selectedBanners.contains("web")) {
            bannerExpression = bannerExpression != null
                    ? bannerExpression.and(category.web.isTrue())
                    : category.web.isTrue();
        }

        // bannerExpression에 이미 어떤 조건이 들어가있다면 현재 bannerExpression에 app필드가 true인 걸 찾으라는 조건을 추가
        // bannerExpression에 어떠한 조건도 아직 들어가있지 않다면 bannerExpression에 app필드가 true인 걸 찾으라는 조건을 처음 세팅함
        if (selectedBanners.contains("app")) {
            bannerExpression = bannerExpression != null
                    ? bannerExpression.and(category.app.isTrue())
                    : category.app.isTrue();
        }

        // bannerExpression에 이미 어떤 조건이 들어가있다면 현재 bannerExpression에 game필드가 true인 걸 찾으라는 조건을 추가
        // bannerExpression에 어떠한 조건도 아직 들어가있지 않다면 bannerExpression에 game필드가 true인 걸 찾으라는 조건을 처음 세팅함
        if (selectedBanners.contains("game")) {
            bannerExpression = bannerExpression != null
                    ? bannerExpression.and(category.game.isTrue())
                    : category.game.isTrue();
        }

        // bannerExpression에 이미 어떤 조건이 들어가있다면 현재 bannerExpression에 ai필드가 true인 걸 찾으라는 조건을 추가
        // bannerExpression에 어떠한 조건도 아직 들어가있지 않다면 bannerExpression에 ai필드가 true인 걸 찾으라는 조건을 처음 세팅함
        if (selectedBanners.contains("ai")) {
            bannerExpression = bannerExpression != null
                    ? bannerExpression.and(category.ai.isTrue())
                    : category.ai.isTrue();
        }

        // 최종적으로 where절에 들거갈 조건 완성해서 반환
        return condition.and(bannerExpression);
    }



    // GroupPage에 내가 작성한 게시물 데이터를 가져오는 메서드
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
    public Page<GroupPostsDto> getWriterPosts(String userEmail, String sortOption, Pageable pageable) {
        QPosts posts = QPosts.posts; // 게시물 엔티티에 대한 QueryDSL Q타입 객체 생성
        QCategory category = QCategory.category; // 카테고리 엔티티에 대한 QueryDSL Q타입 객체 생성
        QUserApplyPosts userApplyPosts = QUserApplyPosts.userApplyPosts; // 사용자 지원 게시물 엔티티에 대한 QueryDSL Q타입 객체 생성

        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물 조회 쿼리 생성
                .join(posts.category, category).fetchJoin() // 게시물과 카테고리를 조인
                .leftJoin(posts.userApplyPosts, userApplyPosts).fetchJoin() // 게시물과 사용자 지원 게시물을 왼쪽 조인
                .where(posts.user.email.eq(userEmail)) // 현재 로그인한 사용자의 이메일과 일치하는 게시물만 선택
                .orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc()) // 정렬 옵션에 따라 정렬 방식 지정
                .orderBy(userApplyPosts.createdDate.asc());      // 게시물에 지원한 유저를 선착순으로 보여주기

        List<Posts> filteredPosts = query
                .fetch(); // 게시물 데이터를 가져옴

        List<GroupPostsDto> groupPostsDtosList = new ArrayList<>(); // GroupPostsDto 객체를 저장할 리스트 생성

        for (Posts post : filteredPosts) { // 가져온 게시물을 순회
            Category postCategory = post.getCategory();        // post를 통해 카테고리로 접근한 것을 postCategory로 명명
            User user = post.getUser();                         // post를 통해 유저 접근한 것을 user로 명명

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);
            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            List<UserApplyPosts> userApplyPost = post.getUserApplyPosts()   // 게시물에 지원한 유저를 선착순으로 보여주기 위해, lastModifiedDate를 기준으로 정렬해서 가져옴
                    .stream()
                    .sorted(Comparator.comparing(UserApplyPosts::getCreatedDate))   // // 승인을 하는 순간, 데이터가 변경되므로, 승인버튼에서 유저의 위치가 뒤죽박죽 된다. 따라서 getLastModifiedDate가 아닌 getCreatedDate를 사용한다.
                    .collect(Collectors.toList());

            List<String> applyNickNames = userApplyPost.stream()
                    .map(userApply -> userApply.getUser().getNickName()) // 각 사용자의 닉네임을 가져와 리스트로 저장
                    .collect(Collectors.toList());

            List<Boolean> approvedList = userApplyPost.stream()
                    .map(userApply -> userApply.getConfirm()) // 각 사용자의 승인 허가 여부를 가져와 리스트로 저장
                    .collect(Collectors.toList());

            GroupPostsDto groupPostsDto = GroupPostsDto.builder() // GroupPostsDto 객체 생성
                    .id(post.getId())
                    .writerNickName(user.getNickName()) // 게시물 작성자의 닉네임 설정
                    .applyNickNames(applyNickNames) // 이 게시물에 지원한 사용자의 닉네임 리스트 설정
                    .postType(post.getPostType().toString()) // 게시물 타입 설정. Enum이므로, toString()해주기.
                    .title(post.getTitle()) // 게시물 제목 설정
                    .web(postCategory.getWeb()) // 카테고리의 웹 정보 설정
                    .app(postCategory.getApp()) // 카테고리의 앱 정보 설정
                    .game(postCategory.getGame()) // 카테고리의 게임 정보 설정
                    .ai(postCategory.getAi()) // 카테고리의 AI 정보 설정
                    //.counts(post.getCounts()) // 현재까지 승인된 인원 수 설정
                    .counts(applyCount)
                    .recruitmentCount(post.getRecruitmentCount()) // 총 모집 인원 설정
                    .endDate(post.getEndDate()) // 게시물의 마감일 설정
                    .approved(approvedList) // userApplyPost에서 각 게시물에 대해 지원한 유저들의 승인 허가 여부 리스트 설정
                    .isApproved(null) // 작성자 승인 여부 설정 (null로 초기화)
                    .isFull(null) // 정원이 다 찼는지 여부 설정 (null로 초기화)
                    .build(); // GroupPostsDto 객체 생성 및 초기화

            groupPostsDtosList.add(groupPostsDto); // 생성한 GroupPostsListDto를 리스트에 추가
        }

        // 변환한 데이터의 총 개수 계산
        long total = groupPostsDtosList.size();

        // 페이지네이션을 위한 offset과 pageSize 계산, 예외 상황 처리
        int offset = (int) pageable.getOffset();    // 현재 페이지 정보 가져오기
        int pageSize = pageable.getPageSize();      // 현재 페이지에 몇 개를 띄울건지 size 정보 가져오기

        // offset이 데이터의 총 개수를 초과하면 더 이상 데이터를 가져올 필요 없음
        if (offset >= total) {
            offset = 0;
            pageSize = 0;
        }
        // 현재 페이지 + 현재 페이지에서 보고있는 게시물의 개수 > 총 게시물의 개수라면, 페이지 사이즈 재조정
        else if (offset + pageSize > total) {
            pageSize = (int) (total - offset);
        }

        // 페이지 객체 생성하여 반환
        // subList : 시작 위치부터 시작 위치 + 페이지 크기 범위의 데이터만 추출
        // Pageable : 현재 페이지 번호, 페이지 크기 등이 포함되어 있어, 이를 기반으로 데이터를 추출하고 페이지 정보를 생성
        // total : 전체 데이터의 개수를 나타냄
        return new PageImpl<>(groupPostsDtosList.subList(offset, offset + pageSize), pageable, total);
    }



    // GroupPage에 내가 지원한 게시물 데이터를 가져오는 메서드
    @Transactional(readOnly = true) //읽기 전용
    // PageRequest.of(page, size)을 인자로 받을 때, 파라미터의 이름은 pageable로 바꾸어 설정
    public Page<GroupPostsDto> getApplicantPosts(String userEmail, String sortOption, Pageable pageable) {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QUserApplyPosts userApplyPosts = QUserApplyPosts.userApplyPosts;

        // '데이터'를 가져오는 쿼리
        JPAQuery<UserApplyPosts> query = queryFactory.selectFrom(userApplyPosts) // 게시물을 추출할 건데,
                .join(userApplyPosts.posts, posts).fetchJoin()  // 게시물과 지원 게시물을 조인할거고
                .join(posts.category, category).fetchJoin()     // 게시물을 카테고리와 조인한 형태로 가져올거임
                .where(userApplyPosts.user.email.eq(userEmail))  // 근데 userEmail과 지원한 이메일이 같아야 해.
                .orderBy(sortOption.equals("nearDeadline") ? userApplyPosts.posts.endDate.asc() : userApplyPosts.posts.createdDate.desc()); // 정렬 옵션에 따른 조건 추가
                //만약 소트 조건이 마감일순이면 마감일 순 정렬, 아니면 최신등록순 정렬

        // '카운트 쿼리' 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<UserApplyPosts> countQuery = queryFactory.selectFrom(userApplyPosts)
                .join(userApplyPosts.posts, posts)
                .join(posts.category, category)
                .where(userApplyPosts.user.email.eq(userEmail));

        long total = countQuery.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄

        // 데이터를 가져오는 쿼리를 실제로 offset, limit까지 설정해서 쿼리 날림
        List<UserApplyPosts> filteredPosts = query
                .offset(pageable.getOffset())       // Controller에서 인자로 넘겨준 page
                .limit(pageable.getPageSize())      // Controller에서 인자로 넘겨준 size
                .fetch();

        List<GroupPostsDto> groupPostsDtosList = new ArrayList<>(); // 빈 컬렉션 생성


        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (UserApplyPosts userApplyPost : filteredPosts) {
            Posts post = userApplyPost.getPosts();          // userApplyPost를 통해 posts에 접근한 것을 post로 명명
            Category postCategory = userApplyPost.getPosts().getCategory();        // userApplyPost에서 posts를 통해 카테고리로 접근한 것을 postCategory로 명명

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);
            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            GroupPostsDto groupPostsDto = GroupPostsDto.builder()
                    .id(post.getId())
                    .writerNickName(post.getUser().getNickName())   // post = userApplyPost.getPosts()
                    .applyNickNames(null)                       // 지원한 게시물을 찾는 것이므로, 지원자는 없음. null로 반환.
                    .postType(post.getPostType().toString())    // postType은 Enum 타입이므로, toString() 해주기
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    //.counts(post.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    .approved(null)                 // 지원한 게시물이므로, 지원 승인한 사람들의 리스트는 없음. null로 반환.
                    .isApproved(userApplyPost.getConfirm())     // 지원한 게시물이므로, 내가 승인 받았는지의 여부를 DTO에 포함하여 넘겨주어야 함.
                    .isFull(null)           // 프론트 측에 DTO로 반환했을 때, 정원이 다 찼는지의 여부는 어차피 사용하지 않으므로, null로 세팅
                    .build();

            groupPostsDtosList.add(groupPostsDto);     // 컬렉션에 추가
        }

        return new PageImpl<>(groupPostsDtosList, pageable, total); // 동적쿼리의 결과를 반환
    }



    // 프로젝트에 지원하는 것과 관련된 메서드
    // 어차피 프로젝트와 스터디는 모두 Posts라는 한 개의 테이블 안에 있기 때문에, applyProject와 applyStudy를 나눌 필요가 없음.
    public PostsDto applyPosts(String userEmail, Long postsId) {

        // email로 현재 유저 찾기
        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // postsId로 게시물 찾기
        Posts findPosts = postsRepository.findById(postsId)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 해당 유저가 이 게시물에 스크랩 했는지의 여부를 알기 위해 사용
        Optional<ScrapPosts> scrapPosts = scrapPostsRepository.findByUser_IdAndPosts_Id(findUser.getId(), postsId);

        // UserApplyPosts 테이블에 들어갈 내용 채우기
        UserApplyPosts userApplyPosts = UserApplyPosts.builder()
                .user(findUser)
                .posts(findPosts)
                .confirm(false)     // 초기에는 승인되지 않았으므로, false
                .build();

        UserApplyPosts savedUserApplyPosts = userApplyPostsRepository.save(userApplyPosts);

        System.out.println("====== notificationService.notify(findPosts.getUser().getNickName(), \"게시물에 새로운 지원자가 있습니다.\"); ======");
        System.out.println("findPosts.getUser().getId() = " + findPosts.getUser().getId());

        String findUserNickname= findUser.getNickName(); // 지원자 닉네임
        String findPostsTitle = findPosts.getTitle(); // 게시물 제목
        User findPostUser = findPosts.getUser(); // 게시물 작성자

        NotificationMessageDto notificationMessage;
        String notifyMessage;

        if (PostType.PROJECT.equals(findPosts.getPostType())) {
            // notificationMessage에 들어갈 내용을 그냥 String으로 적으면, 한글은 알아듣지 못해 ???로 나온다.
            // 이를 해결하기 위해 NotificationMessageDto를 만들고, 이 안에 값을 생성하여 반환하도록 함으로써, JSON으로 반환하여 한글을 인식하도록 한다.

            // notificationMessage : 실시간 알림 카드에 들어갈 내용
            // notifyMessage : Notification 배너 안에 들어갈 카드 내용
            notificationMessage = new NotificationMessageDto("project/detail/" + postsId + ": 프로젝트 게시물 : \"" + findPostsTitle + "\"에 \"" + findUserNickname + "\"님이 지원하셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
            notifyMessage = "프로젝트 게시물 : \"" + findPostsTitle + "\"에 \"" + findUserNickname + "\"님이 지원하셨습니다.";
        }
        else  {
            notificationMessage = new NotificationMessageDto("study/detail/" + postsId + ": 스터디 게시물 : \"" + findPostsTitle + "\"에 \"" + findUserNickname + "\"님이 지원하셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
            notifyMessage = "스터디 게시물 : \"" + findPostsTitle + "\"에 \"" + findUserNickname + "\"님이 지원하셨습니다.";
        }


        // 지원을 하면, 게시물 작성자의 알림 배너 안에 해당 알림 내용이 들어있어야함
        // 실제 jpa를 통해 notification을 만들어 저장한다.
        Notifications applyNotification = Notifications.builder()
                .user(findPostUser)
                .postId(postsId)
                .notificationMessage(notifyMessage)
                .postType(findPosts.getPostType())
                .checked(false)
                .build();

        Notifications savedNotification = notificationsRepository.save(applyNotification);

        // 기존의 Data에 넣을 메시지에, notification id를 추가해서 보냄
        notificationMessage.setMessage(notificationMessage.getMessage() + savedNotification.getId().toString());

        notificationService.notify(findPosts.getUser().getId(), notificationMessage); // 게시물 작성자에게 실시간 알림 전송
        System.out.println("==========================================================");



        PostsDto postsDto;

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPosts);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // 해당 게시물을 스크랩했다면
        if (scrapPosts.isPresent()) {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .scrap(true)        // 스크랩 한 유저
                    .applying(true)     // 지원은 했으나 (지원 중이지만),
                    .applied(false)     // 지원이 승인된 것은 아님.
                    .nickName(findPosts.getUser().getNickName())
                    .title(findPosts.getTitle())
                    .web(findPosts.getCategory().getWeb())
                    .app(findPosts.getCategory().getApp())
                    .game(findPosts.getCategory().getGame())
                    .ai(findPosts.getCategory().getAi())
                    .content(findPosts.getContent())
                    .promoteImageUrl(findPosts.getPromoteImageUrl())
                    .fileUrl(findPosts.getFileUrl())
                    //.counts(findPosts.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(findPosts.getRecruitmentCount())
                    .endDate(findPosts.getEndDate())
                    .build();
        }
        // 해당 게시물을 스크랩하지 않았다면
        else {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .scrap(false)        // 스크랩 안한 유저
                    .applying(true)     // 지원은 했으나 (지원 중이지만),
                    .applied(false)     // 지원이 승인된 것은 아님.
                    .nickName(findPosts.getUser().getNickName())
                    .title(findPosts.getTitle())
                    .web(findPosts.getCategory().getWeb())
                    .app(findPosts.getCategory().getApp())
                    .game(findPosts.getCategory().getGame())
                    .ai(findPosts.getCategory().getAi())
                    .content(findPosts.getContent())
                    .promoteImageUrl(findPosts.getPromoteImageUrl())
                    .fileUrl(findPosts.getFileUrl())
                    //.counts(findPosts.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(findPosts.getRecruitmentCount())
                    .endDate(findPosts.getEndDate())
                    .build();
        }

        return postsDto;
    }


    // 그룹 페이지에서 승인 허가
    public Page<GroupPostsDto> approveUser(String userEmail, String nickName, Long postsId, String sortOption, Pageable pageable) {

        // 닉네임을 UserApplyPosts 테이블에 함께 두면 쿼리 날리는 것을 한 번 줄일 수 있을텐데..
        // 지원 승인하려는 유저
        User findUser = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        UserApplyPosts findUserApplyPosts = userApplyPostsRepository.findByUser_IdAndPosts_Id(findUser.getId(), postsId)
                .orElseThrow(() -> new AppException("해당하는 지원 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts findPosts = postsRepository.findById(postsId)
                .orElseThrow(() -> new AppException("해당하는 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

//        // apply() : 인원 += 1. 인원을 한 명 추가해서 정원을 넘지 않았으면, apply는 "ok"를 반환함.
//        String apply = findPosts.apply();
//
//        if ("ok".equals(apply)) {
//            // 정원을 넘지 않았으므로, 해당 유저를 승인해도 문제 없음. 따라서 해당 유저의 confirm을 true로 바꿈.
//            findUserApplyPosts.setConfirm(true);
//        }


        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applicationCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPosts);
        Integer applicationCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applicationCountOptional.isPresent()) {
            applicationCount = applicationCountOptional.get() + 1;
        } else {
            applicationCount = 1;
        }

        // recruitmentCount(모집인원)과 같지 않아야 승인 가능
        if (!findPosts.getRecruitmentCount().equals(applicationCount)) {
            // 승인 여부를 true로 변경
            findUserApplyPosts.setConfirm(true);

            // 지원 승인 시, 게시물 지원자에게 알림.
            System.out.println("====== notificationService.notify(findPosts.getTitle(), \"게시물 지원이 승인되었습니다.\"); ======");
            System.out.println("findPosts.getTitle() = " + findPosts.getTitle());

            String findWriterNickname = findPosts.getUser().getNickName(); // 게시물 작성자 닉네임
            String findPostTitle = findPosts.getTitle(); // 게시물 제목

            NotificationMessageDto notificationMessage;
            String notifyMessage;

            // notificationMessage : 실시간 알림 카드에 들어갈 내용
            // notifyMessage : Notification 배너 안에 들어갈 카드 내용

            if (PostType.PROJECT.equals(findPosts.getPostType())) {
                notificationMessage = new NotificationMessageDto("project/detail/" + postsId + ": \"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원이 승인되셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
                notifyMessage = "\"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원이 승인되셨습니다.";
            }
            else  {
                notificationMessage = new NotificationMessageDto("study/detail/" + postsId + ": \"" + findWriterNickname + "\"님이 작성한 스터디 게시물 : \"" + findPostTitle + "\"에 지원이 승인되셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
                notifyMessage = "\"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원이 승인되셨습니다.";
            }

            // 지원 승인을 하면, 게시물 지원자의 알림 배너 안에 해당 알림 내용이 들어있어야함
            // 실제 jpa를 통해 notification을 만들어 저장한다.
            Notifications applyNotification = Notifications.builder()
                    .user(findUser)
                    .postId(postsId)
                    .notificationMessage(notifyMessage)
                    .postType(findPosts.getPostType())
                    .checked(false)
                    .build();

            Notifications savedNotification = notificationsRepository.save(applyNotification);

            // 기존의 Data에 넣을 메시지에, notification id를 추가해서 보냄
            notificationMessage.setMessage(notificationMessage.getMessage() + savedNotification.getId().toString());

            // 지원을 승인하려는 유저에게 notify 발송
            notificationService.notify(findUser.getId(), notificationMessage);
            System.out.println("==========================================================");
        }

        userApplyPostsRepository.save(findUserApplyPosts);





        // 지원자가 '승인' 버튼을 눌렀을 때, 디테일 페이지에서 '승인 완료'버튼이 바로 보이고, 현재 인원 + 1이 되도록 하기 위해 동적 쿼리 생성
        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QUserApplyPosts userApplyPosts = QUserApplyPosts.userApplyPosts;

        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물 조회 쿼리 생성
                .join(posts.category, category).fetchJoin() // 게시물과 카테고리를 조인
                .leftJoin(posts.userApplyPosts, userApplyPosts).fetchJoin() // 게시물과 사용자 지원 게시물을 왼쪽 조인
                .where(posts.user.email.eq(userEmail)) // 현재 로그인한 사용자의 이메일과 일치하는 게시물만 선택
                .orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc()) // 유저 승인 시, 게시물 위치가 바뀌면 안됨. 따라서 createdDate기준으로 정렬
                .orderBy(userApplyPosts.createdDate.asc());      // 게시물에 지원한 유저를 선착순으로 보여주기

        List<Posts> filteredPosts = query
                .fetch(); // 게시물 데이터를 가져옴

        List<GroupPostsDto> groupPostsDtosList = new ArrayList<>(); // GroupPostsDto 객체를 저장할 리스트 생성

        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (Posts post : filteredPosts) {
            Category postCategory = post.getCategory();        // post를 통해 카테고리로 접근한 것을 postCategory로 명명
            User user = post.getUser();                         // post를 통해 유저 접근한 것을 user로 명명
            List<UserApplyPosts> userApplyPost = post.getUserApplyPosts()   // 게시물에 지원한 유저를 선착순으로 보여주기 위해, lastModifiedDate를 기준으로 정렬해서 가져옴
                    .stream()
                    .sorted(Comparator.comparing(UserApplyPosts::getCreatedDate))  // 승인을 하는 순간, 데이터가 변경되므로, 승인버튼에서 유저의 위치가 뒤죽박죽 된다. 따라서 getLastModifiedDate가 아닌 getCreatedDate를 사용한다.
                    .collect(Collectors.toList());

            // applyNickNames라는 List 컬렉션에 게시물에 지원한 닉네임을 모두 담아 리턴한다.
            List<String> applyNickNames = userApplyPost.stream()
                    .map(userApply -> userApply.getUser().getNickName())  // 지원한 각각의 유저의 닉네임을 찾는다.
                    .collect(Collectors.toList());

            List<Boolean> approvedList = userApplyPost.stream()
                    .map(userApply -> userApply.getConfirm()) // 지원한 각각의 유저의 승인 여부를 찾는다.
                    .collect(Collectors.toList());

            GroupPostsDto groupPostsDto;

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);
            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            // 정원이 다 찬 경우, isFull을 true로 담아서 내보낸다.
            //if (post.getCounts().equals(post.getRecruitmentCount())) {
            if (applyCount.equals(post.getRecruitmentCount())) {
                groupPostsDto = GroupPostsDto.builder()
                        .id(post.getId())
                        .writerNickName(user.getNickName())   // user = posts.getUser()
                        .applyNickNames(applyNickNames)         // applyNickNames : 지원한 사람들을 모은 리스트
                        .postType(post.getPostType().toString())    // postType은 Enum 타입이므로, toString() 해주기
                        .title(post.getTitle())
                        .web(postCategory.getWeb())     // category = posts.getCategory()
                        .app(postCategory.getApp())
                        .game(postCategory.getGame())
                        .ai(postCategory.getAi())
                        //.counts(post.getCounts())
                        .counts(applyCount)
                        .recruitmentCount(post.getRecruitmentCount())
                        .endDate(post.getEndDate())
                        .approved(approvedList)         // approvedList : 사람들의 승인 허가 여부를 담은 리스트
                        .isApproved(null)               // writer 본인은 승인 허가 여부와 상관 없으므로, null로 세팅
                        .isFull(true)               // 프론트 측에 DTO를 반환했을 때, 정원이 다 찼는지의 여부를 알려주어야, 이에 대한 예외 처리가 가능하다. 현재, 정원이 다 찼으므로 true를 세팅.
                        .build();
            }
            else {
                groupPostsDto = GroupPostsDto.builder()
                        .id(post.getId())
                        .writerNickName(user.getNickName())   // user = posts.getUser()
                        .applyNickNames(applyNickNames)         // applyNickNames : 지원한 사람들을 모은 리스트
                        .postType(post.getPostType().toString())    // postType은 Enum 타입이므로, toString() 해주기
                        .title(post.getTitle())
                        .web(postCategory.getWeb())     // category = posts.getCategory()
                        .app(postCategory.getApp())
                        .game(postCategory.getGame())
                        .ai(postCategory.getAi())
                        //.counts(post.getCounts())
                        .counts(applyCount)
                        .recruitmentCount(post.getRecruitmentCount())
                        .endDate(post.getEndDate())
                        .approved(approvedList)         // approvedList : 사람들의 승인 허가 여부를 담은 리스트
                        .isApproved(null)               // writer 본인은 승인 허가 여부와 상관 없으므로, null로 세팅
                        .isFull(false)              // 프론트 측에 DTO를 반환했을 때, 정원이 다 찼는지의 여부를 알려주어야, 이에 대한 예외 처리가 가능하다. 현재, 정원이 안찼으므로 false를 세팅.
                        .build();
            }

            groupPostsDtosList.add(groupPostsDto);     // 컬렉션에 추가
        }

        // 변환한 데이터의 총 개수 계산
        long total = groupPostsDtosList.size();

        // 페이지네이션을 위한 offset과 pageSize 계산, 예외 상황 처리
        int offset = (int) pageable.getOffset();    // 현재 페이지 정보 가져오기
        int pageSize = pageable.getPageSize();      // 현재 페이지에 몇 개를 띄울건지 size 정보 가져오기

        // offset이 데이터의 총 개수를 초과하면 더 이상 데이터를 가져올 필요 없음
        if (offset >= total) {
            offset = 0;
            pageSize = 0;
        }
        // 현재 페이지 + 현재 페이지에서 보고있는 게시물의 개수 > 총 게시물의 개수라면, 페이지 사이즈 재조정
        else if (offset + pageSize > total) {
            pageSize = (int) (total - offset);
        }

        // 페이지 객체 생성하여 반환
        // subList : 시작 위치부터 시작 위치 + 페이지 크기 범위의 데이터만 추출
        // Pageable : 현재 페이지 번호, 페이지 크기 등이 포함되어 있어, 이를 기반으로 데이터를 추출하고 페이지 정보를 생성
        // total : 전체 데이터의 개수를 나타냄
        return new PageImpl<>(groupPostsDtosList.subList(offset, offset + pageSize), pageable, total);
    }


    // 디테일 페이지에서 직접 승인 허가
    public List<ApplicantDto> approveUserInDetail(String userEmail, String nickName, Long postsId) {

        // 지원 승인하려는 유저
        User findUser = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        UserApplyPosts findUserApplyPosts = userApplyPostsRepository.findByUser_IdAndPosts_Id(findUser.getId(), postsId)
                .orElseThrow(() -> new AppException("해당하는 지원 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts findPosts = postsRepository.findById(postsId)
                .orElseThrow(() -> new AppException("해당하는 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applicationCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPosts);
        Integer applicationCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applicationCountOptional.isPresent()) {
            applicationCount = applicationCountOptional.get() + 1;
        } else {
            applicationCount = 1;
        }

        // recruitmentCount(모집인원)과 같지 않아야 승인 가능
        if (!findPosts.getRecruitmentCount().equals(applicationCount)) {
            // 승인 여부를 true로 변경
            findUserApplyPosts.setConfirm(true);

            // 지원 승인 시, 게시물 지원자에게 알림.
            System.out.println("====== notificationService.notify(findPosts.getTitle(), \"게시물 지원이 승인되었습니다.\"); ======");
            System.out.println("findPosts.getTitle() = " + findPosts.getTitle());

            String findWriterNickname = findPosts.getUser().getNickName(); // 게시물 작성자 닉네임
            String findPostTitle = findPosts.getTitle(); // 게시물 제목

            NotificationMessageDto notificationMessage;
            String notifyMessage;

            // notificationMessage : 실시간 알림 카드에 들어갈 내용
            // notifyMessage : Notification 배너 안에 들어갈 카드 내용

            if (PostType.PROJECT.equals(findPosts.getPostType())) {
                notificationMessage = new NotificationMessageDto("project/detail/" + postsId + ": \"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원이 승인되셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
                notifyMessage = "\"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원이 승인되셨습니다.";
            }
            else  {
                notificationMessage = new NotificationMessageDto("study/detail/" + postsId + ": \"" + findWriterNickname + "\"님이 작성한 스터디 게시물 : \"" + findPostTitle + "\"에 지원이 승인되셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
                notifyMessage = "\"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원이 승인되셨습니다.";
            }

            // 지원 승인을 하면, 게시물 지원자의 알림 배너 안에 해당 알림 내용이 들어있어야함
            // 실제 jpa를 통해 notification을 만들어 저장한다.
            Notifications applyNotification = Notifications.builder()
                    .user(findUser)
                    .postId(postsId)
                    .notificationMessage(notifyMessage)
                    .postType(findPosts.getPostType())
                    .checked(false)
                    .build();

            Notifications savedNotification = notificationsRepository.save(applyNotification);

            // 기존의 Data에 넣을 메시지에, notification id를 추가해서 보냄
            notificationMessage.setMessage(notificationMessage.getMessage() + savedNotification.getId().toString());

            // 지원을 승인하려는 유저에게 notify 발송
            notificationService.notify(findUser.getId(), notificationMessage);
            System.out.println("==========================================================");
        }

        userApplyPostsRepository.save(findUserApplyPosts);


        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴. (그 중 승인 받은 애들의 개수만 가져옴)
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPosts);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }


        // List<ApplicantDto> 객체 생성
        List<ApplicantDto> applicantDtoList = new ArrayList<>();

        // 검색된 모든 지원자 정보를 createdDate를 기준으로 오름차순으로 가져와서 ApplicantDto로 변환하여 리스트에 추가
        List<UserApplyPosts> userApplyPostsList = userApplyPostsRepository.findByPosts_IdOrderByCreatedDateAsc(postsId);

        for (UserApplyPosts userApplyPosts : userApplyPostsList) {
            User applicantUser = userApplyPosts.getUser();
            ApplicantDto applicantDto = ApplicantDto.builder()
                    .nickName(applicantUser.getNickName()) // 지원자의 닉네임
                    .confirm(userApplyPosts.getConfirm()) // 승인 여부
                    .count(applyCount)                      // 현재 지원자 수
                    .build();
            applicantDtoList.add(applicantDto);
        }

        return applicantDtoList;
    }


    // 지원 취소
    public PostsDto cancelApply(String userEmail, Long postsId, String action) {

        // Email로 유저 찾기
        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 유저 ID와 project ID로 지원한 게시물 찾기
        UserApplyPosts findUserApplyPosts = userApplyPostsRepository.findByUser_IdAndPosts_Id(findUser.getId(), postsId)
                .orElseThrow(() -> new AppException("지원한 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 프로젝트 ID로 해당 프로젝트 찾기
        Posts findPosts = postsRepository.findById(postsId)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 해당 유저가 이 게시물에 스크랩 했는지의 여부를 알기 위해 사용
        Optional<ScrapPosts> scrapPosts = scrapPostsRepository.findByUser_IdAndPosts_Id(findUser.getId(), postsId);

        // 승인한 사람이 지원 취소를 했다면, 인원 -= 1을 해야 함.
//        if ("approved".equals(action)) {
//            // 현재 counts 값이 2 이상일 때, counts -= 1을 진행함.
//            // 게시물에 최소 1명(본인)은 지원했으므로, counts의 최소 값은 1이고, 2 이상일 때 1을 빼줄 수 있다.
//            findPosts.cancel();
//            postsRepository.save(findPosts);    // 1 뺀 값을 디비에 저장해주기.
//        }

        // 지원한 게시물 삭제
        userApplyPostsRepository.delete(findUserApplyPosts);


        // 게시물 지원 취소 시, 게시물 작성자에게 알림.
        System.out.println("====== notificationService.notify(findPosts.getUser().getNickName(), \"게시물에 지원을 취소한 사용자가 있습니다.\"); ======");
        System.out.println("findPosts.getUser().getId() = " + findPosts.getUser().getId());

        String findUserNickname= findUser.getNickName(); // 지원 취소를 하고자 하는 사람 닉네임
        String findPostsTitle = findPosts.getTitle(); // 지원 취소를 할 게시물 제목
        User findPostUser = findPosts.getUser(); // 게시물 작성자

        NotificationMessageDto notificationMessage;
        String notifyMessage;

        // notificationMessage : 실시간 알림 카드에 들어갈 내용
        // notifyMessage : Notification 배너 안에 들어갈 카드 내용

        if (PostType.PROJECT.equals(findPosts.getPostType())) {
            // notificationMessage에 들어갈 내용을 그냥 String으로 적으면, 한글은 알아듣지 못해 ???로 나온다.
            // 이를 해결하기 위해 NotificationMessageDto를 만들고, 이 안에 값을 생성하여 반환하도록 함으로써, JSON으로 반환하여 한글을 인식하도록 한다.
            notificationMessage = new NotificationMessageDto("project/detail/" + postsId + ": 프로젝트 게시물 : \"" + findPostsTitle + "\"에 \"" + findUserNickname + "\"님이 지원을 취소하셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
            notifyMessage = "프로젝트 게시물 : \"" + findPostsTitle + "\"에 \"" + findUserNickname + "\"님이 지원을 취소하셨습니다.";
        }
        else  {
            notificationMessage = new NotificationMessageDto("study/detail/" + postsId + ": 스터디 게시물 : \"" + findPostsTitle + "\"에 \"" + findUserNickname + "\"님이 지원을 취소하셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
            notifyMessage = "스터디 게시물 : \"" + findPostsTitle + "\"에 \"" + findUserNickname + "\"님이 지원을 취소하셨습니다.";
        }

        // 지원을 취소하면, 게시물 작성자의 알림 배너 안에 해당 알림 내용이 들어있어야함
        // 실제 jpa를 통해 notification을 만들어 저장한다.
        Notifications applyNotification = Notifications.builder()
                .user(findPostUser)
                .postId(postsId)
                .notificationMessage(notifyMessage)
                .postType(findPosts.getPostType())
                .checked(false)
                .build();

        Notifications savedNotification = notificationsRepository.save(applyNotification);

        // 기존의 Data에 넣을 메시지에, notification id를 추가해서 보냄
        notificationMessage.setMessage(notificationMessage.getMessage() + savedNotification.getId().toString());

        notificationService.notify(findPosts.getUser().getId(), notificationMessage); // 게시물 작성자에게 지원 취소 실시간 알림 보냄
        System.out.println("==========================================================");


        PostsDto postsDto;

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPosts);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // 삭제된 결과에 맞게 화면을 보여주기 위해, DTO 다시 만들기.
        // 이제 해당 유저는 게시물에 지원 안 한 사람과 동일하게 되었음.
        // 해당 게시물을 스크랩했다면
        if (scrapPosts.isPresent()) {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .scrap(true)        // 스크랩 한 유저
                    .applying(false)    // 지원 중이지도 않고
                    .applied(false)     // 지원 승인되지도 않았음
                    .nickName(findPosts.getUser().getNickName())
                    .title(findPosts.getTitle())
                    .web(findPosts.getCategory().getWeb())
                    .app(findPosts.getCategory().getApp())
                    .game(findPosts.getCategory().getGame())
                    .ai(findPosts.getCategory().getAi())
                    .content(findPosts.getContent())
                    .promoteImageUrl(findPosts.getPromoteImageUrl())
                    .fileUrl(findPosts.getFileUrl())
                    //.counts(findPosts.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(findPosts.getRecruitmentCount())
                    .endDate(findPosts.getEndDate())
                    .build();
        }
        // 해당 게시물을 스크랩하지 않았다면
        else {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .scrap(false)        // 스크랩 안한 유저
                    .applying(false)    // 지원 중이지도 않고
                    .applied(false)     // 지원 승인되지도 않았음
                    .nickName(findPosts.getUser().getNickName())
                    .title(findPosts.getTitle())
                    .web(findPosts.getCategory().getWeb())
                    .app(findPosts.getCategory().getApp())
                    .game(findPosts.getCategory().getGame())
                    .ai(findPosts.getCategory().getAi())
                    .content(findPosts.getContent())
                    .promoteImageUrl(findPosts.getPromoteImageUrl())
                    .fileUrl(findPosts.getFileUrl())
                    //.counts(findPosts.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(findPosts.getRecruitmentCount())
                    .endDate(findPosts.getEndDate())
                    .build();
        }


        return postsDto;
    }



    // 그룹 페이지에서 승인 허가 취소
    public Page<GroupPostsDto> cancelApproveUser(String userEmail, String nickName, Long postsId, String sortOption, Pageable pageable) {

        // 닉네임을 UserApplyPosts에 함께 두면 쿼리 날리는 것을 한 번 줄일 수 있을텐데..
        User findUser = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        UserApplyPosts findUserApplyPosts = userApplyPostsRepository.findByUser_IdAndPosts_Id(findUser.getId(), postsId)
                .orElseThrow(() -> new AppException("해당하는 지원 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts findPosts = postsRepository.findById(postsId)
                .orElseThrow(() -> new AppException("해당하는 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 승인 허가를 취소하므로, 지원할 수 있는 빈 자리가 하나 늘었다.
        // 따라서 인원 += 1
//        String cancel = findPosts.cancel();
//
//        if ("ok".equals(cancel)) {
//            // 승인 허가가 취소되면, 해당 유저의 승인 허가 여부를 false로 바꾼다.
//            findUserApplyPosts.setConfirm(false);
//        }

        // 승인 여부를 false로 변경
        findUserApplyPosts.setConfirm(false);

        userApplyPostsRepository.save(findUserApplyPosts);


        // 지원 승인 허가 취소 시, 게시물 지원자에게 알림.
        System.out.println("====== notificationService.notify(findPosts.getTitle(), \"게시물 지원 승인이 취소되었습니다.\"); ======");
        System.out.println("findPosts.getTitle() = " + findPosts.getTitle());

        String findWriterNickname = findPosts.getUser().getNickName(); // 게시물 작성자 닉네임
        String findPostTitle = findPosts.getTitle(); // 승인 취소를 할 게시물

        NotificationMessageDto notificationMessage;
        String notifyMessage;


        // notificationMessage : 실시간 알림 카드에 들어갈 내용
        // notifyMessage : Notification 배너 안에 들어갈 카드 내용

        if (PostType.PROJECT.equals(findPosts.getPostType())) {
            notificationMessage = new NotificationMessageDto("project/detail/" + postsId + ": \"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원 승인이 취소되셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
            notifyMessage = "\"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원 승인이 취소되셨습니다.";
        }
        else  {
            notificationMessage = new NotificationMessageDto("study/detail/" + postsId + ": \"" + findWriterNickname + "\"님이 작성한 스터디 게시물 : \"" + findPostTitle + "\"에 지원 승인이 취소되셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
            notifyMessage = "\"" + findWriterNickname + "\"님이 작성한 스터디 게시물 : \"" + findPostTitle + "\"에 지원 승인이 취소되셨습니다.";
        }

        // 승인 허가를 취소하면, 게시물 지원자의 알림 배너 안에 해당 알림 내용이 들어있어야함
        // 실제 jpa를 통해 notification을 만들어 저장한다.
        Notifications applyNotification = Notifications.builder()
                .user(findUser)
                .postId(postsId)
                .notificationMessage(notifyMessage)
                .postType(findPosts.getPostType())
                .checked(false)
                .build();

        Notifications savedNotification = notificationsRepository.save(applyNotification);

        // 기존의 Data에 넣을 메시지에, notification id를 추가해서 보냄
        notificationMessage.setMessage(notificationMessage.getMessage() + savedNotification.getId().toString());

        // 지원을 승인하려는 유저에게 notify 발송
        notificationService.notify(findUser.getId(), notificationMessage);
        System.out.println("==========================================================");


        // '승인' 버튼을 눌렀을 때, '승인 완료'버튼이 바로 보이고, 현재 인원 + 1이 되도록 하기 위해 동적 쿼리 생성
        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QUserApplyPosts userApplyPosts = QUserApplyPosts.userApplyPosts;

        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물 조회 쿼리 생성
                .join(posts.category, category).fetchJoin() // 게시물과 카테고리를 조인
                .leftJoin(posts.userApplyPosts, userApplyPosts).fetchJoin() // 게시물과 사용자 지원 게시물을 왼쪽 조인
                .where(posts.user.email.eq(userEmail)) // 현재 로그인한 사용자의 이메일과 일치하는 게시물만 선택
                .orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc()) // 유저 승인 취소 시, 게시물 위치가 바뀌면 안됨. 따라서 createdDate기준으로 정렬
                .orderBy(userApplyPosts.createdDate.asc());      // 게시물에 지원한 유저를 선착순으로 보여주기

        List<Posts> filteredPosts = query
                .fetch(); // 게시물 데이터를 가져옴

        List<GroupPostsDto> groupPostsDtosList = new ArrayList<>(); // GroupPostsDto 객체를 저장할 리스트 생성

        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (Posts post : filteredPosts) {
            Category postCategory = post.getCategory();        // post를 통해 카테고리로 접근한 것을 postCategory로 명명
            User user = post.getUser();                         // post를 통해 유저 접근한 것을 user로 명명

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);
            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            List<UserApplyPosts> userApplyPost = post.getUserApplyPosts()   // 게시물에 지원한 유저를 선착순으로 보여주기 위해, lastModifiedDate를 기준으로 정렬해서 가져옴
                    .stream()
                    .sorted(Comparator.comparing(UserApplyPosts::getCreatedDate))   // 승인을 취소하는 순간, 데이터가 변경되므로, 승인버튼에서 유저의 위치가 뒤죽박죽 된다. 따라서 getLastModifiedDate가 아닌 getCreatedDate를 사용한다.
                    .collect(Collectors.toList());

            // applyNickNames라는 List 컬렉션에 게시물에 지원한 닉네임을 모두 담아 리턴한다.
            List<String> applyNickNames = userApplyPost.stream()
                    .map(userApply -> userApply.getUser().getNickName())  // 지원한 각각의 유저의 닉네임을 찾는다.
                    .collect(Collectors.toList());

            List<Boolean> approvedList = userApplyPost.stream()
                    .map(userApply -> userApply.getConfirm()) // 지원한 각각의 유저의 승인 허가 여부를 찾는다.
                    .collect(Collectors.toList());

            GroupPostsDto groupPostsDto = GroupPostsDto.builder()
                    .id(post.getId())
                    .writerNickName(user.getNickName())   // user = posts.getUser()
                    .applyNickNames(applyNickNames)         // applyNickNames : 지원한 사람들을 모은 리스트
                    .postType(post.getPostType().toString())    // postType은 Enum 타입이므로, toString() 해주기
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    //.counts(post.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    .approved(approvedList)         // approvedList : 사람들의 승인 허가 여부를 담은 리스트
                    .isApproved(null)       // 내가 게시물 작성자이므로, 나의 승인 허가 여부는 알 필요가 없다. 따라서 null로 반환.
                    .isFull(null)           // 승인 허가 취소하면, 인원이 줄게 되므로, Full과는 무관하다. 따라서 null로 반환.
                    .build();

            groupPostsDtosList.add(groupPostsDto);     // 컬렉션에 추가
        }

        // 변환한 데이터의 총 개수 계산
        long total = groupPostsDtosList.size();

        // 페이지네이션을 위한 offset과 pageSize 계산, 예외 상황 처리
        int offset = (int) pageable.getOffset();    // 현재 페이지 정보 가져오기
        int pageSize = pageable.getPageSize();      // 현재 페이지에 몇 개를 띄울건지 size 정보 가져오기

        // offset이 데이터의 총 개수를 초과하면 더 이상 데이터를 가져올 필요 없음
        if (offset >= total) {
            offset = 0;
            pageSize = 0;
        }
        // 현재 페이지 + 현재 페이지에서 보고있는 게시물의 개수 > 총 게시물의 개수라면, 페이지 사이즈 재조정
        else if (offset + pageSize > total) {
            pageSize = (int) (total - offset);
        }

        // 페이지 객체 생성하여 반환
        // subList : 시작 위치부터 시작 위치 + 페이지 크기 범위의 데이터만 추출
        // Pageable : 현재 페이지 번호, 페이지 크기 등이 포함되어 있어, 이를 기반으로 데이터를 추출하고 페이지 정보를 생성
        // total : 전체 데이터의 개수를 나타냄
        return new PageImpl<>(groupPostsDtosList.subList(offset, offset + pageSize), pageable, total);
    }

    // 디테일 페이지에서 직접 승인 허가 취소
    public List<ApplicantDto> cancelApproveUserInDetail(String userEmail, String nickName, Long postsId) {

        // 닉네임을 UserApplyPosts에 함께 두면 쿼리 날리는 것을 한 번 줄일 수 있을텐데..
        User findUser = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        UserApplyPosts findUserApplyPosts = userApplyPostsRepository.findByUser_IdAndPosts_Id(findUser.getId(), postsId)
                .orElseThrow(() -> new AppException("해당하는 지원 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts findPosts = postsRepository.findById(postsId)
                .orElseThrow(() -> new AppException("해당하는 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));


        // 승인 여부를 false로 변경
        findUserApplyPosts.setConfirm(false);

        userApplyPostsRepository.save(findUserApplyPosts);


        // 지원 승인 허가 취소 시, 게시물 지원자에게 알림.
        System.out.println("====== notificationService.notify(findPosts.getTitle(), \"게시물 지원 승인이 취소되었습니다.\"); ======");
        System.out.println("findPosts.getTitle() = " + findPosts.getTitle());

        String findWriterNickname = findPosts.getUser().getNickName(); // 게시물 작성자 닉네임
        String findPostTitle = findPosts.getTitle(); // 승인 취소를 할 게시물

        NotificationMessageDto notificationMessage;
        String notifyMessage;


        // notificationMessage : 실시간 알림 카드에 들어갈 내용
        // notifyMessage : Notification 배너 안에 들어갈 카드 내용

        if (PostType.PROJECT.equals(findPosts.getPostType())) {
            notificationMessage = new NotificationMessageDto("project/detail/" + postsId + ": \"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원 승인이 취소되셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
            notifyMessage = "\"" + findWriterNickname + "\"님이 작성한 프로젝트 게시물 : \"" + findPostTitle + "\"에 지원 승인이 취소되셨습니다.";
        }
        else  {
            notificationMessage = new NotificationMessageDto("study/detail/" + postsId + ": \"" + findWriterNickname + "\"님이 작성한 스터디 게시물 : \"" + findPostTitle + "\"에 지원 승인이 취소되셨습니다."); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
            notifyMessage = "\"" + findWriterNickname + "\"님이 작성한 스터디 게시물 : \"" + findPostTitle + "\"에 지원 승인이 취소되셨습니다.";
        }

        // 승인 허가를 취소하면, 게시물 지원자의 알림 배너 안에 해당 알림 내용이 들어있어야함
        // 실제 jpa를 통해 notification을 만들어 저장한다.
        Notifications applyNotification = Notifications.builder()
                .user(findUser)
                .postId(postsId)
                .notificationMessage(notifyMessage)
                .postType(findPosts.getPostType())
                .checked(false)
                .build();

        Notifications savedNotification = notificationsRepository.save(applyNotification);

        // 기존의 Data에 넣을 메시지에, notification id를 추가해서 보냄
        notificationMessage.setMessage(notificationMessage.getMessage() + savedNotification.getId().toString());

        // 지원을 승인하려는 유저에게 notify 발송
        notificationService.notify(findUser.getId(), notificationMessage);
        System.out.println("==========================================================");



        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴. (그 중 승인 받은 애들의 개수만 가져옴)
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPosts);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }


        // List<ApplicantDto> 객체 생성
        List<ApplicantDto> applicantDtoList = new ArrayList<>();

        // 검색된 모든 지원자 정보를 createdDate를 기준으로 오름차순으로 가져와서 ApplicantDto로 변환하여 리스트에 추가
        List<UserApplyPosts> userApplyPostsList = userApplyPostsRepository.findByPosts_IdOrderByCreatedDateAsc(postsId);

        for (UserApplyPosts userApplyPosts : userApplyPostsList) {
            User applicantUser = userApplyPosts.getUser();
            ApplicantDto applicantDto = ApplicantDto.builder()
                    .nickName(applicantUser.getNickName()) // 지원자의 닉네임
                    .confirm(userApplyPosts.getConfirm()) // 승인 여부
                    .count(applyCount)                      // 현재 지원자 수
                    .build();
            applicantDtoList.add(applicantDto);
        }

        return applicantDtoList;
    }



    // 게시물 스크랩
    public PostsDto postsScrap(String userEmail, Long postsId) {

        // email로 현재 유저 찾기
        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // postsId로 게시물 찾기
        Posts findPosts = postsRepository.findById(postsId)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        ScrapPosts scrapPosts = ScrapPosts.builder()
                .user(findUser)
                .posts(findPosts)
                .build();

        ScrapPosts savedScrapPosts = scrapPostsRepository.save(scrapPosts);

        // 해당 게시물을 방금 스크랩했으므로 다음과 같은 DTO 반환
        boolean hasApplied = false;     // 지원 여부
        boolean isConfirmed = false;    // 승인 여부

        for (UserApplyPosts apply : findUser.getUserApplyPosts()) {
            // UserApplyPosts에서 현재 내가 보고 있는 게시물의 postsId와 일치하는 게시물을 찾았다면,
            if (apply.getPosts().getId().equals(postsId)) {
                hasApplied = true;      // 지원 여부는 true
                isConfirmed = apply.getConfirm();   // 승인 여부는 직접 가져오기
                break; // Exit the loop since we found a matching entry
            }
        }

        PostsDto postsDto;

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPosts);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // 게시물 지원 안한 사람
        if (!hasApplied) {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .scrap(true)        // 방금 게시물을 스크랩 했으므로, 이제 이 유저는 스크랩 한 유저
                    .applying(false)    // 지원 중이지도 않고
                    .applied(false)     // 지원 승인되지도 않았음
                    .nickName(savedScrapPosts.getPosts().getUser().getNickName())   // 게시물 작성자 닉네임. 주의 : savedScrapPosts.getUser().getNickName()하면 지원한 사람의 닉네임이 나옴!!
                    .title(savedScrapPosts.getPosts().getTitle())
                    .web(savedScrapPosts.getPosts().getCategory().getWeb())
                    .app(savedScrapPosts.getPosts().getCategory().getApp())
                    .game(savedScrapPosts.getPosts().getCategory().getGame())
                    .ai(savedScrapPosts.getPosts().getCategory().getAi())
                    .content(savedScrapPosts.getPosts().getContent())
                    .promoteImageUrl(savedScrapPosts.getPosts().getPromoteImageUrl())
                    .fileUrl(savedScrapPosts.getPosts().getFileUrl())
                    //.counts(savedScrapPosts.getPosts().getCounts())
                    .counts(applyCount)
                    .recruitmentCount(savedScrapPosts.getPosts().getRecruitmentCount())
                    .endDate(savedScrapPosts.getPosts().getEndDate())
                    .build();
        }
        // 게시물 지원 한 사람
        else {
            // 근데 승인 아직 안난 사람
            if (!isConfirmed) {
                postsDto = PostsDto.builder()
                        .writer(false)      // writer에 false를 리턴
                        .scrap(true)        // 방금 게시물을 스크랩 했으므로, 이제 이 유저는 스크랩 한 유저
                        .applying(true)     // 지원은 했으나 (지원 중이지만),
                        .applied(false)     // 지원이 승인된 것은 아님.
                        .nickName(savedScrapPosts.getPosts().getUser().getNickName())   // 게시물 작성자 닉네임. 주의 : savedScrapPosts.getUser().getNickName()하면 지원한 사람의 닉네임이 나옴!!
                        .title(savedScrapPosts.getPosts().getTitle())
                        .web(savedScrapPosts.getPosts().getCategory().getWeb())
                        .app(savedScrapPosts.getPosts().getCategory().getApp())
                        .game(savedScrapPosts.getPosts().getCategory().getGame())
                        .ai(savedScrapPosts.getPosts().getCategory().getAi())
                        .content(savedScrapPosts.getPosts().getContent())
                        .promoteImageUrl(savedScrapPosts.getPosts().getPromoteImageUrl())
                        .fileUrl(savedScrapPosts.getPosts().getFileUrl())
                        //.counts(savedScrapPosts.getPosts().getCounts())
                        .counts(applyCount)
                        .recruitmentCount(savedScrapPosts.getPosts().getRecruitmentCount())
                        .endDate(savedScrapPosts.getPosts().getEndDate())
                        .build();
            }
            // 승인 난 사람
            else {
                postsDto = PostsDto.builder()
                        .writer(false)      // writer에 false를 리턴
                        .scrap(true)        // 방금 게시물을 스크랩 했으므로, 이제 이 유저는 스크랩 한 유저
                        .applying(false)    // 지원 중은 아니고,
                        .applied(true)     // 지원이 승인되었음.
                        .nickName(savedScrapPosts.getPosts().getUser().getNickName())   // 게시물 작성자 닉네임. 주의 : savedScrapPosts.getUser().getNickName()하면 지원한 사람의 닉네임이 나옴!!
                        .title(savedScrapPosts.getPosts().getTitle())
                        .web(savedScrapPosts.getPosts().getCategory().getWeb())
                        .app(savedScrapPosts.getPosts().getCategory().getApp())
                        .game(savedScrapPosts.getPosts().getCategory().getGame())
                        .ai(savedScrapPosts.getPosts().getCategory().getAi())
                        .content(savedScrapPosts.getPosts().getContent())
                        .promoteImageUrl(savedScrapPosts.getPosts().getPromoteImageUrl())
                        .fileUrl(savedScrapPosts.getPosts().getFileUrl())
                        //.counts(savedScrapPosts.getPosts().getCounts())
                        .counts(applyCount)
                        .recruitmentCount(savedScrapPosts.getPosts().getRecruitmentCount())
                        .endDate(savedScrapPosts.getPosts().getEndDate())
                        .build();
            }
        }

        return postsDto;
    }

    public PostsDto cancelPostsScrap(String userEmail, Long postsId) {

        // Email로 유저 찾기
        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 프로젝트 ID로 해당 프로젝트 찾기
        Posts findPosts = postsRepository.findById(postsId)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 스크랩한 레포지토리 찾기
        ScrapPosts findScrapPosts = scrapPostsRepository.findByUser_IdAndPosts_Id(findUser.getId(), postsId)
                .orElseThrow(() -> new AppException("스크랩한 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        scrapPostsRepository.delete(findScrapPosts);


        boolean hasApplied = false;     // 지원 여부
        boolean isConfirmed = false;    // 승인 여부

        for (UserApplyPosts apply : findUser.getUserApplyPosts()) {
            // UserApplyPosts에서 현재 내가 보고 있는 게시물의 postsId와 일치하는 게시물을 찾았다면,
            if (apply.getPosts().getId().equals(postsId)) {
                hasApplied = true;      // 지원 여부는 true
                isConfirmed = apply.getConfirm();   // 승인 여부는 직접 가져오기
                break; // Exit the loop since we found a matching entry
            }
        }

        PostsDto postsDto;

        // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
        Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(findPosts);
        Integer applyCount;

        // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
        // null이라면, 인원 = 1(본인)
        if (applyCountOptional.isPresent()) {
            applyCount = applyCountOptional.get() + 1;
        } else {
            applyCount = 1;
        }

        // 해당 게시물을 방금 스크랩 취소했으므로 다음과 같은 DTO 반환 (.scrap(false))
        // 게시물 지원 안한 사람
        if (!hasApplied) {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .scrap(false)       // 방금 게시물을 스크랩 취소했으므로, 이제 이 유저는 스크랩 안한 유저
                    .applying(false)    // 지원 중이지도 않고
                    .applied(false)     // 지원 승인되지도 않았음
                    .nickName(findPosts.getUser().getNickName())   // 게시물 작성자 닉네임. 주의 : savedScrapPosts.getUser().getNickName()하면 지원한 사람의 닉네임이 나옴!!
                    .title(findPosts.getTitle())
                    .web(findPosts.getCategory().getWeb())
                    .app(findPosts.getCategory().getApp())
                    .game(findPosts.getCategory().getGame())
                    .ai(findPosts.getCategory().getAi())
                    .content(findPosts.getContent())
                    .promoteImageUrl(findPosts.getPromoteImageUrl())
                    .fileUrl(findPosts.getFileUrl())
                    //.counts(findPosts.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(findPosts.getRecruitmentCount())
                    .endDate(findPosts.getEndDate())
                    .build();
        }
        // 게시물 지원 한 사람
        else {
            // 근데 승인 아직 안난 사람
            if (!isConfirmed) {
                postsDto = PostsDto.builder()
                        .writer(false)      // writer에 false를 리턴
                        .scrap(false)       // 방금 게시물을 스크랩 취소했으므로, 이제 이 유저는 스크랩 안한 유저
                        .applying(true)     // 지원은 했으나 (지원 중이지만),
                        .applied(false)     // 지원이 승인된 것은 아님.
                        .nickName(findPosts.getUser().getNickName())   // 게시물 작성자 닉네임. 주의 : savedScrapPosts.getUser().getNickName()하면 지원한 사람의 닉네임이 나옴!!
                        .title(findPosts.getTitle())
                        .web(findPosts.getCategory().getWeb())
                        .app(findPosts.getCategory().getApp())
                        .game(findPosts.getCategory().getGame())
                        .ai(findPosts.getCategory().getAi())
                        .content(findPosts.getContent())
                        .promoteImageUrl(findPosts.getPromoteImageUrl())
                        .fileUrl(findPosts.getFileUrl())
                        //.counts(findPosts.getCounts())
                        .counts(applyCount)
                        .recruitmentCount(findPosts.getRecruitmentCount())
                        .endDate(findPosts.getEndDate())
                        .build();
            }
            // 승인 난 사람
            else {
                postsDto = PostsDto.builder()
                        .writer(false)      // writer에 false를 리턴
                        .scrap(false)       // 방금 게시물을 스크랩 취소했으므로, 이제 이 유저는 스크랩 안한 유저
                        .applying(false)    // 지원 중은 아니고,
                        .applied(true)     // 지원이 승인되었음.
                        .nickName(findPosts.getUser().getNickName())   // 게시물 작성자 닉네임. 주의 : savedScrapPosts.getUser().getNickName()하면 지원한 사람의 닉네임이 나옴!!
                        .title(findPosts.getTitle())
                        .web(findPosts.getCategory().getWeb())
                        .app(findPosts.getCategory().getApp())
                        .game(findPosts.getCategory().getGame())
                        .ai(findPosts.getCategory().getAi())
                        .content(findPosts.getContent())
                        .promoteImageUrl(findPosts.getPromoteImageUrl())
                        .fileUrl(findPosts.getFileUrl())
                        //.counts(findPosts.getCounts())
                        .counts(applyCount)
                        .recruitmentCount(findPosts.getRecruitmentCount())
                        .endDate(findPosts.getEndDate())
                        .build();
            }
        }

        return postsDto;
    }



    // 스크랩한 프로젝트 게시물 가져오기
    @Transactional(readOnly = true)
    public Page<ScrapPostsDto> getProjectScrapPosts(String userEmail, String sortOption, Pageable pageable) {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QScrapPosts scrapPosts = QScrapPosts.scrapPosts;
        QUserApplyPosts userApplyPosts = QUserApplyPosts.userApplyPosts;

        // '데이터'를 가져오는 쿼리
        JPAQuery<Tuple> query = queryFactory    // queryDsl에서 제공하는 Tuple을 import하기!
                .select(scrapPosts, userApplyPosts) // 스크랩한 게시물과 지원한 게시물을 함께 선택
                .from(scrapPosts)
                .join(scrapPosts.posts, posts).fetchJoin() // 스크랩한 게시물의 Posts 엔티티와 조인
                .join(posts.category, category).fetchJoin() // Posts 엔티티의 Category 엔티티와 조인
                .leftJoin(userApplyPosts) // 지원한 게시물과 조인 (left join으로 변경하여 모든 스크랩 게시물을 가져옴)
                .on(userApplyPosts.user.id.eq(scrapPosts.user.id) // 조인 조건: 스크랩 게시물의 사용자 ID와 지원 게시물의 사용자 ID가 일치해야 함
                        .and(userApplyPosts.posts.id.eq(scrapPosts.posts.id))) // 조인 조건: 스크랩 게시물의 게시물 ID와 지원 게시물의 게시물 ID가 일치해야 함
                .where(scrapPosts.user.email.eq(userEmail)) // 스크랩한 사용자 이메일이 일치해야 함
                .where(posts.postType.eq(PostType.PROJECT)) // 프로젝트 게시물이어야 함
                .orderBy(sortOption.equals("nearDeadline") ? scrapPosts.posts.endDate.asc() : scrapPosts.posts.createdDate.desc()); // 정렬 옵션에 따른 조건 추가


        // 페이징 처리
        query.offset(pageable.getOffset()) // Controller에서 인자로 넘겨준 page
                .limit(pageable.getPageSize()); // Controller에서 인자로 넘겨준 size

        List<Tuple> result = query.fetch();

        List<ScrapPostsDto> scrapPostsDtosList = new ArrayList<>(); // 빈 컬렉션 생성

        // 동적 쿼리의 결과를 순회하며 DTO로 변환. 데이터를 가져오는 쿼리에서, scrapPosts와 userpplyPosts를 동시에 가져오려 했기 때문에, Tuple 타입으로 받았고, 여기서 Tuple을 순회한다.
        for (Tuple tuple : result) {
            // 튜플에서 각 테이블을 꺼낸다.
            ScrapPosts scrapPost = tuple.get(0, ScrapPosts.class);
            UserApplyPosts userApplyPost = tuple.get(1, UserApplyPosts.class);

            Posts post = scrapPost.getPosts();
            Category postCategory = post.getCategory();

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);
            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            ScrapPostsDto scrapPostsDto = ScrapPostsDto.builder()
                    .id(post.getId())
                    .nickName(post.getUser().getNickName())
                    .postType(post.getPostType().toString())
                    .title(post.getTitle())
                    .web(postCategory.getWeb())
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    //.counts(post.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    // userApplyPost == null인 경우 isApplied와 isApproved는 false로 설정되고, userApplyPost != null인 경우 true로 설정됨
                    .isApplied(userApplyPost != null) // 지원 여부 확인 (userApplyPosts가 null이 아닌 경우 지원한 것으로 처리).
                    .isApproved(userApplyPost != null && userApplyPost.getConfirm()) // 승인 여부 확인
                    .build();

            scrapPostsDtosList.add(scrapPostsDto); // 컬렉션에 추가
        }

        long total = query.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄

        return new PageImpl<>(scrapPostsDtosList, pageable, total); // 동적 쿼리의 결과를 반환
    }

    // 스크랩한 스터디 게시물 가져오기
    @Transactional(readOnly = true)
    public Page<ScrapPostsDto> getStudyScrapPosts(String userEmail, String sortOption, Pageable pageable) {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QScrapPosts scrapPosts = QScrapPosts.scrapPosts;
        QUserApplyPosts userApplyPosts = QUserApplyPosts.userApplyPosts;

        // '데이터'를 가져오는 쿼리
        JPAQuery<Tuple> query = queryFactory    // queryDsl에서 제공하는 Tuple을 import하기!
                .select(scrapPosts, userApplyPosts) // 스크랩한 게시물과 지원한 게시물을 함께 선택
                .from(scrapPosts)
                .join(scrapPosts.posts, posts).fetchJoin() // 스크랩한 게시물의 Posts 엔티티와 조인
                .join(posts.category, category).fetchJoin() // Posts 엔티티의 Category 엔티티와 조인
                .leftJoin(userApplyPosts) // 지원한 게시물과 조인 (left join으로 변경하여 모든 스크랩 게시물을 가져옴)
                .on(userApplyPosts.user.id.eq(scrapPosts.user.id) // 조인 조건: 스크랩 게시물의 사용자 ID와 지원 게시물의 사용자 ID가 일치해야 함
                        .and(userApplyPosts.posts.id.eq(scrapPosts.posts.id))) // 조인 조건: 스크랩 게시물의 게시물 ID와 지원 게시물의 게시물 ID가 일치해야 함
                .where(scrapPosts.user.email.eq(userEmail)) // 스크랩한 사용자 이메일이 일치해야 함
                .where(posts.postType.eq(PostType.STUDY)) // 스터디 게시물이어야 함
                .orderBy(sortOption.equals("nearDeadline") ? scrapPosts.posts.endDate.asc() : scrapPosts.posts.createdDate.desc()); // 정렬 옵션에 따른 조건 추가


        // 페이징 처리
        query.offset(pageable.getOffset()) // Controller에서 인자로 넘겨준 page
                .limit(pageable.getPageSize()); // Controller에서 인자로 넘겨준 size

        List<Tuple> result = query.fetch();

        List<ScrapPostsDto> scrapPostsDtosList = new ArrayList<>(); // 빈 컬렉션 생성

        // 동적 쿼리의 결과를 순회하며 DTO로 변환. 데이터를 가져오는 쿼리에서, scrapPosts와 userpplyPosts를 동시에 가져오려 했기 때문에, Tuple 타입으로 받았고, 여기서 Tuple을 순회한다.
        for (Tuple tuple : result) {
            // 튜플에서 각 테이블을 꺼낸다.
            ScrapPosts scrapPost = tuple.get(0, ScrapPosts.class);
            UserApplyPosts userApplyPost = tuple.get(1, UserApplyPosts.class);

            Posts post = scrapPost.getPosts();
            Category postCategory = post.getCategory();

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);
            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            ScrapPostsDto scrapPostsDto = ScrapPostsDto.builder()
                    .id(post.getId())
                    .nickName(post.getUser().getNickName())
                    .postType(post.getPostType().toString())
                    .title(post.getTitle())
                    .web(postCategory.getWeb())
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    //.counts(post.getCounts())
                    .counts(applyCount)
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    // userApplyPost == null인 경우 isApplied와 isApproved는 false로 설정되고, userApplyPost != null인 경우 true로 설정됨
                    .isApplied(userApplyPost != null) // 지원 여부 확인 (userApplyPosts가 null이 아닌 경우 지원한 것으로 처리).
                    .isApproved(userApplyPost != null && userApplyPost.getConfirm()) // 승인 여부 확인
                    .build();

            scrapPostsDtosList.add(scrapPostsDto); // 컬렉션에 추가
        }

        long total = query.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄

        return new PageImpl<>(scrapPostsDtosList, pageable, total); // 동적 쿼리의 결과를 반환
    }

    public List<HotPostsListDto> getHotPost() {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QViewCountPosts viewCountPosts = QViewCountPosts.viewCountPosts;

        // 현재 날짜를 가져옴
        LocalDate currentDate = LocalDate.now();

        // endDate가 현재 날짜 이후인 게시물을 필터링하기 위한 조건
        BooleanExpression notExpiredCondition = posts.endDate.goe(currentDate);

        // 데이터를 가져오는 쿼리
        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물을 추출할 건데,
                .join(posts.category, category).fetchJoin() // 게시물을 카테고리와 조인한 형태로 가져올거임
                .where(notExpiredCondition)
                .leftJoin(viewCountPosts).on(posts.id.eq(viewCountPosts.posts.id))
                .groupBy(posts, category)
                .orderBy(viewCountPosts.count().intValue().desc(), posts.lastModifiedDate.desc());

        List<Posts> filteredPosts = query
                .limit(12)
                .fetch();

        List<HotPostsListDto> hotPostsListDtosList = new ArrayList<>(); // 빈 컬렉션 생성

        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (Posts post : filteredPosts) {
            Category postCategory = post.getCategory();        // posts를 통해 카테고리로 접근한 것을 postCategory로 명명

            // UserApplyPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> applyCountOptional = userApplyPostsRepository.countByPostsAndConfirmTrue(post);

            Integer applyCount;

            // applyCountOptional에 값이 존재한다면, 인원 = 현재까지 게시물에 모집된 인원 + 1 (본인)
            // null이라면, 인원 = 1(본인)
            if (applyCountOptional.isPresent()) {
                applyCount = applyCountOptional.get() + 1;
            } else {
                applyCount = 1;
            }

            // ViewCountPosts 엔티티에서 posts_id가 동일한 레코드의 개수를 가져옴.
            Optional<Integer> viewCountOptional = viewCountPostsRepository.countByPosts_Id(post.getId());

            Integer viewCount;

            // viewCountOptional에 값이 존재한다면, 해당 값 가져오기
            // null이라면 조회수는 0으로 세팅
            if (viewCountOptional.isPresent()) {
                viewCount = viewCountOptional.get();
            } else {
                viewCount = 0;
            }

            HotPostsListDto hotPostsListDto = HotPostsListDto.builder()
                    .id(post.getId())
                    .postType(post.getPostType().toString())
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    .counts(applyCount)
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    .viewCount(viewCount)
                    .build();

            hotPostsListDtosList.add(hotPostsListDto);     // 컬렉션에 추가
        }

        return hotPostsListDtosList; // 동적쿼리의 결과를 반환
    }
}

