package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostsRepository extends JpaRepository<Posts, Long> {
}
