package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.entity.Posts;
import PickMe.PickMeDemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostsRepository extends JpaRepository<Posts, Long> {

    // User와 PostType을 통해 게시물 리스트를 조회
    List<Posts> findByUserAndPostType(User user, PostType postType);
}
