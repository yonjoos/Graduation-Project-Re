package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PostFormDto;
import PickMe.PickMeDemo.dto.PostsListDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.CategoryRepository;
import PickMe.PickMeDemo.repository.PostsRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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



    // 프로젝트 게시물 리스트 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public List<PostsListDto> getProjectList(UserDto userDto) {
        return getPostsList(userDto, PostType.PROJECT);
    }

    // 스터디 게시물 리스트 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public List<PostsListDto> getStudyList(UserDto userDto) {
        return getPostsList(userDto, PostType.STUDY);
    }

    // 공통 코드를 분리하여 메서드로 만듦
    private List<PostsListDto> getPostsList(UserDto userDto, PostType postType) {
        Optional<User> findUser = userRepository.findByEmail(userDto.getEmail());

        User user = findUser.orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        List<Posts> postsList = postsRepository.findByUserAndPostType(user, postType);

        List<PostsListDto> postsListDtoList = new ArrayList<>();

        for (Posts posts : postsList) {
            Category category = posts.getCategory();

            PostsListDto postsListDto = PostsListDto.builder()
                    .nickName(user.getNickName())
                    .title(posts.getTitle())
                    .web(category.getWeb())
                    .app(category.getApp())
                    .game(category.getGame())
                    .ai(category.getAi())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();

            postsListDtoList.add(postsListDto);
        }

        return postsListDtoList;
    }


    // 특정 프로젝트 게시물 조회
//    @Transactional(readOnly = true)
//    @EntityGraph(attributePaths = {"user", "category"})
//    public PostsListDto getProject(UserDto userDto) {
//        Optional<User> findUser = userRepository.findByEmail(userDto.getEmail());
//
//        User user = findUser.orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));
//
//        Posts posts = postsRepository.findByUserAndPostType(user, PostType.PROJECT)
//                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));
//
//        Category category = categoryRepository.findCategoryById(posts.getId())
//                .orElseThrow(() -> new AppException("카테고리를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
//
//        PostsListDto postsListDto = PostsListDto.builder()
//                .nickName(user.getNickName())
//                .title(posts.getTitle())
//                .web(category.getWeb())
//                .app(category.getApp())
//                .game(category.getGame())
//                .ai(category.getAi())
//                .recruitmentCount(posts.getRecruitmentCount())
//                .endDate(posts.getEndDate())
//                .build();
//
//        return postsListDto;
//    }


    // 특정 스터디 게시물 조회
//    @Transactional(readOnly = true)
//    @EntityGraph(attributePaths = "user")
//    public PostsListDto getStudy(UserDto userDto) {
//
//    }

}

