package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.UserApplyPosts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserApplyPostsRepository extends JpaRepository<UserApplyPosts, Long> {
}
