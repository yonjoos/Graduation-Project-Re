package PickMe.PickMeDemo.entity;

import PickMe.PickMeDemo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class UserTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    UserRepository userRepository;
    //생성일자, 수정일자를 값타입으로 멤버에 넣어놓고 확인하는 테스트
    @Test
    public void JpaEventBaseEntity() throws Exception {

//        //given
//        User user = userRepository.findByEmail("user@gmail.com").get(); //db에 미리 저장되어있는 유저를 찾아옴
//
//
//        //Thread.sleep(100); //
//        user.setUserName("user2"); //이름 수정
//
//        em.flush(); //업데이트 됨
//        em.clear(); //영속성 컨텍스트 비움
//
//        //생성일자와 수정일자가 달라야함
//        //생성자와 수정자는 uuid값으로 들어감

    }

}