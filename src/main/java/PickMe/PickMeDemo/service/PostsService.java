package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PostFormDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.CategoryRepository;
import PickMe.PickMeDemo.repository.PostsRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostsService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final CategoryRepository categoryRepository;

    public void uploadProjectPost(PostFormDto postFormDto, UserDto userDto) {
        uploadPost(postFormDto, userDto, PostType.PROJECT);
    }

    public void uploadStudyPost(PostFormDto postFormDto, UserDto userDto) {
        uploadPost(postFormDto, userDto, PostType.STUDY);
    }


    // uploadProjectPost 함수와 uploadStudyPost 함수는 구조가 거의 동일하다.
    // 디비에 저장할 때 다른 부분은 오직 PostType이다.
    // 따라서 두 함수의 저장 로직 중 겹치는 부분을 따로 함수로 떼어냈다.
    public void uploadPost(PostFormDto postFormDto, UserDto userDto, PostType postType) {
        Optional<User> findUser = userRepository.findByEmail(userDto.getEmail());

        User user = findUser.orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts posts = Posts.builder()
                .user(user)
                .postType(postType)
                .title(postFormDto.getTitle())
                .recruitmentCount(postFormDto.getRecruitmentCount())
                .content(postFormDto.getContent())
                .promoteImageUrl(postFormDto.getPromoteImageUrl())
                .fileUrl(postFormDto.getFileUrl())
                .endDate(postFormDto.getEndDate())
                .build();

        Posts savedPosts = postsRepository.save(posts);

        Category category = Category.builder()
                .posts(savedPosts)
                .web(postFormDto.getPostType().contains("Web"))
                .app(postFormDto.getPostType().contains("App"))
                .game(postFormDto.getPostType().contains("Game"))
                .ai(postFormDto.getPostType().contains("AI"))
                .build();

        categoryRepository.save(category);
    }

}

