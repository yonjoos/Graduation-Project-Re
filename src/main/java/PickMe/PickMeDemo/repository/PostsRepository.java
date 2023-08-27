package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.entity.Posts;
import PickMe.PickMeDemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.query.Param;


import org.springframework.data.querydsl.QuerydslPredicateExecutor;







import java.util.List;
import java.util.Optional;

public interface PostsRepository extends JpaRepository<Posts, Long>, QuerydslPredicateExecutor<Posts> {

    // User와 PostType을 통해 특정 게시물 단건 조회
    Optional<Posts> findByUserAndPostType(User user, PostType postType);

    // ProjectId와 User_Email을 통해 조회
    Optional<Posts> findByIdAndUser_Email(@Param("projectId") Long projectId, @Param("userEmail") String userEmail);

    // PostType을 통해 게시물 리스트를 조회
    List<Posts> findByPostType(PostType postType);

    // Project Id와 게시물 타입을 통해 조회
    Optional<Posts> findByIdAndPostType(Long projectId, PostType postType);
}
