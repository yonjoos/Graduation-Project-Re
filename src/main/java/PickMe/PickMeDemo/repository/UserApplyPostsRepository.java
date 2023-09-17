package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Posts;
import PickMe.PickMeDemo.entity.UserApplyPosts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserApplyPostsRepository extends JpaRepository<UserApplyPosts, Long> {

    Optional<UserApplyPosts> findByPosts_Id(Long postsId);

    Optional<UserApplyPosts> findByUser_IdAndPosts_Id(Long userId, Long postsId);

    // Posts와 연관된 UserApplyPosts 엔티티의 개수를 반환하는 메서드. 승인된 유저(confirm == true)의 개수만 반환.
    Optional<Integer> countByPostsAndConfirmTrue(Posts posts);
}
