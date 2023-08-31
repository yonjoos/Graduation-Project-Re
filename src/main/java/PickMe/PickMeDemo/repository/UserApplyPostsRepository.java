package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.UserApplyPosts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserApplyPostsRepository extends JpaRepository<UserApplyPosts, Long> {

    Optional<UserApplyPosts> findByPosts_Id(Long postsId);
}
