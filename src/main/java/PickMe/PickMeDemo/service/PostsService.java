package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.CategoryRepository;
import PickMe.PickMeDemo.repository.PostsRepository;
import PickMe.PickMeDemo.repository.UserApplyPostsRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostsService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final CategoryRepository categoryRepository;
    private final UserApplyPostsRepository userApplyPostsRepository;
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
                .counts(1)      // 맨 처음 지원자 수는 1명 (본인 포함)
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



    // 프로젝트 게시물 리스트 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})     // 페치 조인으로 조회할 대상 테이블
    public List<PostsListDto> getProjectList() {
        return getPostsList(PostType.PROJECT);
    }

    // 스터디 게시물 리스트 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})     // 페치 조인으로 조회할 대상 테이블
    public List<PostsListDto> getStudyList() {
        return getPostsList(PostType.STUDY);
    }

    // 프로젝트 리스트 조회와 스터디 리스트 조회의 공통 코드를 분리하여 메서드로 만듦
    private List<PostsListDto> getPostsList(PostType postType) {
        List<Posts> postsList = postsRepository.findByPostType(postType);   // 게시물의 타입에 맞는 게시물 찾기

        List<PostsListDto> postsListDtoList = new ArrayList<>();        // 빈 컬렉션 생성

        for (Posts posts : postsList) {
            Category category = posts.getCategory();        // posts라는 연결고리를 통해 연결고리로 접근
            User user = posts.getUser();                    // posts라는 연결고리를 통해 연결고리로 접근

            PostsListDto postsListDto = PostsListDto.builder()
                    .id(posts.getId())
                    .nickName(user.getNickName())   // user = posts.getUser()
                    .title(posts.getTitle())
                    .web(category.getWeb())     // category = posts.getCategory()
                    .app(category.getApp())
                    .game(category.getGame())
                    .ai(category.getAi())
                    .counts(posts.getCounts())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();

            postsListDtoList.add(postsListDto);     // 컬렉션에 추가
        }

        return postsListDtoList;
    }



    // 프로젝트 단건 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public PostsDto getProject(String userEmail, Long projectId) {

        Posts posts = postsRepository.findByIdAndPostType(projectId, PostType.PROJECT)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        PostsDto postsDto;

        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())와 동일하다면
        if (posts.getUser().getEmail().equals(userEmail)) {
             postsDto = PostsDto.builder()
                    .writer(true)      // writer에 true를 리턴
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    .counts(posts.getCounts())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
        }
        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())가 아니라면
        else {
            boolean hasApplied = false;     // 지원 여부
            boolean isConfirmed = false;    // 승인 여부

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
                postsDto = PostsDto.builder()
                        .writer(false)      // writer에 false를 리턴
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
                        .counts(posts.getCounts())
                        .recruitmentCount(posts.getRecruitmentCount())
                        .endDate(posts.getEndDate())
                        .build();
            }
            // 게시물에 지원했으나, 승인이 안난 사람
            else if (!isConfirmed) {
                postsDto = PostsDto.builder()
                        .writer(false)      // writer에 false를 리턴
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
                        .counts(posts.getCounts())
                        .recruitmentCount(posts.getRecruitmentCount())
                        .endDate(posts.getEndDate())
                        .build();
            }
            // writer로부터 승인이 난 사람
            else {
                postsDto = PostsDto.builder()
                        .writer(false)      // writer에 false를 리턴
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
                        .counts(posts.getCounts())
                        .recruitmentCount(posts.getRecruitmentCount())
                        .endDate(posts.getEndDate())
                        .build();
            }
        }

        return postsDto;
    }


    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public PostsDto getStudy(String userEmail, Long studyId) {

        Posts posts = postsRepository.findByIdAndPostType(studyId, PostType.STUDY)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        PostsDto postsDto;

        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())와 동일하다면
        if (posts.getUser().getEmail().equals(userEmail)) {
            postsDto = PostsDto.builder()
                    .writer(true)      // writer에 true를 리턴
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    .counts(posts.getCounts())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
        }
        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())가 아니라면
        else {
            // 게시물에 지원 안한 사람
            if (posts.getUserApplyPosts().isEmpty()) {
                postsDto = PostsDto.builder()
                        .writer(false)      // writer에 false를 리턴
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
                        .counts(posts.getCounts())
                        .recruitmentCount(posts.getRecruitmentCount())
                        .endDate(posts.getEndDate())
                        .build();
            }
            else {
                UserApplyPosts post = userApplyPostsRepository.findByPosts_Id(studyId)
                        .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

                // 게시물에 지원했으나, 승인이 안난 사람
                if (!post.getConfirm()) {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
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
                            .counts(posts.getCounts())
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .build();
                }
                else {
                    postsDto = PostsDto.builder()
                            .writer(false)      // writer에 false를 리턴
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
                            .counts(posts.getCounts())
                            .recruitmentCount(posts.getRecruitmentCount())
                            .endDate(posts.getEndDate())
                            .build();
                }
            }
        }

        return postsDto;
    }



    // ** 중요 **
    // postType을 Boolean 리스트로 받아오는 PostsUpdateFormDto 사용!
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public PostsUpdateFormDto getProjectForm(String userEmail, Long projectId) {

        // projectId와 userEmail로 Project 찾기
        Posts findProject = postsRepository.findByIdAndUser_Email(projectId, userEmail)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

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

        // '데이터'를 가져오는 쿼리
        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물을 추출할 건데,
                .join(posts.category, category) // 게시물을 카테고리와 조인한 형태로 가져올거임
                .where(bannerConditions,posts.postType.eq(PostType.valueOf("PROJECT")));
                // (where로 조건 추가 1.) 근데 조건은 이러하고 (밑에 있음)
                // (where로 조건 추가 2.) 게시물의 TYPE이 프로젝트인 것만 가져옴

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (titleOrContentConditions != null) {
            query = query.where(titleOrContentConditions);
        }

        // 정렬 옵션에 따른 조건 추가
        query=query.orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc());
                //만약 소트 조건이 마감일순이면 마감일 순 정렬, 아니면 최신등록순 정렬


        // '카운트 쿼리' 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<Posts> countQuery = queryFactory.selectFrom(posts)
                .join(posts.category, category) // Join with category
                .where(bannerConditions,posts.postType.eq(PostType.valueOf("PROJECT")));

//              .orderBy(posts.createdDate.desc()); 카운트 쿼리에선 정렬 필요없음

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
            Category postCategory = post.getCategory();        // posts라는 연결고리를 통해 연결고리로 접근
            User user = post.getUser();                    // posts라는 연결고리를 통해 연결고리로 접근

            PostsListDto postsListDto = PostsListDto.builder()
                    .id(post.getId())
                    .nickName(user.getNickName())   // user = posts.getUser()
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    .counts(post.getCounts())
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
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
    public Page<PostsListDto> getFilteredStudies(List<String> selectedBanners, String sortOption, Pageable pageable) {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;

        // buildBannerConditionsInStudies 메서드를 사용하여 선택한 배너를 기반으로 BooleanExpression을 구성
        BooleanExpression bannerConditions = buildBannerConditionsInStudies(category, selectedBanners);

        // 데이터를 가져오는 쿼리
        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물을 추출할 건데,
                .join(posts.category, category) // 게시물을 카테고리와 조인한 형태로 가져올거임
                .where(bannerConditions,posts.postType.eq(PostType.valueOf("STUDY")))
                // (where로 조건 추가 1.) 근데 조건은 이러하고 (밑에 있음)
                // (where로 조건 추가 2.) 게시물의 TYPE이 프로젝트인 것만 가져옴
                .orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc());
        //만약 소트 조건이 마감일순이면 마감일 순 정렬, 아니면 최신등록순 정렬

        // 카운트 쿼리 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<Posts> countQuery = queryFactory.selectFrom(posts)
                .join(posts.category, category) // Join with category
                .where(bannerConditions,posts.postType.eq(PostType.valueOf("STUDY")));

        // .orderBy(posts.createdDate.desc()); 카운트 쿼리에선 정렬 필요없음

        long total = countQuery.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄

        // 데이터를 가져오는 쿼리를 실제로 offset, limit까지 설정해서 쿼리 날림
        List<Posts> filteredPosts = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        List<PostsListDto> postsListDtoList = new ArrayList<>(); // 빈 컬렉션 생성

        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (Posts post : filteredPosts) {
            Category postCategory = post.getCategory();        // posts라는 연결고리를 통해 연결고리로 접근
            User user = post.getUser();                    // posts라는 연결고리를 통해 연결고리로 접근

            PostsListDto postsListDto = PostsListDto.builder()
                    .id(post.getId())
                    .nickName(user.getNickName())   // user = posts.getUser()
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
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
    @Transactional(readOnly = true) //읽기 전용
    // PageRequest.of(page, size)을 인자로 받을 때, 파라미터의 이름은 pageable로 바꾸어 설정
    public Page<GroupPostsListDto> getWriterPosts(String userEmail, String sortOption, Pageable pageable) {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QUserApplyPosts userApplyPosts = QUserApplyPosts.userApplyPosts;

        // '데이터'를 가져오는 쿼리
        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물을 추출할 건데,
                .join(posts.category, category) // 게시물을 카테고리와 조인한 형태 +
                .leftJoin(posts.userApplyPosts, userApplyPosts) // 현재 게시물과 지원 게시물을 조인한 형태로 가져올 것임. userApplyPosts가 비어있을 경우, join의 결과가 null이므로, leftJoin으로 묶어준다!!
                .where(posts.user.email.eq(userEmail))     // 단, 현재 로그인한 유저가 올린 글이어야 함
                .orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc());
        //만약 소트 조건이 마감일순이면 마감일 순 정렬, 아니면 최신등록순 정렬

        // '카운트 쿼리' 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<Posts> countQuery = queryFactory.selectFrom(posts)
                .join(posts.category, category) // 게시물과 카테고리를 조인
                .join(posts.userApplyPosts, userApplyPosts) // 현재 게시물과 지원 게시물을 조인
                .where(posts.user.email.eq(userEmail));

        long total = countQuery.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄

        // 데이터를 가져오는 쿼리를 실제로 offset, limit까지 설정해서 쿼리 날림
        List<Posts> filteredPosts = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<GroupPostsListDto> groupPostsListDtosList = new ArrayList<>(); // 빈 컬렉션 생성

        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (Posts post : filteredPosts) {
            Category postCategory = post.getCategory();        // posts라는 연결고리를 통해 연결고리로 접근
            User user = post.getUser();
            List<UserApplyPosts> userApplyPost = post.getUserApplyPosts();

            // applyNickNames라는 List 컬렉션에 게시물에 지원한 닉네임을 모두 담아 리턴한다.
            List<String> applyNickNames = userApplyPost.stream()
                    .map(userApply -> userApply.getUser().getNickName())  // Get the nickname of each user who applied
                    .collect(Collectors.toList());

            GroupPostsListDto groupPostsListDto = GroupPostsListDto.builder()
                    .id(post.getId())
                    .writerNickName(user.getNickName())   // user = posts.getUser()
                    .applyNickNames(applyNickNames)
                    .postType(post.getPostType().toString())    // postType은 Enum 타입이므로, toString() 해주기
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    .counts(post.getCounts())
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    .build();

            groupPostsListDtosList.add(groupPostsListDto);     // 컬렉션에 추가
        }

        return new PageImpl<>(groupPostsListDtosList, pageable, total); // 동적쿼리의 결과를 반환
    }


    // GroupPage에 내가 지원한 게시물 데이터를 가져오는 메서드
    @Transactional(readOnly = true) //읽기 전용
    // PageRequest.of(page, size)을 인자로 받을 때, 파라미터의 이름은 pageable로 바꾸어 설정
    public Page<GroupPostsListDto> getApplicantPosts(String userEmail, String sortOption, Pageable pageable) {

        QUser user = QUser.user;
        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QUserApplyPosts userApplyPosts = QUserApplyPosts.userApplyPosts;

        // '데이터'를 가져오는 쿼리
        JPAQuery<UserApplyPosts> query = queryFactory.selectFrom(userApplyPosts) // 게시물을 추출할 건데,
                .join(userApplyPosts.posts, posts)  // 게시물을 카테고리와 조인한 형태로 가져올거임
                .join(posts.category, category)
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<GroupPostsListDto> groupPostsListDtosList = new ArrayList<>(); // 빈 컬렉션 생성


        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (UserApplyPosts userApplyPost : filteredPosts) {
            Posts post = userApplyPost.getPosts();          // posts에 접근
            Category postCategory = userApplyPost.getPosts().getCategory();        // posts라는 연결고리를 통해 category로 접근

            GroupPostsListDto groupPostsListDto = GroupPostsListDto.builder()
                    .id(post.getId())
                    .writerNickName(post.getUser().getNickName())   // post = userApplyPost.getPosts()
                    .applyNickNames(null)
                    .postType(post.getPostType().toString())    // postType은 Enum 타입이므로, toString() 해주기
                    .title(post.getTitle())
                    .web(postCategory.getWeb())     // category = posts.getCategory()
                    .app(postCategory.getApp())
                    .game(postCategory.getGame())
                    .ai(postCategory.getAi())
                    .counts(post.getCounts())
                    .recruitmentCount(post.getRecruitmentCount())
                    .endDate(post.getEndDate())
                    .build();

            groupPostsListDtosList.add(groupPostsListDto);     // 컬렉션에 추가
        }

        return new PageImpl<>(groupPostsListDtosList, pageable, total); // 동적쿼리의 결과를 반환
    }

//    public Page<GroupPostsListDto> getGroupPosts(String userEmail, String postsOption, String sortOption, Pageable pageable) {
//
//        QPosts posts = QPosts.posts;
//        QCategory category = QCategory.category;
//
//        // '데이터'를 가져오는 쿼리
//        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물을 추출할 건데,
//                .join(posts.category, category); // 게시물을 카테고리와 조인한 형태로 가져올거임
//
//
//        // 만약 postsOption이 writer라면, userEmail과 같은 애들로 쿼리를 만들 것임.
//        // postsOption == "writer"로 하면, 작동하지 않음! equals 써줄 것.
//        if ("writer".equals(postsOption)) {
//            query = query.where(posts.user.email.eq(userEmail));
//        }
//        // 만약 postsOption이 aplicant라면, userEmail과 같지 않은 애들로 쿼리를 만들 것임.
//        // postsOption != "writer"로 하면, 작동하지 않음! equals 써줄 것.
//        if (!"writer".equals(postsOption)) {
//            // 지원한 게시물 긁어오기. 지원 테이블을 찾은 후, 해당 게시물 가져오는 로직 추가.
//            query = query.join(posts.userApplyPosts, userApplyPosts);
//        }
//
//        // 정렬 옵션에 따른 조건 추가
//        query=query.orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc());
//        //만약 소트 조건이 마감일순이면 마감일 순 정렬, 아니면 최신등록순 정렬
//
//
//        // '카운트 쿼리' 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
//        JPQLQuery<Posts> countQuery = queryFactory.selectFrom(posts)
//                .join(posts.category, category); // Join with category
//
//        // 만약 postsOption이 writer라면, userEmail과 같은 애들로 쿼리를 만들 것임.
//        // postsOption == "writer"로 하면, 작동하지 않음! equals 써줄 것.
//        if ("writer".equals(postsOption)) {
//            countQuery = countQuery.where(posts.user.email.eq(userEmail));
//        }
//        // 만약 postsOption이 aplicant라면, userEmail과 같지 않은 애들로 쿼리를 만들 것임.
//        // postsOption != "writer"로 하면, 작동하지 않음! equals 써줄 것.
//        else if (!"writer".equals(postsOption)) {
//            countQuery = countQuery.where(posts.user.email.ne(userEmail));
//        }
//
//        long total = countQuery.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄
//
//        // 데이터를 가져오는 쿼리를 실제로 offset, limit까지 설정해서 쿼리 날림
//        List<Posts> filteredPosts = query
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//
//        List<GroupPostsListDto> groupPostsListDtosList = new ArrayList<>(); // 빈 컬렉션 생성
//
//        // 동적 쿼리의 결과를 순회하며 dto로 변환
//        for (Posts post : filteredPosts) {
//            Category postCategory = post.getCategory();        // posts라는 연결고리를 통해 연결고리로 접근
//            User user = post.getUser();                    // posts라는 연결고리를 통해 연결고리로 접근
//
//            GroupPostsListDto groupPostsListDto = GroupPostsListDto.builder()
//                    .id(post.getId())
//                    .nickName(user.getNickName())   // user = posts.getUser()
//                    .postType(post.getPostType().toString())    // postType은 Enum 타입이므로, toString() 해주기
//                    .title(post.getTitle())
//                    .web(postCategory.getWeb())     // category = posts.getCategory()
//                    .app(postCategory.getApp())
//                    .game(postCategory.getGame())
//                    .ai(postCategory.getAi())
//                    .counts(post.getCounts())
//                    .recruitmentCount(post.getRecruitmentCount())
//                    .endDate(post.getEndDate())
//                    .build();
//
//            groupPostsListDtosList.add(groupPostsListDto);     // 컬렉션에 추가
//        }
//
//        return new PageImpl<>(groupPostsListDtosList, pageable, total); // 동적쿼리의 결과를 반환
//    }



    // 프로젝트에 지원하는 것과 관련된 메서드
    public PostsDto applyProject(String userEmail, Long projectId) {

        // email로 현재 유저 찾기
        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // projectId로 게시물 찾기
        Posts findProject = postsRepository.findById(projectId)
                .orElseThrow(() -> new AppException("프로젝트를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // UserApplyPosts 테이블에 들어갈 내용 채우기
        UserApplyPosts userApplyPosts = UserApplyPosts.builder()
                .user(findUser)
                .posts(findProject)
                .confirm(false)     // 초기에는 승인되지 않았으므로, false
                .build();

        UserApplyPosts savedUserApplyPosts = userApplyPostsRepository.save(userApplyPosts);

        PostsDto postsDto = PostsDto.builder()
                .writer(false)      // writer에 false를 리턴
                .applying(true)     // 지원은 했으나 (지원 중이지만),
                .applied(false)     // 지원이 승인된 것은 아님.
                .nickName(savedUserApplyPosts.getPosts().getUser().getNickName())   // 게시물 작성자 닉네임. 주의 : savedUserApplyPosts.getUser().getNickName()하면 지원한 사람의 닉네임이 나옴!!
                .title(savedUserApplyPosts.getPosts().getTitle())
                .web(savedUserApplyPosts.getPosts().getCategory().getWeb())
                .app(savedUserApplyPosts.getPosts().getCategory().getApp())
                .game(savedUserApplyPosts.getPosts().getCategory().getGame())
                .ai(savedUserApplyPosts.getPosts().getCategory().getAi())
                .content(savedUserApplyPosts.getPosts().getContent())
                .promoteImageUrl(savedUserApplyPosts.getPosts().getPromoteImageUrl())
                .fileUrl(savedUserApplyPosts.getPosts().getFileUrl())
                .counts(savedUserApplyPosts.getPosts().getCounts())
                .recruitmentCount(savedUserApplyPosts.getPosts().getRecruitmentCount())
                .endDate(savedUserApplyPosts.getPosts().getEndDate())
                .build();

        return postsDto;
    }
}

