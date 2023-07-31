package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.domain.Member;
import PickMe.PickMeDemo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 읽기 전용에서는 속도 향상을 위해 readOnly = true를 가급적 넣어주기. 기본적으로 readOnly = true가 다 먹힘
@RequiredArgsConstructor    // final이 붙은 애들을 갖고 생성자를 만들어줌. 의존관계 주입에 필수적!
public class MemberService {

    private final MemberRepository memberRepository;

    // 얘는 @Transactional을 따로 적어주었기 때문에 얘가 우선되어 데이터 변경이 가능함.
    @Transactional  // springframework의 애노테이션을 쓰는게 더 좋음. jakarta 아님! 쓰기의 경우, readOnly = true를 넣으면 안됨! (데이터 변경이 안되기 때문)
    public Long join(Member member) {
        memberRepository.save(member);
        return member.getId();  // ID라도 리턴을 해주어야 뭐라도 들어있다는 사실을 확인할 수 있음
    }
}
