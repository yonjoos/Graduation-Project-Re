package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//회원 정보를 담고 있는 레포지토리 -> data jpa 인터페이스 사용
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickName(String nickName);

    List<User> findAll();
}