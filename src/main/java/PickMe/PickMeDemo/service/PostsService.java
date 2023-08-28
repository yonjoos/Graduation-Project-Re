package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.CategoryRepository;
import PickMe.PickMeDemo.repository.PostsRepository;
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
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostsService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final CategoryRepository categoryRepository;
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
        Optional<User> findUser = userRepository.findByEmail(userEmail);

        User user = findUser.orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts posts = Posts.builder()
                .user(user)
                .postType(postType)
                .title(postsFormDto.getTitle())
                .recruitmentCount(postsFormDto.getRecruitmentCount())
                .counts(1)      // 맨 처음 지원자 수는 1명 (본인)
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
        if (posts.getUser().getEmail().equals(userEmail) ) {
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
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
        }
        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())가 아니라면
        else {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
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
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
        }
        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())가 아니라면
        else {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
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
    public Page<PostsListDto> getFilteredProjects(List<String> selectedBanners, String sortOption, Pageable pageable) {

        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;

        //System.out.println("pageable.getOffset() = " + pageable.getOffset());
        //System.out.println("pageable.getPageSize() = " + pageable.getPageSize());

        // buildBannerConditionsInProjects 메서드를 사용하여 선택한 배너를 기반으로 BooleanExpression을 구성
        BooleanExpression bannerConditions = buildBannerConditionsInProjects(category, selectedBanners);

        // 데이터를 가져오는 쿼리
        JPAQuery<Posts> query = queryFactory.selectFrom(posts) // 게시물을 추출할 건데,
                .join(posts.category, category) // 게시물을 카테고리와 조인한 형태로 가져올거임
                .where(bannerConditions) // (where로 조건 추가 1.) 근데 조건은 이러하고 (밑에 있음)
                .where(posts.postType.eq(PostType.valueOf("PROJECT"))) // (where로 조건 추가 2.) 게시물의 TYPE이 프로젝트인 것만 가져옴
                .orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc());
                //만약 소트 조건이 마감일순이면 마감일 순 정렬, 아니면 최신등록순 정렬

        // 카운트 쿼리 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<Posts> countQuery = queryFactory.selectFrom(posts)
                .join(posts.category, category) // Join with category
                .where(bannerConditions)
                .where(posts.postType.eq(PostType.valueOf("PROJECT")));
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

    // 프로젝트 게시물 조회에서 동적 쿼리의 where절에 들어갈 조건 생성하기
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
                .where(bannerConditions) // (where로 조건 추가 1.) 근데 조건은 이러하고 (밑에 있음)
                .where(posts.postType.eq(PostType.valueOf("STUDY"))) // (where로 조건 추가 2.) 게시물의 TYPE이 프로젝트인 것만 가져옴
                .orderBy(sortOption.equals("nearDeadline") ? posts.endDate.asc() : posts.createdDate.desc());
        //만약 소트 조건이 마감일순이면 마감일 순 정렬, 아니면 최신등록순 정렬

        // 카운트 쿼리 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<Posts> countQuery = queryFactory.selectFrom(posts)
                .join(posts.category, category) // Join with category
                .where(bannerConditions)
                .where(posts.postType.eq(PostType.valueOf("STUDY")));
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
}

