package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PortfolioCardDto;
import PickMe.PickMeDemo.dto.PortfolioDto;
import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.QUser;
import PickMe.PickMeDemo.entity.QVectorSimilarity;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.UserRepository;
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

    private final JPAQueryFactory queryFactory;

    // User 테이블에서 last_access_date가 특정 날짜 이상인 유저 중, 랜덤 10명 뽑기 + 포폴도 함께 가져오기

    // 10명의 포폴에서, 관심사를 web app game ai 순으로 string으로 연결하기

    // 코사인 유사도 계산하기. (관심사, 유저 닉네임)을 pair로 만들어 계산하기

    // 코사인 유사도를 내림차순으로 정렬, 같은 값에 대해 후, pair의 닉네임을 활용해 상위 3개의 프로필 가져오기

    // 일치하는 상대방의 관심사를 바탕으로 해당 프로필 찾아 DTO에 담아 리턴하기

    // 날아가는 쿼리 수가 상당할지도??

    @Transactional(readOnly = true)
    public List<PortfolioCardDto> getRecommend(String email){

        User user = userRepository.findByEmail(email).get();

        //유저의 관심사 벡터
        Integer[] usersInterest = portfolioRepository.findByUser(user).get().getVector();


        //이 기간 안에 있는 : [현재로부터 200일 전, 현재]
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startDate = currentTime.minusDays(200);

        //유저들의
        List<User> usersInDuration = findUsersByLastAccessDate(user, startDate, currentTime);



        //포트폴리오를 찾아서
        List<Pair<Long, Portfolio>> pairedIdPortfolio = findUsersPortfolio(usersInDuration);

        List<Pair<Long, Double>> pairedIdInterests = rankSimilarity(usersInterest, pairedIdPortfolio, usersInDuration);

        //List<Pair<Long, Double>> firstThreeElements = pairedIdInterests.subList(0, 3);

        List<Pair<Long, Portfolio>> sortedPairedIdPortfolio = sortPortfoliosById(pairedIdPortfolio, pairedIdInterests);


        List<PortfolioCardDto> dtos = new ArrayList<>();
        for(Pair<Long, Portfolio> pair : sortedPairedIdPortfolio){

            String nickName = "";
            for(User u : usersInDuration){
                if(u.getId() == pair.getFirst()){
                    nickName = u.getNickName();
                    break;
                }
            }

            PortfolioCardDto cardDto = PortfolioCardDto.builder()
                    .nickName(nickName)   // user = posts.getUser()
                    .web(pair.getSecond().getWeb())     // category = posts.getCategory()
                    .app(pair.getSecond().getApp())
                    .game(pair.getSecond().getGame())
                    .ai(pair.getSecond().getAi())
                    .shortIntroduce(pair.getSecond().getShortIntroduce())
                    .build();

            dtos.add(cardDto);
        }

        for(PortfolioCardDto p : dtos){
            System.out.println("DTO Id: "+ p.getNickName());
            System.out.println();
        }

        return dtos;
    }

    public List<Pair<Long, Portfolio>> sortPortfoliosById(List<Pair<Long, Portfolio>> pairedIdPortfolio,
                                                          List<Pair<Long, Double>> pairedIdInterests){
        List<Pair<Long, Portfolio>> sorted = pairedIdInterests.stream()
                .map(pair -> pairedIdPortfolio.stream()
                        .filter(portfolioPair -> portfolioPair.getFirst().equals(pair.getFirst()))
                        .findFirst()
                        .orElse(null)) // You can handle cases where there's no match
                .collect(Collectors.toList());

        return sorted;
    }



    public List<Pair<Long, Portfolio>> findUsersPortfolio(List<User> users){

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

    public List<Pair<Long, Double>> rankSimilarity(final Integer[] userInterest,
                                                   final List<Pair<Long, Portfolio>> pairedIdPortfolio,
                                                   final List<User> users){

        List<Pair<Long, Double>> pairedSimilarities = new ArrayList<>();
        for(Pair<Long, Portfolio> pair : pairedIdPortfolio){

            Integer[] interest = pair.getSecond().getVector();
            Double similarity = calculateSimilarity(userInterest, interest);

            Pair<Long, Double> pairedIdSimilarity = Pair.of(pair.getFirst(), similarity);
            pairedSimilarities.add(pairedIdSimilarity);
        }

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

    public Double calculateSimilarity(final Integer[] userInterest, final Integer[] interest){

        QVectorSimilarity vectorSimilarity = QVectorSimilarity.vectorSimilarity;

        if (userInterest.length != interest.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }


        double similarity = queryFactory.select(vectorSimilarity.similarity)
                .from(vectorSimilarity)
                .where(vectorSimilarity.vectorA.eq(userInterest), vectorSimilarity.vectorB.eq(interest))
                .fetchOne();



        return similarity;
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByLastAccessDate(User user, LocalDateTime startDate, LocalDateTime endDate) {
        QUser users = QUser.user;

        JPQLQuery<User> query = queryFactory.selectFrom(users)
                .where(users.lastAccessDate.between(startDate, endDate)
                        .and(users.ne(user)))
                .limit(10);;

        return query.fetch();
    }
}
