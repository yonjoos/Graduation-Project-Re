package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import PickMe.PickMeDemo.repository.ViewCountPortfolioRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PortfolioService {


    private final JPAQueryFactory queryFactory;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository; // Add this if not already defined
    // Portfolio Mapper를 사용하고자 했으나, 이상하게 스프링 빈으로 등록이 안되어서, private final 변수로 사용할 수 없었음.
    private final ViewCountPortfolioRepository viewCountPortfolioRepository;
    
    
    // 포트폴리오 등록
    public PortfolioDto uploadPortfolio(PortfolioFormDto portfolioFormDto, String userEmail) {

        // Optional이므로, 해당 유저가 발견되면 유저를 반환, 해당 유저가 없으면 null 반환
        Optional<User> findUser = userRepository.findByEmail(userEmail);

        // orElseThrow(...): 이 메서드는 Optional 객체에서 호출됩니다.
        // 비어있는 경우 예외 객체를 생성하고 던질 람다 표현식을 받습니다.
        // 이 경우 Optional이 비어있는 경우(사용자를 찾지 못한 경우) "사용자를 찾을 수 없습니다"라는 메시지와 BAD_REQUEST (400) HTTP 상태 코드를 가진 AppException이 생성됩니다.
        User user = findUser.orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // Setter대신 생성자를 사용하여 Portfolio 테이블을 채움
        Portfolio registerPortfolio = Portfolio.builder()
                .user(user)
                .web(portfolioFormDto.getWeb())
                .app(portfolioFormDto.getApp())
                .game(portfolioFormDto.getGame())
                .ai(portfolioFormDto.getAi())
                .shortIntroduce(portfolioFormDto.getShortIntroduce())
                .introduce(portfolioFormDto.getIntroduce().replace("<br>", "\n"))
                .fileUrl(portfolioFormDto.getFileUrl())
                .build();

        // 포트폴리오 디비에 저장
        Portfolio portfolio = portfolioRepository.save(registerPortfolio);

        // 디비에 저장과는 별개로, 화면에 다시 데이터를 뿌려줄 PortfolioDto를 생성해서 반환
        // portfolioDto의 필드 : isCreated, nickName, email, web, app, game, ai, shortIntroduce, introduce, fileUrl
        // 포트폴리오를 생성하는 것이므로, isCreated를 true로 바로 저장
        PortfolioDto portfolioDto = PortfolioDto.builder()
                .isCreated(true)
                .nickName(user.getNickName())
                .email(user.getEmail())
                .web(portfolio.getWeb())
                .app(portfolio.getApp())
                .game(portfolio.getGame())
                .ai(portfolio.getAi())
                .shortIntroduce(portfolio.getShortIntroduce())
                .introduce(portfolio.getIntroduce())
                .fileUrl(portfolio.getFileUrl())
                .build();

        return portfolioDto;
    }



    // 나의 포트폴리오 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = "user")
    public PortfolioReturnDto getPortfolio(String userEmail) {
        // UserEmail을 통해 해당 User 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // User를 통해 User가 갖고 있는 포트폴리오 찾기
        Optional<Portfolio> findPortfolio = portfolioRepository.findByUser(user);

        Optional<Integer> viewCountOptional = viewCountPortfolioRepository.countByPortfolio_Id(findPortfolio.get().getId());

        Integer viewCount = viewCountOptional.orElse(0); // 조회수 값이 없으면 0을 사용

        // PortfolioReturnDtoDto를 빌더를 통해 생성
        PortfolioReturnDto portfolioReturnDto;

        if (findPortfolio.isPresent()) {
            // portfolioReturnDto를 빌더를 통해 생성
            portfolioReturnDto = PortfolioReturnDto.builder()
                    .isCreated(true)
                    .nickName(user.getNickName())
                    .email(user.getEmail())
                    .web(findPortfolio.get().getWeb())
                    .app(findPortfolio.get().getApp())
                    .game(findPortfolio.get().getGame())
                    .ai(findPortfolio.get().getAi())
                    .shortIntroduce(findPortfolio.get().getShortIntroduce())
                    .introduce(findPortfolio.get().getIntroduce())
                    .fileUrl(findPortfolio.get().getFileUrl())
                    .viewCount(viewCount)
                    .build();
        }
        else {
            portfolioReturnDto = PortfolioReturnDto.builder()
                    .isCreated(false)
                    .nickName(user.getNickName())
                    .email(user.getEmail())
                    .web(null)
                    .app(null)
                    .game(null)
                    .ai(null)
                    .shortIntroduce(null)
                    .introduce(null)
                    .fileUrl(null)
                    .viewCount(null)
                    .build();
        }

        return portfolioReturnDto;
    }


    // 포트폴리오 폼 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = "user")
    public PortfolioFormDto getPortfolioForm(String userEmail) {
        // UserEmail을 통해 해당 User 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // User를 통해 User가 갖고 있는 포트폴리오 찾기
        Optional<Portfolio> portfolio = portfolioRepository.findByUser(user);

        PortfolioFormDto portfolioFormDto;

        if (portfolio.isPresent()) {
            portfolioFormDto = PortfolioFormDto.builder()
                    .hasPortfolio(true)
                    .web(user.getPortfolio().getWeb())
                    .app(user.getPortfolio().getApp())
                    .game(user.getPortfolio().getGame())
                    .ai(user.getPortfolio().getAi())
                    .shortIntroduce(user.getPortfolio().getShortIntroduce())
                    .introduce(user.getPortfolio().getIntroduce())
                    .fileUrl(user.getPortfolio().getFileUrl())
                    .build();
        }
        else {
            portfolioFormDto = PortfolioFormDto.builder()
                    .hasPortfolio(false)
                    .web(user.getPortfolio().getWeb())
                    .app(user.getPortfolio().getApp())
                    .game(user.getPortfolio().getGame())
                    .ai(user.getPortfolio().getAi())
                    .shortIntroduce(user.getPortfolio().getShortIntroduce())
                    .introduce(user.getPortfolio().getIntroduce())
                    .fileUrl(user.getPortfolio().getFileUrl())
                    .build();
        }
        // PortfolioDto를 빌더를 통해 생성


        return portfolioFormDto;
    }


    // Optional을 사용하여, portfolio를 찾았으면 true를 리턴, 찾지 못했으면 false를 리턴
    @Transactional(readOnly = true)
    public boolean hasPortfolio(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        Optional<Portfolio> portfolioOptional = portfolioRepository.findByUser(user);

        // 해당 유저의 포트폴리오가 존재하면 true, 존재하지 않으면 false
        return portfolioOptional.isPresent();
    }


    // 포트폴리오 업데이트
    @EntityGraph(attributePaths = "user")
    public void updatePortfolio(String userEmail, PortfolioFormDto portfolioFormDto) {

        // UserEmail을 통해 해당 User 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // User를 통해 User가 갖고 있는 포트폴리오 찾기
        Portfolio portfolio = portfolioRepository.findByUser(user)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        portfolio.setWeb(portfolioFormDto.getWeb());
        portfolio.setApp(portfolioFormDto.getApp());
        portfolio.setGame(portfolioFormDto.getGame());
        portfolio.setAi(portfolioFormDto.getAi());
        portfolio.setShortIntroduce(portfolioFormDto.getShortIntroduce());
        portfolio.setIntroduce(portfolioFormDto.getIntroduce());
        portfolio.setFileUrl(portfolioFormDto.getFileUrl());

        portfolioRepository.save(portfolio);
    }


    // 포트폴리오 삭제
    @EntityGraph(attributePaths = "user")
    public void deletePortfolio(String userEmail) {
        // userEmail로 user 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("유저를 찾을 수 없습니다.",HttpStatus.NOT_FOUND));

        // User를 통해 User가 갖고 있는 포트폴리오 찾기
        Portfolio portfolio = portfolioRepository.findByUser(user)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        portfolioRepository.delete(portfolio);
    }

    //@Transactional(readOnly = true)   // 포트폴리오 조회 함수지만, 조회 시 조회 수를 카운트하기 위해 ViewCountPortfolio에 값을 넣어주어야 하므로, readOnly = true를 쓰면 안됨.
    public PortfolioReturnDto getUserPortfolio(String nickName, String userEmail) {

        // Email로 현재 로그인 한 유저 찾기
        User loginUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 포트폴리오를 쓴 유저
        Optional<User> findUser = userRepository.findByNickName(nickName);

        // PortfolioReturnDto를 빌더를 통해 생성
        // 유저가 없는 경우, 모든 값이 null로 세팅된 아래의 DTO를 프론트에 넘길 것임.
        PortfolioReturnDto portfolioReturnDto = PortfolioReturnDto.builder()
                .isCreated(null)
                .nickName(null)
                .email(null)
                .web(null)
                .app(null)
                .game(null)
                .ai(null)
                .shortIntroduce(null)
                .introduce(null)
                .fileUrl(null)
                .viewCount(null)
                .build();

        // 유저가 있는 경우 정상적인 DTO 생성
        if (findUser.isPresent()) {
            // User를 통해 User가 갖고 있는 포트폴리오 찾기
            Optional<Portfolio> findUserPortfolio = portfolioRepository.findByUser(findUser.get());

            // 유저와 포트폴리오가 모두 있는 경우
            if (findUserPortfolio.isPresent()) {

                // 포트폴리오를 조회한 사람(loginUser)이 포트폴리오 작성자(findUser)가 아니어야 조회수 + 1
                if (!loginUser.getId().equals(findUser.get().getId())) {
                    // 단, 해당 유저가 해당 포트폴리오를 방문한 적 없을 때에만 viewCount를 새로 만들어 저장.
                    if (viewCountPortfolioRepository.findByPortfolio_IdAndUser_Id(findUserPortfolio.get().getId(), loginUser.getId()).isEmpty()) {
                        ViewCountPortfolio viewCountPortfolio = ViewCountPortfolio.builder()
                                .user(loginUser)
                                .portfolio(findUserPortfolio.get())
                                .build();

                        viewCountPortfolioRepository.save(viewCountPortfolio);
                    }
                }

                Optional<Integer> viewCountOptional = viewCountPortfolioRepository.countByPortfolio_Id(findUserPortfolio.get().getId());

                Integer viewCount = viewCountOptional.orElse(0); // 조회수 값이 없으면 0을 사용

                // PortfolioReturnDto를 빌더를 통해 생성
                portfolioReturnDto = PortfolioReturnDto.builder()
                        .isCreated(true)
                        .nickName(findUser.get().getNickName())
                        .email(findUser.get().getEmail())
                        .web(findUserPortfolio.get().getWeb())
                        .app(findUserPortfolio.get().getApp())
                        .game(findUserPortfolio.get().getGame())
                        .ai(findUserPortfolio.get().getAi())
                        .shortIntroduce(findUserPortfolio.get().getShortIntroduce())
                        .introduce(findUserPortfolio.get().getIntroduce())
                        .fileUrl(findUserPortfolio.get().getFileUrl())
                        .viewCount(viewCount)
                        .build();
            }
            // 유저는 있는데 포트폴리오가 없는 경우
            else {
                portfolioReturnDto = PortfolioReturnDto.builder()
                        .isCreated(false)
                        .nickName(findUser.get().getNickName())
                        .email(findUser.get().getEmail())
                        .web(null)
                        .app(null)
                        .game(null)
                        .ai(null)
                        .shortIntroduce(null)
                        .introduce(null)
                        .fileUrl(null)
                        .viewCount(null)
                        .build();
            }
        }

        return portfolioReturnDto;
    }

     /*
    ############################## LOGIC FOR PORFOLIOCARD ###########################################
     */


//    public boolean ToBoolean(Integer interest){
//        if(interest > 0) return true;
//        else if(interest == null) return false; //필요한가?
//        else return false;
//    }
//
//
//    /*
//    private String nickName;
//    private String email;
//    private Integer web;
//    private Integer app;
//    private Integer game;
//    private Integer ai;
//    private String shortIntroduce;
//     */
//
//    @EntityGraph(attributePaths = "user")
//    @Transactional(readOnly = true)
//    public List<PortfolioCardDto> getPortfolioCard(){
//
//        List<Portfolio> portfolios = portfolioRepository.findAll();
//
//        List<PortfolioCardDto> portfolioCardDtos = new ArrayList<>();
//
//
////        List<PortfolioCardDto> resultDto = portfolios.stream()
////                .map(data -> modelMapper.map(data, PortfolioCardDto.class))
////                .collect(Collectors.toList());
//
//
//        for(Portfolio portfolio : portfolios){
//
////            User user = userRepository.findById(portfolio.getUser().getId()).get();
//            User user = portfolio.getUser();
//
//            PortfolioCardDto portfolioDto = PortfolioCardDto.builder()
//                    .nickName(user.getNickName())
//                    .email(user.getEmail())// user = posts.getUser()
//                    .shortIntroduce(portfolio.getShortIntroduce())
//                    .web(portfolio.getWeb())
//                    .app(portfolio.getApp())
//                    .ai(portfolio.getAi())
//                    .game(portfolio.getGame())
//                    .build();
//
//            portfolioCardDtos.add(portfolioDto);
//        }
//
//
//
//
//            /*
//            private String nickName;
//            private String email;
//            private String shortIntroduce;
//             */
//
//
//
//        return portfolioCardDtos;
//
//    }



    /*
    ################################################################################
     */
    @Transactional(readOnly = true) //읽기 전용
    public Page<PortfolioCardDto> getCards(List<String> selectedBanners, String sortOption, String searchTerm, Pageable pageable) {

        QPortfolio portfolios = QPortfolio.portfolio;
        QUser users = QUser.user;
        QViewCountPortfolio viewCountPortfolio = QViewCountPortfolio.viewCountPortfolio;

        //System.out.println("pageable.getOffset() = " + pageable.getOffset());
        //System.out.println("pageable.getPageSize() = " + pageable.getPageSize());
        //System.out.println("searchTerm = " + searchTerm);
        //System.out.println("searchTerm = " + searchTerm.getClass());

        BooleanExpression bannerConditions = buildBannerConditions(portfolios, selectedBanners);
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
                    .build();

            portfolioCardDtos.add(cardDto);     // 컬렉션에 추가
        }

        return new PageImpl<>(portfolioCardDtos, pageable, total); // 동적쿼리의 결과를 반환

    }



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




}
