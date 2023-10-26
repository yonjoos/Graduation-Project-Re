package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PortfolioCardDto;
import PickMe.PickMeDemo.dto.PortfolioDto;
import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.QUser;
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

    // User 테이블에서 last_access_date가 특정 날짜 이상인 유저 중, 랜덤 10명 뽑기 + 포폴도 함께 가져오기

    // 10명의 포폴에서, 관심사를 web app game ai 순으로 string으로 연결하기

    // 코사인 유사도 계산하기. (관심사, 유저 닉네임)을 pair로 만들어 계산하기

    // 코사인 유사도를 내림차순으로 정렬, 같은 값에 대해 후, pair의 닉네임을 활용해 상위 3개의 프로필 가져오기

    // 일치하는 상대방의 관심사를 바탕으로 해당 프로필 찾아 DTO에 담아 리턴하기

    // 날아가는 쿼리 수가 상당할지도??


    @Transactional(readOnly = true)
    public List<PortfolioCardDto> getRecommend(final String email){

        /*
        >> STEP 1 : 로그인한 유저(나) 찾기 <<<
         */

        final User user = userRepository.findByEmail(email).get();

        //유저의 관심사 벡터, 'usersIneterest'
        final Integer[] usersInterest = portfolioRepository.findByUser(user).get().getVector();


        /*
        >>> STEP 2 : 다른 유저 추출 <<<
         */

        //이 기간 안에 있는 : [startDate, currentTime]
        final LocalDateTime currentTime = LocalDateTime.now();
        final LocalDateTime startDate = currentTime.minusDays(200); //200일 전부터 지금까지


        //유저들의
        final List<User> usersInDuration = findUsersByLastAccessDate(user, startDate, currentTime);

        //포트폴리오를 찾아서 Id랑 pair 해줌
        List<Pair<Long, Portfolio>> pairedIdPortfolio = findUsersPortfolio(usersInDuration);



        /*
        >>> STEP 3 : 유사도 순위 매기기 <<<
         */

        //순위 매기기 - step 3.1 : 유사도 정렬하기
        final List<Pair<Long, Double>> pairedIdInterests = rankSimilarity(usersInterest, pairedIdPortfolio, usersInDuration);


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
                                                   final List<User> users){


        //최종 반환값, 'pairedSimilarities'
        List<Pair<Long, Double>> pairedSimilarities = new ArrayList<>();
        for(Pair<Long, Portfolio> pair : pairedIdPortfolio){

            Integer[] interest = pair.getSecond().getVector();
            Double similarity = calculateSimilarity(userInterest, interest);

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


    /*
    ========================================================================
    findUserByLastAccessDate() : startDate ~ currentTime 사이에 로그인 한, user가 아닌 회원 반환
            ARGUMENT
                - user : User 이 유저가 아닌 유저를 반환
     */
    @Transactional(readOnly = true)
    private List<User> findUsersByLastAccessDate(final User user, final LocalDateTime startDate, final LocalDateTime endDate) {
        QUser users = QUser.user;

        JPQLQuery<User> query = queryFactory.selectFrom(users)
                .where(users.lastAccessDate.between(startDate, endDate)
                        .and(users.ne(user)))
                .orderBy(NumberExpression.random().asc())
                .limit(10);

        return query.fetch();
    }
}
