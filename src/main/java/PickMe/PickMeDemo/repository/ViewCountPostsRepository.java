package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.ViewCountPosts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewCountPostsRepository extends JpaRepository<ViewCountPosts, Long> {

    // Id로 해당 게시물 조회 수 카운트 후 반환
    Optional<Integer> countByPosts_Id(Long postId);

    // 게시물 Id와 user Email로 해당 유저가 해당 게시물에 방문한 적이 있는지 찾기
    Optional<ViewCountPosts> findByPosts_IdAndUser_Id(Long projectId, Long userId);
}
