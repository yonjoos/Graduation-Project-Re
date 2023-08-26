package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PostsDto;
import PickMe.PickMeDemo.dto.PostsFormDto;
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

    public void uploadProjectPost(PostsFormDto postsFormDto, String userEmail) {
        uploadPost(postsFormDto, userEmail, PostType.PROJECT);
    }

    public void uploadStudyPost(PostsFormDto postsFormDto, String userEmail) {
        uploadPost(postsFormDto, userEmail, PostType.STUDY);
    }


    // uploadProjectPost 함수와 uploadStudyPost 함수는 구조가 거의 동일하다.
    // 디비에 저장할 때 다른 부분은 오직 PostType이다.
    // 따라서 두 함수의 저장 로직 중 겹치는 부분을 따로 함수로 떼어냈다.
    public void uploadPost(PostsFormDto postsFormDto, String userEmail, PostType postType) {
        Optional<User> findUser = userRepository.findByEmail(userEmail);

        User user = findUser.orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts posts = Posts.builder()
                .user(user)
                .postType(postType)
                .title(postsFormDto.getTitle())
                .recruitmentCount(postsFormDto.getRecruitmentCount())
                .content(postsFormDto.getContent())
                .promoteImageUrl(postsFormDto.getPromoteImageUrl())
                .fileUrl(postsFormDto.getFileUrl())
                .endDate(postsFormDto.getEndDate())
                .build();

        Posts savedPosts = postsRepository.save(posts);

        Category category = Category.builder()
                .posts(savedPosts)
                .web(postsFormDto.getPostType().contains("Web"))
                .app(postsFormDto.getPostType().contains("App"))
                .game(postsFormDto.getPostType().contains("Game"))
                .ai(postsFormDto.getPostType().contains("AI"))
                .build();

        categoryRepository.save(category);
    }



    // 프로젝트 게시물 리스트 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})     // 페치 조인으로 조회할 대상 테이블
    public List<PostsListDto> getProjectList() {
        return getPostsList(PostType.PROJECT);
    }

    // 스터디 게시물 리스트 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})     // 페치 조인으로 조회할 대상 테이블
    public List<PostsListDto> getStudyList() {
        return getPostsList(PostType.STUDY);
    }

    // 프로젝트 리스트 조회와 스터디 리스트 조회의 공통 코드를 분리하여 메서드로 만듦
    private List<PostsListDto> getPostsList(PostType postType) {
        List<Posts> postsList = postsRepository.findByPostType(postType);   // 게시물의 타입에 맞는 게시물 찾기

        List<PostsListDto> postsListDtoList = new ArrayList<>();        // 빈 컬렉션 생성

        for (Posts posts : postsList) {
            Category category = posts.getCategory();        // posts라는 연결고리를 통해 연결고리로 접근
            User user = posts.getUser();                    // posts라는 연결고리를 통해 연결고리로 접근

            PostsListDto postsListDto = PostsListDto.builder()
                    .id(posts.getId())
                    .nickName(user.getNickName())   // user = posts.getUser()
                    .title(posts.getTitle())
                    .web(category.getWeb())     // category = posts.getCategory()
                    .app(category.getApp())
                    .game(category.getGame())
                    .ai(category.getAi())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();

            postsListDtoList.add(postsListDto);     // 컬렉션에 추가
        }

        return postsListDtoList;
    }



    // 프로젝트 단건 조회
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "category"})
    public PostsDto getProject(String userEmail, Long projectId) {

        Posts posts = postsRepository.findByIdAndPostType(projectId, PostType.PROJECT)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        PostsDto postsDto;

        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())와 동일하다면
        if (posts.getUser().getEmail().equals(userEmail) ) {
             postsDto = PostsDto.builder()
                     .writer(true)      // writer에 true를 리턴
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
        }
        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())가 아니라면
        else {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
        }

        return postsDto;
    }


    public PostsDto getStudy(String userEmail, Long studyId) {

        Posts posts = postsRepository.findByIdAndPostType(studyId, PostType.STUDY)
                .orElseThrow(() -> new AppException("게시물을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        PostsDto postsDto;

        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())와 동일하다면
        if (posts.getUser().getEmail().equals(userEmail)) {
            postsDto = PostsDto.builder()
                    .writer(true)      // writer에 true를 리턴
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
        }
        // 현재 조회한 사람(userEmail)이 게시물 작성자(posts.getUser().getEmail())가 아니라면
        else {
            postsDto = PostsDto.builder()
                    .writer(false)      // writer에 false를 리턴
                    .nickName(posts.getUser().getNickName())
                    .title(posts.getTitle())
                    .web(posts.getCategory().getWeb())
                    .app(posts.getCategory().getApp())
                    .game(posts.getCategory().getGame())
                    .ai(posts.getCategory().getAi())
                    .content(posts.getContent())
                    .promoteImageUrl(posts.getPromoteImageUrl())
                    .fileUrl(posts.getFileUrl())
                    .recruitmentCount(posts.getRecruitmentCount())
                    .endDate(posts.getEndDate())
                    .build();
        }

        return postsDto;
    }
}

