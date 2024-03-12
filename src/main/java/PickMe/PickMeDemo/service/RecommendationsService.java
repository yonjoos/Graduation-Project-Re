package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PortfolioCardRecommendationDto;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import PickMe.PickMeDemo.repository.ViewCountPortfolioRepository;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class RecommendationsService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final ViewCountPortfolioRepository viewCountPortfolioRepository;

    private final JPAQueryFactory queryFactory;


    // 일단은 중간 발표용 코사인 유사도 값이 포함된 PortfolioCardRecommendationDto를 사용.
    // 중간 발표 이후로는 PortfolioCardDto로 돌려놓기!!
    @Transactional(readOnly = true)
    public List<PortfolioCardRecommendationDto> getRecommend(final String email, final String type){


        User user = null; // 추천을 받을 회원(나)
        Integer[] usersInterest = null; // 유저(나)의 관심사 벡터

        // 추천 받을 회원(나)를 찾기
        try{
            user = userRepository.findByEmail(email)
                    .orElseThrow(()-> new IllegalStateException("There is no such user"));

        }catch(IllegalStateException e){
            System.out.println("User가 없어요");
        }

        // 나의 관심사 벡터 가져오기
        Optional<Portfolio> userPortfolioOptional = portfolioRepository.findByUser(user);

        if (userPortfolioOptional.isPresent()) {
            usersInterest = userPortfolioOptional.get().getVector();

            /*
        >>> STEP 2 : 다른 유저들의 포폴 추출 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         */

            //이 기간 안에 있는 : [startDate, currentTime]
            final LocalDateTime currentTime = LocalDateTime.now();
            final LocalDateTime startDate = currentTime.minusDays(200); //200일 전부터 지금까지
            StartEnd startEnd = new StartEnd(startDate, currentTime); // 현재 시간부터 200일 전까지 그 사이의 기간 설정


            // 포폴 추출
            final List<Portfolio> portfolioList = findPortfolioByLastAccessDate(user, startEnd);

        /*
        ####### 포트폴리오, 유저와 사용자간의 유사를 매핑하기 위해 클래스를 새로 선언 #########
        class PairedUsersPortfolio 의 attributes
            1. Portfolio portfolio
            2. Double similarity
         */

            // 즉, 8명 뽑은 유저의 'portfolio'를 PairedUsersPortfolio의 속성인 portfolio에 세팅
            List<PairedUsersPortfolio> portfolios = portfolioList.stream()
                    .map(PairedUsersPortfolio::new)
                    .collect(Collectors.toList());

            // 사용자와 다른 유저의 유사도를 계산해서 유저의 portfolio와 매핑
            // PairedUsersPortfoliodml similarity attribute 할당
            // 즉, 8명 뽑은 유저의 'similarity'값을 PairedUsersPortfolio의의 속성인 similarity에 세팅
            for(PairedUsersPortfolio pair :portfolios){
                pair.setSimilarity(usersInterest, type);
            }


        /*
        >>> STEP 3 : 유사도 순위 매기기 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         */

            //순위 매기기 - step 3.1 : 유사도 정렬하기
            //List<PairedUsersPortfolio> portfolios, 3개의 attributes been sorted
            sortPortfolios(portfolios);

            //인덱스 0, 1, 2 만 추출
            List<PairedUsersPortfolio> top3 = portfolios.subList(0,3);


        /*
        >>> STEP 4 : DTO 변환 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         */
            // 최종 반환값, List<PortfolioCardDto> dtos
            // 일단은 중간 발표용 코사인 유사도 값이 포함된 PortfolioCardRecommendationDto를 사용.
            // 중간 발표 이후로는 PortfolioCardDto로 돌려놓기!!
            List<PortfolioCardRecommendationDto> dtos = new ArrayList<>();

            // 상위 3개의 데이터만 처리하기 위한 변수
            for(PairedUsersPortfolio pair : top3){

                User u = pair.portfolio.getUser();
                String nickName = u.getNickName();
                String url = u.getImageUrl(); // 프사 주소

                Integer views = viewCountPortfolioRepository.countByPortfolio_Id(pair.portfolio.getId()).orElse(null);
                Double cosine = pair.similarity;
                int returnCosine = (int) (cosine * 100.0);
                PortfolioCardRecommendationDto cardDto = PortfolioCardRecommendationDto.builder()
                        .nickName(nickName)
                        .web(pair.portfolio.getWeb())
                        .app(pair.portfolio.getApp())
                        .game(pair.portfolio.getGame())
                        .ai(pair.portfolio.getAi())
                        .shortIntroduce(pair.portfolio.getShortIntroduce())
                        .viewCount(views)
                        .cosineSimilarity(returnCosine)
                        .imageUrl(url)
                        .build();

                dtos.add(cardDto);
            }

            return dtos;
        } else {
            return null;
        }
    }



    /*
    #################################### 내부 로직 함수 ###########################################################
    #################################### 내부 로직 함수 ###########################################################
    #################################### 내부 로직 함수 ###########################################################
     */


    /*
    ========================================================================
    calculateSimilarity() : 유사도 계산, Double 반환
                ARGUMENTS
                    - Integer[] userInterest : 나의 관심사 백터
                    - Integer[] interest : 비교대상 관심사 벡터
     */
    private Double calculateSimilarity(final Integer[] userInterest, final Integer[] interest){

        if (userInterest.length != interest.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        Double dotProduct = 0.0;
        Double magnitudeUser = 0.0;
        Double magnitudeOtherUser = 0.0;

        for (int i = 0; i < userInterest.length; i++) {
            dotProduct += userInterest[i] * interest[i];
            magnitudeUser += Math.pow(userInterest[i], 2);
            magnitudeOtherUser += Math.pow(interest[i], 2);
        }

        magnitudeUser = Math.sqrt(magnitudeUser);
        magnitudeOtherUser = Math.sqrt(magnitudeOtherUser);

        if (magnitudeUser == 0 || magnitudeOtherUser == 0) {
            return 0.0; // To handle division by zero
        }

        return dotProduct / (magnitudeUser * magnitudeOtherUser);
    }



    private Double calculateSimilarityDB(final Integer[] vectorA, final Integer[] vectorB){

        QVectorSimilarity vectorSimilarity = QVectorSimilarity.vectorSimilarity;

        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        boolean allZerosA = true;
        boolean allZerosB = true;

        for (int i = 0; i < vectorA.length; i++) {
            if (vectorA[i] != 0) {
                allZerosA = false;
                break;
            }
        }

        for (int i = 0; i < vectorB.length; i++) {
            if (vectorB[i] != 0) {
                allZerosB = false;
                break;
            }
        }

        if (allZerosA || allZerosB) {
            return 0.0;
        }


        Double similarity = queryFactory.select(vectorSimilarity.similarity)
                .from(vectorSimilarity)
                .where(vectorSimilarity.vectorA.eq(vectorA), vectorSimilarity.vectorB.eq(vectorB))
                .fetchOne();


        if (similarity == null) {
            similarity = 0.0;
        }
        return similarity;

    }


    //포트폴리오 Entity 안에 User 변수가 있음(fetch_type.LAZY -> 나중에 fetchJoin()으로 가져올거임)
    //portfolio.getUser() 로 받을 수 있는 유저의 최근 접속일자가
    //startEnd에 속해있으면서
    //로그인한 user와 겹치지 않고
    //포트폴리오 값이 있으면
    //반환
    private List<Portfolio> findPortfolioByLastAccessDate(final User user, final StartEnd startEnd) {
        QPortfolio portfolios = QPortfolio.portfolio;

        List<Portfolio> result = queryFactory.selectFrom(portfolios)
                .leftJoin(portfolios.user).fetchJoin()
                .where(portfolios.user.lastAccessDate.between(startEnd.getStartDate(), startEnd.getEndDate())
                        .and(portfolios.user.ne(user))
                        .and(portfolios.isNotNull()))
                .orderBy(NumberExpression.random().asc())
                .limit(8)
                .fetch();

        return result;
    }


    //portfolio 를 similarity 가 큰 순으로 정렬하는 함수
    private void sortPortfolios(List<PairedUsersPortfolio> portfolios){
        Collections.sort(portfolios, new Comparator<PairedUsersPortfolio>() {
            @Override
            public int compare(PairedUsersPortfolio o1, PairedUsersPortfolio o2) {
                int result =  Double.compare(o2.similarity, o1.similarity); // 유사도를 비교해서 내림차순 정렬
                if (result != 0) { // 만약 두 portfolio에 대해 유사도가 다른 경우 -> 정상적으로 비교 로직 동작
                    return result;
                }

                // 만약 두 portfolio간에 유사도가 같은 경우, 두 포트폴리오를 지닌 유저의 마지막 접속 일자를 비교
                LocalDateTime lastAccessDate1 = o1.portfolio.getUser().getLastAccessDate();
                LocalDateTime lastAccessDate2 = o2.portfolio.getUser().getLastAccessDate();

                // 두 포트폴리오(사용자)가 둘 다 로그인을 한번이라도 한 적이 있다면,
                // 두 포트폴리오를 가진 사용자 중 더 최근에 로그인 한 사용자에 더 높은 우선순위를 부여
                if (lastAccessDate1 != null && lastAccessDate2 != null) {
                    return lastAccessDate2.compareTo(lastAccessDate1);
                }

                // 두 포트폴리오를 가진 사용자가 로그인 시간까지 똑같다면
                // 0을 반환
                return 0;

            }
        });
    }



    /*
    내부 class #####################################################################################################
    내부 class #####################################################################################################
    내부 class #####################################################################################################
     */

    //User, Portfolio, similarity 3개를 묶기위한 class
    private class PairedUsersPortfolio{
        private Portfolio portfolio;
        private Double similarity;


        private PairedUsersPortfolio(Portfolio portfolio){
            this.portfolio = portfolio;
        }

        //로그인한 사용자의 벡터(vectorA) 와
        //다른 유저의 벡터(vectorB)
        //사이의 유사도 구하는 함수
        // type 의 종류 :  {real-time, DB}
        private void setSimilarity(final Integer[] vectorA, String type){
            Integer[] vectorB = portfolio.getVector();
            Double sim = (type.equals("DB")) ? calculateSimilarityDB(vectorA, vectorB) : calculateSimilarity(vectorA, vectorB);

            this.similarity = sim;
        }

    }


    //기간을 나타내는 클래스
    //start : 시작일
    //end : 끝
    private class StartEnd{
        private LocalDateTime start;
        private LocalDateTime end;

        private StartEnd(LocalDateTime start, LocalDateTime end){
            this.start = start;
            this.end = end;
        }

        private LocalDateTime getStartDate(){
            return start;
        }
        private LocalDateTime getEndDate(){
            return end;
        }

    }


}
