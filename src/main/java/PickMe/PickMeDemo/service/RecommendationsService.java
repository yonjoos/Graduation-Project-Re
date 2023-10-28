package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PortfolioCardDto;
import PickMe.PickMeDemo.dto.PortfolioDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sound.sampled.Port;
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


    @Transactional(readOnly = true)
    public List<PortfolioCardDto> getRecommend(final String email, final String type){


        User user = userRepository.findByEmail(email).get();

        //유저의 관심사 벡터
        Integer[] usersInterest = portfolioRepository.findByUser(user).get().getVector();


        /*
        >>> STEP 2 : 다른 유저 추출 <<<
         */

        //이 기간 안에 있는 : [startDate, currentTime]
        final LocalDateTime currentTime = LocalDateTime.now();
        final LocalDateTime startDate = currentTime.minusDays(200); //200일 전부터 지금까지
        StartEnd startEnd = new StartEnd(startDate, currentTime);


        //유저들의
        final List<User> usersInDuration = findUsersByLastAccessDate(user, startEnd);

        //포트폴리오를 찾아서 Id랑 pair 해줌
        List<Pair<Long, Portfolio>> pairedIdPortfolio = findUsersPortfolio(usersInDuration);
        int size = pairedIdPortfolio.size();

        while(size < 10){
            List<User> additionalUsers = findUsersAgain(usersInDuration, startEnd, 10);
            List<Pair<Long, Portfolio>> additionalPairedIdPortfolio = findUsersPortfolio(additionalUsers);
            pairedIdPortfolio.addAll(additionalPairedIdPortfolio);
            size = pairedIdPortfolio.size();
        }
        if(size > 10){
            pairedIdPortfolio = pairedIdPortfolio.subList(0, Math.min(10, pairedIdPortfolio.size()));
        }

        /*
        >>> STEP 3 : 유사도 순위 매기기 <<<
         */

        //순위 매기기 - step 3.1 : 유사도 정렬하기
        final List<Pair<Long, Double>> pairedIdInterests = rankSimilarity(usersInterest, pairedIdPortfolio, usersInDuration, type);


        //앞에 3명만 뽑는 기능은 일단 패쓰
        //List<Pair<Long, Double>> firstThreeElements = pairedIdInterests.subList(0, 3);


        //순위 매기기 - step 3.2 : 정렬된 유사도에 맞춰 Portfolio 정렬
        // Pair A : Pair<id, similarity> 정렬된 유사도
        // Pair B : Pair<id, portfolio> --> Pair A, B 두 개의 id의 순서가 같게 만들어줌
        // 결과적으로, Pair A를 정렬한 후, Pair A의 id에 맞춰 Pair B도 정렬하여, portfolio를 순서대로 반환
        final List<Pair<Long, Portfolio>> sortedPairedIdPortfolio = sortPortfoliosById(pairedIdPortfolio, pairedIdInterests);


        /*
        >>> STEP 4 : DTO 변환 <<<
         */

        //최종 반환값, List<PortfolioCardDto> dtos
        List<PortfolioCardDto> dtos = new ArrayList<>();
        for(Pair<Long, Portfolio> pair : sortedPairedIdPortfolio){

            String nickName = "";
            for(User u : usersInDuration){
                if(u.getId() == pair.getFirst()){
                    nickName = u.getNickName();
                    break;
                }
            }

            Integer views = viewCountPortfolioRepository.countByPortfolio_Id(pair.getSecond().getId()).orElse(null);

            PortfolioCardDto cardDto = PortfolioCardDto.builder()
                    .nickName(nickName)   // user = posts.getUser()
                    .web(pair.getSecond().getWeb())     // category = posts.getCategory()
                    .app(pair.getSecond().getApp())
                    .game(pair.getSecond().getGame())
                    .ai(pair.getSecond().getAi())
                    .shortIntroduce(pair.getSecond().getShortIntroduce())
                    .viewCount(views)
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



    /*
    ========================================================================
    sortPortfoliosById() : 첫 번째 arg의 id 순서에 맞춰 두 번째 arg의 index를 바꾸는(정렬하는) 함수
                ARGUMENTS
                    - List<Pair<Long, Portfolio>> pairedIdPortfolio : 정렬해야할 <id, 포트폴리오>
                    - List<Pair<Long, Double>> pairedIdInterests : 유사도순으로 정렬된 <id, 유사도>
                    - pairedIdInterests와 pairedIdPortfolio 의 id index 순서를 같게 만들어 반환
     */
    private List<Pair<Long, Portfolio>> sortPortfoliosById(List<Pair<Long, Portfolio>> pairedIdPortfolio,
                                                          List<Pair<Long, Double>> pairedIdInterests){
        List<Pair<Long, Portfolio>> sorted = pairedIdInterests.stream()
                .map(pair -> pairedIdPortfolio.stream()
                        .filter(portfolioPair -> portfolioPair.getFirst().equals(pair.getFirst()))
                        .findFirst()
                        .orElse(null)) // You can handle cases where there's no match
                .collect(Collectors.toList());

        return sorted;
    }



    private List<Pair<Long, Portfolio>> findUsersPortfolio(final List<User> users){

        List<Pair<Long, Portfolio>> pairedIdPortfolios = new ArrayList<>();
        for(User u : users){

            Long userId = u.getId();
            //유저의 포트폴리오
            Portfolio portfolio = portfolioRepository.findByUser(u).orElse(null);
            if(portfolio != null){
                Pair<Long, Portfolio> pairedIdPortfolio = Pair.of(userId, portfolio);
                pairedIdPortfolios.add(pairedIdPortfolio);
            }
        }

        return pairedIdPortfolios;
    }




    /*
    ========================================================================
    rankSimilarity() : 유저들의 순위에 따라 Id와 유사도 반환
                ARGUMENTS
                    - userdInterest : 나의 관심사 백터
                    - pairedPortfolio : Pair<Long id, Portfolio portfolio>, '아이디 - 포트폴리오' 묶음
                    - usersInDuration : List<Users> 추출된 유저들
     */
    private List<Pair<Long, Double>> rankSimilarity(final Integer[] userInterest,
                                                   final List<Pair<Long, Portfolio>> pairedIdPortfolio,
                                                   final List<User> users,
                                                    String type){


        //최종 반환값, 'pairedSimilarities'
        List<Pair<Long, Double>> pairedSimilarities = new ArrayList<>();
        for(Pair<Long, Portfolio> pair : pairedIdPortfolio){

            Integer[] interest = pair.getSecond().getVector();
            Double similarity = (type.equals("DB")) ? calculateSimilarityDB(userInterest, interest) : calculateSimilarity(userInterest, interest);


            Pair<Long, Double> pairedIdSimilarity = Pair.of(pair.getFirst(), similarity);
            pairedSimilarities.add(pairedIdSimilarity);
        }

        //Pair<id, similarity> 에서 similarity로 'List<> pairedSimilarities' 정렬
        Collections.sort(pairedSimilarities, new Comparator<Pair<Long, Double>>() {
            @Override
            public int compare(Pair<Long, Double> o1, Pair<Long, Double> o2) {
                int result = Double.compare(o2.getSecond(), o1.getSecond());

                if (result != 0) {
                    return result;
                }
                else {

                    LocalDateTime lastAccessDate1 = users.stream()
                            .filter(user -> user.getId().equals(o1.getFirst()))
                            .findFirst()
                            .map(User::getLastAccessDate)
                            .orElse(null);

                    LocalDateTime lastAccessDate2 = users.stream()
                            .filter(user -> user.getId().equals(o2.getFirst()))
                            .findFirst()
                            .map(User::getLastAccessDate)
                            .orElse(null);

                    if (lastAccessDate1 != null && lastAccessDate2 != null) {
                        return lastAccessDate2.compareTo(lastAccessDate1);
                    } else {
                        return 0;
                    }
                }
            }
        });

        return pairedSimilarities;
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



    private Double calculateSimilarityDB(final Integer[] userInterest, final Integer[] interest){

        QVectorSimilarity vectorSimilarity = QVectorSimilarity.vectorSimilarity;

        if (userInterest.length != interest.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        boolean user = true;
        boolean other = true;

        for(int i = 0; i < userInterest.length; i++){
            if(userInterest[i] != 0) {
                user = false;
                break;
            }
        }

        for(int i = 0; i < interest.length; i++){
            if(interest[i] != 0) {
                other = false;
                break;
            }
        }

        if(user && other) return 0.0;


        Double similarity = queryFactory.select(vectorSimilarity.similarity)
                .from(vectorSimilarity)
                .where(vectorSimilarity.vectorA.eq(userInterest), vectorSimilarity.vectorB.eq(interest))
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
    @Transactional(readOnly = true)
    private List<User> findUsersByLastAccessDate(final User user, final StartEnd startEnd) {
        QUser users = QUser.user;

        JPQLQuery<User> query = queryFactory.selectFrom(users)
                .where(users.lastAccessDate.between(startEnd.getStartDate(), startEnd.getEndDate())
                        .and(users.ne(user)))
                .orderBy(NumberExpression.random().asc())
                .limit(10);

        return query.fetch();
    }

    @Transactional(readOnly = true)
    private List<User> findUsersAgain(final List<User> user,
                                      final StartEnd startEnd,
                                      int amount) {
        QUser users = QUser.user;

        JPQLQuery<User> query = queryFactory.selectFrom(users)
                .where(users.lastAccessDate.between(startEnd.getStartDate(), startEnd.getEndDate())
                        .and(users.notIn(user)))
                .orderBy(NumberExpression.random().asc())
                .limit(amount);

        return query.fetch();
    }

    class StartEnd{
        private LocalDateTime start;
        private LocalDateTime end;

        public StartEnd(LocalDateTime start, LocalDateTime end){
            this.start = start;
            this.end = end;
        }

        public LocalDateTime getStartDate(){
            return start;
        }
        public LocalDateTime getEndDate(){
            return end;
        }

    }


}