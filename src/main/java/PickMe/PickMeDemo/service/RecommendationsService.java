package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PortfolioCardRecommendationDto;
import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.QUser;
import PickMe.PickMeDemo.entity.QVectorSimilarity;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import PickMe.PickMeDemo.repository.ViewCountPortfolioRepository;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
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


        User user = null;
        Integer[] usersInterest = null; //유저의 관심사 벡터

        try{
            user = userRepository.findByEmail(email).orElseThrow(()-> new IllegalStateException("There is no such user"));

        }catch(IllegalStateException e){
            System.out.println("User가 없어요");
        }

        try{
            usersInterest = portfolioRepository.findByUser(user)
                    .orElseThrow(()-> new IllegalStateException("There is no such portfolio")).getVector();
        }catch(IllegalStateException e){
            System.out.println("portfolio가 없어요");
        }

        /*
        >>> STEP 2 : 다른 유저 추출 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         */

        //이 기간 안에 있는 : [startDate, currentTime]
        final LocalDateTime currentTime = LocalDateTime.now();
        final LocalDateTime startDate = currentTime.minusDays(200); //200일 전부터 지금까지
        StartEnd startEnd = new StartEnd(startDate, currentTime);


        //유저들의
        final List<User> usersInDuration = findUsersByLastAccessDate(user, startEnd);


        /*
        ####### 유저, 포트폴리오, 유저와 사용자간의 유사도 3개를 매핑하기 위해 클래스를 새로 선언 #########
        class PairedUsersPortfolio 의 attributes
            1. User user
            2. Portfolio portfolio
            3. Double similarity
         */

        //포트폴리오를 찾아서 user랑 pair 해줌
        // PairedUsersPortfolio의 User와 Portfolio attributes를 할당
        List<PairedUsersPortfolio> portfolios = findUsersPortfolio(usersInDuration);


        //사용자와 다른 유저의 유사도를 계산해서 유저의 portfolio와 매핑
        //PairedUsersPortfoliodml similarity attribute 할당
        for(PairedUsersPortfolio pair :portfolios){
            pair.setSimilarity(usersInterest, type);
        }



        /*
        >>> STEP 3 : 유사도 순위 매기기 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         */

        //순위 매기기 - step 3.1 : 유사도 정렬하기
        //List<PairedUsersPortfolio> portfolios, 3개의 attributes been sorted
        sortPortfolios(portfolios);



        /*
        >>> STEP 4 : DTO 변환 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
         */
        // 최종 반환값, List<PortfolioCardDto> dtos
        // 일단은 중간 발표용 코사인 유사도 값이 포함된 PortfolioCardRecommendationDto를 사용.
        // 중간 발표 이후로는 PortfolioCardDto로 돌려놓기!!
        List<PortfolioCardRecommendationDto> dtos = new ArrayList<>();

        // 상위 3개의 데이터만 처리하기 위한 변수
        for(int i = 0; i < 3; i++){

            PairedUsersPortfolio pair = portfolios.get(i);

            String nickName = pair.user.getNickName();

            Integer views = viewCountPortfolioRepository.countByPortfolio_Id(pair.portfolio.getId()).orElse(null);
            Double cosine = pair.similarity;

            PortfolioCardRecommendationDto cardDto = PortfolioCardRecommendationDto.builder()
                    .nickName(nickName)
                    .web(pair.portfolio.getWeb())
                    .app(pair.portfolio.getApp())
                    .game(pair.portfolio.getGame())
                    .ai(pair.portfolio.getAi())
                    .shortIntroduce(pair.portfolio.getShortIntroduce())
                    .viewCount(views)
                    .cosineSimilarity(cosine)
                    .build();

            dtos.add(cardDto);
        }

        return dtos;
    }



    /*
    #################################### 내부 로직 함수 ###########################################################
    #################################### 내부 로직 함수 ###########################################################
    #################################### 내부 로직 함수 ###########################################################
     */


    //유저의 포트폴리오를 반환
    //input : List of users
    //out : List of PairedUsersPortfolio
    /*
        PairedUsersPortfolio 클래스의 생성자 생김새
        PairedUsersPortfolio(User user, Portfolio portfolio, Double similarity)
     */
    private List<PairedUsersPortfolio> findUsersPortfolio(final List<User> users){

        List<PairedUsersPortfolio> pairedIdPortfolios = new ArrayList<>();
        for(User u : users){
            Long userId = u.getId();
            //유저의 포트폴리오
            Portfolio portfolio = portfolioRepository.findByUser(u).orElse(null);
            if(portfolio != null){
                PairedUsersPortfolio usersPortfolio = new PairedUsersPortfolio(u, portfolio);
                pairedIdPortfolios.add(usersPortfolio);
            }
        }

        return pairedIdPortfolios;
    }




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


    /*
    ========================================================================
    findUserByLastAccessDate() : startDate ~ currentTime 사이에 로그인 한, user가 아닌 회원 반환
            ARGUMENT
                - user : User 이 유저가 아닌 유저를 반환
     */
    private List<User> findUsersByLastAccessDate(final User user, final StartEnd startEnd) {
        QUser users = QUser.user;

        JPQLQuery<User> query = queryFactory.selectFrom(users)
                .where(users.lastAccessDate.between(startEnd.getStartDate(), startEnd.getEndDate())
                        .and(users.ne(user))
                        .and(users.portfolio.isNotNull()))
                .orderBy(NumberExpression.random().asc())
                .limit(8);

        return query.fetch();
    }

    private void sortPortfolios(List<PairedUsersPortfolio> portfolios){
        Collections.sort(portfolios, new Comparator<PairedUsersPortfolio>() {
            @Override
            public int compare(PairedUsersPortfolio o1, PairedUsersPortfolio o2) {
                int result =  Double.compare(o2.similarity, o1.similarity);
                if (result != 0) {
                    return result;
                }

                LocalDateTime lastAccessDate1 = portfolios.stream()
                        .filter(p->p.user.equals(o1.user))
                        .findFirst()
                        .map(p->p.user.getLastAccessDate())
                        .orElse(null);


                LocalDateTime lastAccessDate2 = portfolios.stream()
                        .filter(p->p.user.equals(o2.user))
                        .findFirst()
                        .map(p->p.user.getLastAccessDate())
                        .orElse(null);

                if (lastAccessDate1 != null && lastAccessDate2 != null) {
                    return lastAccessDate2.compareTo(lastAccessDate1);
                }

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
        private User user;
        private Portfolio portfolio;
        private Double similarity;


        private PairedUsersPortfolio(User user, Portfolio portfolio){
            this.user = user;
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
