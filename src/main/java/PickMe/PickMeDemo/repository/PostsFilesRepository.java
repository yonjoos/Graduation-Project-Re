package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Posts;
import PickMe.PickMeDemo.entity.PostsFiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostsFilesRepository extends JpaRepository<PostsFiles, Long> {

    List<PostsFiles> findByPostsAndIsImageTrue(Posts posts);
    List<PostsFiles> findByPostsAndIsImageFalse(Posts posts);

    List<PostsFiles> findByPosts(Posts posts);
}
