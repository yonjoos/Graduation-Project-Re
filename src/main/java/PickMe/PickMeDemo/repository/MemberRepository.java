package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 컴포넌트 스캔의 대상
@RequiredArgsConstructor
public class MemberRepository {

//    @PersistenceContext     // 이 애노테이션을 통해
//    private EntityManager em;   // 스프링이 EntityManager를 만들어서 주입해줌

    // @RequiredArgsConstructor를 사용할 것이므로 final 붙은 변수 사용
    private final EntityManager em; // @RequiredArgsConstructor로 인해 생성자가 자동으로 생김.

    public void save(Member member) {
        em.persist(member);
    }
}
