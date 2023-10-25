package PickMe.PickMeDemo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RecommendationsService {

    // User 테이블에서 last_access_date가 특정 날짜 이상인 유저 중, 랜덤 10명 뽑기 + 포폴도 함께 가져오기

    // 10명의 포폴에서, 관심사를 web app game ai 순으로 string으로 연결하기

    // 코사인 유사도 계산하기. (관심사, 유저 닉네임)을 pair로 만들어 계산하기

    // 코사인 유사도를 내림차순으로 정렬, 같은 값에 대해 후, pair의 닉네임을 활용해 상위 3개의 프로필 가져오기

    // 일치하는 상대방의 관심사를 바탕으로 해당 프로필 찾아 DTO에 담아 리턴하기

    // 날아가는 쿼리 수가 상당할지도??
}
