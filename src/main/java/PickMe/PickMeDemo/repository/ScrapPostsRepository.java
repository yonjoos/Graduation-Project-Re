package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.ScrapPosts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapPostsRepository extends JpaRepository<ScrapPosts, Long> {

    Optional<ScrapPosts> findByUser_IdAndPosts_Id(Long userId, Long postsId);
}
