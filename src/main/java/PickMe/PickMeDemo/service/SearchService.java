package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.repository.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final ViewCountPortfolioRepository viewCountPortfolioRepository;
    private final ViewCountPostsRepository viewCountPostsRepository;
    private final JPAQueryFactory queryFactory;
    private final UserApplyPostsRepository userApplyPostsRepository;

    // 실시간 검색어를 받아서 검색 결과 리스트를 뿌려주는 모듈
    @Transactional(readOnly = true) //읽기 전용
    public SearchResultDto getFilteredSearchLists(String searchTerm) {

        QPosts posts = QPosts.posts;
        QUser user = QUser.user;


        BooleanExpression projectTitleConditions = null; // 검색어 기반으로 프로젝트를 필터링할 때 쓰는 BooleanExpression 조건
        BooleanExpression studyTitleConditions = null; // 검색어 기반으로 스터디를 필터링할 때 쓰는 BooleanExpression 조건
        BooleanExpression userNicknameConditions = null; // 검색어 기반으로 유저 닉네임을 필터링할 때 쓰는 BooleanExpression 조건


//      검색어 문자열을 공백 기호 기준으로 다 split해서 배열로 만들고, 각 배열 요소에 담긴 키워드 조각들을 and한 결과가 게시물에 있으면 해당 게시물이 추출됨
        if (!searchTerm.isEmpty()) { // 만약 문자열이 공백이 아니라면
            String[] keywords = searchTerm.split("\\s+"); // 공백으로 분리한 검색어 배열 생성

            // 각 단어를 처리하여 BooleanExpression 조건 생성 (프로젝트)
            List<BooleanExpression> keywordConditionsForProject = Arrays.stream(keywords)
                    .map(keyword -> posts.title.lower().like("%" + keyword.toLowerCase() + "%")) // keyword가 포함된 프로젝트 title이 있으면 추출될 게시물로 선정
                    .collect(Collectors.toList()); // 각 키워드 조각들에 대해 title에 포함됨? 에 대한 조건을 모두 만들어 list로 만든다.

            projectTitleConditions = keywordConditionsForProject.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(null); // 모든 조건이 없을 경우 null로 설정

            // 공백 기준으로 나눈 키워드 조각들을 모두 and해서 가장 일치하는 프로젝트 제목 찾기
            if (projectTitleConditions != null) {
                projectTitleConditions = projectTitleConditions.and(posts.postType.eq(PostType.PROJECT));
            }

            // 각 단어를 처리하여 BooleanExpression 조건 생성 (스터디)
            List<BooleanExpression> keywordConditionsForStudy = Arrays.stream(keywords)
                    .map(keyword -> posts.title.lower().like("%" + keyword.toLowerCase() + "%")) // keyword가 포함된 스터디 title이 있으면 추출될 게시물로 선정
                    .collect(Collectors.toList()); // 각 키워드 조각들에 대해 title에 포함됨?  에 대한 조건을 모두 만들어 list로 만든다.

            studyTitleConditions = keywordConditionsForStudy.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(null); // 모든 조건이 없을 경우 null로 설정

            // 공백 기준으로 나눈 키워드 조각들을 모두 and해서 가장 일치하는 스터디 제목 찾기
            if (studyTitleConditions != null) {
                studyTitleConditions = studyTitleConditions.and(posts.postType.eq(PostType.STUDY));
            }

            // 각 단어를 처리하여 BooleanExpression 조건 생성 (유저)
            List<BooleanExpression> keywordConditionsForUser = Arrays.stream(keywords)
                    .map(keyword -> user.nickName.lower().like("%" + keyword.toLowerCase() + "%")) // keyword가 포함된 유저 닉네임이 있으면 추출될 게시물로 선정
                    .collect(Collectors.toList()); // 각 키워드 조각들에 대해 nickName에 포함됨?에 대한 조건을 모두 만들어 list로 만든다.

            // 공백 기준으로 나눈 키워드 조각들을 모두 and해서 가장 일치하는 유저 닉네임 찾기
            userNicknameConditions = keywordConditionsForUser.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(null); // 모든 조건이 없을 경우 null로 설정



        }
        ///////////////////////////////////////////////////////////////////////////////////////////// 이제 쿼리를 실행

        // '데이터'를 가져오는 쿼리
        JPAQuery<Posts> queryForProject = queryFactory.selectFrom(posts);
        JPAQuery<Posts> queryForStudy = queryFactory.selectFrom(posts);
        JPAQuery<User> queryForUser = queryFactory.selectFrom(user);

        // 현재 날짜를 가져옴
        LocalDate currentDate = LocalDate.now();
        // endDate가 현재 날짜 이후인 게시물을 필터링하기 위한 조건
        BooleanExpression notExpiredCondition = posts.endDate.goe(currentDate);

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (projectTitleConditions != null) {
            queryForProject.where(projectTitleConditions, notExpiredCondition)
                    .orderBy(posts.endDate.asc()) // 마감일자가 가까운 순으로 정렬
                    .limit(5); // 상위 5개만 가져오기
        }

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (studyTitleConditions != null) {
            queryForStudy.where(studyTitleConditions, notExpiredCondition)
                    .orderBy(posts.endDate.asc()) // 마감일자가 가까운 순으로 정렬
                    .limit(5); // 상위 5개만 가져오기
        }

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (userNicknameConditions != null) {
            queryForUser.where(userNicknameConditions)
                    .orderBy(user.lastAccessDate.desc()) // 로그인 시간이 최근과 가까울 수록 정렬
                    .limit(5); // 상위 5개만 가져오기
        }

        List<Posts> filteredProject = queryForProject.fetch();
        List<Posts> filteredStudy = queryForStudy.fetch();
        List<User> filteredUser = queryForUser.fetch();

        List<ProjectSearchDto> projectSearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성
        List<StudySearchDto> studySearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성
        List<UserSearchDto> userSearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성

        // 프로젝트를 dto에 담기
        for(Posts findPost : filteredProject) {
            ProjectSearchDto projectSearchDto = ProjectSearchDto.builder()
                    .id(findPost.getId())
                    .name(findPost.getTitle())
                    .build();

            projectSearchDtoList.add(projectSearchDto);
        }

        // 스터디를 dto에 담기
        for(Posts findPost : filteredStudy) {
            StudySearchDto studySearchDto = StudySearchDto.builder()
                    .id(findPost.getId())
                    .name(findPost.getTitle())
                    .build();

            studySearchDtoList.add(studySearchDto);
        }

        // 유저를 dto에 담기
        for(User findUser : filteredUser) {
            UserSearchDto userSearchDto = UserSearchDto.builder()
                    .id(findUser.getId())
                    .name(findUser.getNickName())
                    .build();

            userSearchDtoList.add(userSearchDto);
        }

        // 각각 담은 프로젝트 검색결과 배열, 스터디 검색결과 배열, 유저 검색결과 배열을 새로운 dto에 담아 프론트에 반환
        SearchResultDto searchResultDto = SearchResultDto.builder()
                .projectSearchDtoList(projectSearchDtoList)
                .studySearchDtoList(studySearchDtoList)
                .userSearchDtoList(userSearchDtoList)
                .build();



        return searchResultDto; // 동적쿼리의 결과를 반환

    }


    // 포트폴리오 검색 결과 데이터 뿌려주는 함수
    @Transactional(readOnly = true) //읽기 전용
    public Page<PortfolioCardDto> getPortfolioSearchList(List<String> selectedBanners, String sortOption, String searchTerm, Pageable pageable) {

        QPortfolio portfolios = QPortfolio.portfolio;
        QUser users = QUser.user;
        QViewCountPortfolio viewCountPortfolio = QViewCountPortfolio.viewCountPortfolio;

        BooleanExpression bannerConditions = buildBannerConditions(portfolios, selectedBanners);
        // 검색어 기반으로 필터링할 때 쓰는 BooleanExpression 조건
        BooleanExpression titleOrContentConditions = null;

//      검색어 문자열을 공백 기호 기준으로 다 split해서 배열로 만들고, 각 배열 요소에 담긴 키워드 조각들을 and한 결과가 게시물에 있으면 해당 게시물이 추출됨
        if (!searchTerm.isEmpty()) { // 만약 문자열이 공백이 아니라면
            String[] keywords = searchTerm.split("\\s+"); // 공백으로 분리한 검색어 배열 생성

            // 각 단어를 처리하여 BooleanExpression 조건 생성
            List<BooleanExpression> keywordConditions = Arrays.stream(keywords)
                    .map(keyword -> portfolios.user.nickName.lower().like("%" + keyword.toLowerCase() + "%") // keyword가 포함된 게시물 title이 있으면 추출될 게시물로 선정
                            .or(portfolios.shortIntroduce.lower().like("%" + keyword.toLowerCase() + "%"))) // keyword가 포함된 게시물 content가 있으면 추출될 게시물로 선정
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
        JPAQuery<Portfolio> query = queryFactory.selectFrom(portfolios) // 게시물을 추출할 건데,
                .join(portfolios.user, users) // 게시물을 카테고리와 조인한 형태로 가져올거임
                .where(bannerConditions, portfolios.user.nickName.eq(users.nickName));
        // (where로 조건 추가 1.) 근데 조건은 이러하고 (밑에 있음)
        // (where로 조건 추가 2.) 게시물의 TYPE이 프로젝트인 것만 가져옴

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (titleOrContentConditions != null) {
            query = query.where(titleOrContentConditions);
        }

        if ("byViewCount".equals(sortOption)) {
            query = query
                    .leftJoin(viewCountPortfolio).on(portfolios.id.eq(viewCountPortfolio.portfolio.id)).fetchJoin() // 위쪽에서 fetchJoin()을 써버리면, 조회수 순 정렬을 할 때 오류가 발생한다!!
                    .groupBy(portfolios, users)
                    .orderBy(viewCountPortfolio.count().intValue().desc());
        } else {
            query = query.orderBy(portfolios.lastModifiedDate.desc());
        }

        // '카운트 쿼리' 별도로 보냄 (리팩토링 필요 예정 - 성능 최적화 위해)
        JPQLQuery<Portfolio> countQuery = queryFactory.selectFrom(portfolios)
                .join(portfolios.user, users); // Join with category

//              .orderBy(posts.lastModifiedDate.desc()); 카운트 쿼리에선 정렬 필요없음

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (titleOrContentConditions != null) {
            countQuery = countQuery.where(titleOrContentConditions);
        }


        long total = countQuery.fetchCount(); // Count쿼리에 의해 전체 데이터 개수 알아냄

        // 데이터를 가져오는 쿼리를 실제로 offset, limit까지 설정해서 쿼리 날림
        List<Portfolio> filteredPortfolios = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        List<PortfolioCardDto> portfolioCardDtos = new ArrayList<>(); // 빈 컬렉션 생성

        // 동적 쿼리의 결과를 순회하며 dto로 변환
        for (Portfolio portfolio : filteredPortfolios) {
            //User portfolioUser = portfolio.getUser();        // posts를 통해 카테고리로 접근한 것을 postCategory로 명명
            User user = portfolio.getUser();                         // posts를 통해 유저 접근한 것을 user로 명명
            String url = user.getImageUrl();

            Optional<Integer> viewCountOptional = viewCountPortfolioRepository.countByPortfolio_Id(portfolio.getId());

            Integer viewCount = viewCountOptional.orElse(0); // 조회수 값이 없으면 0을 사용

            PortfolioCardDto cardDto = PortfolioCardDto.builder()
                    .nickName(user.getNickName())   // user = posts.getUser()
                    .web(portfolio.getWeb())     // category = posts.getCategory()
                    .app(portfolio.getApp())
                    .game(portfolio.getGame())
                    .ai(portfolio.getAi())
                    .shortIntroduce(portfolio.getShortIntroduce())
                    .viewCount(viewCount)
                    .imageUrl(url)
                    .build();

            portfolioCardDtos.add(cardDto);     // 컬렉션에 추가
        }


        return new PageImpl<>(portfolioCardDtos, pageable, total); // 동적쿼리의 결과를 반환

    }


    // 포트폴리오 검색 결과 창에서 사용할 배너 상태 조건 함수
    private BooleanExpression buildBannerConditions(QPortfolio portfolio, List<String> selectedBanners) {
        BooleanExpression bannerConditions = null;

        List<BooleanExpression> selectedConditions = new ArrayList<>();

        for (String selectedBanner : selectedBanners) {
            BooleanExpression bannerCondition = null;

            switch (selectedBanner) {
                case "web":
                    bannerCondition = portfolio.web.gt(0);
                    break;
                case "app":
                    bannerCondition = portfolio.app.gt(0);
                    break;
                case "game":
                    bannerCondition = portfolio.game.gt(0);
                    break;
                case "ai":
                    bannerCondition = portfolio.ai.gt(0);
                    break;
                default:
                    bannerCondition = null;
                    break;
            }

            if (bannerCondition != null) {
                selectedConditions.add(bannerCondition);
            }
        }

        if (!selectedConditions.isEmpty()) {
            bannerConditions = selectedConditions.get(0);
            for (int i = 1; i < selectedConditions.size(); i++) {
                bannerConditions = bannerConditions.and(selectedConditions.get(i));
            }
        }

        return bannerConditions;
    }


    // 포트폴리오 검색 결과 데이터 뿌려주는 함수
    @Transactional(readOnly = true) //읽기 전용
    public Page<PostsListDto> getProjectSearchList(List<String> selectedBanners, String sortOption, String searchTerm, Pageable pageable) {
        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QViewCountPosts viewCountPosts = QViewCountPosts.viewCountPosts;


        // buildBannerConditionsInProjects 메서드를 사용하여 선택한 배너를 기반으로 BooleanExpression을 구성
        BooleanExpression bannerConditions = buildBannerConditionsInProjects(category, selectedBanners);

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

    // 스터디 검색 결과 데이터 뿌려주는 함수
    @Transactional(readOnly = true) //읽기 전용
    public Page<PostsListDto> getStudySearchList(List<String> selectedBanners, String sortOption, String searchTerm, Pageable pageable) {
        QPosts posts = QPosts.posts;
        QCategory category = QCategory.category;
        QViewCountPosts viewCountPosts = QViewCountPosts.viewCountPosts;


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
}
