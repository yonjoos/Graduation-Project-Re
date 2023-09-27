package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.repository.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true) //읽기 전용
    public SearchResultDto getFilteredSearchLists(String searchTerm) {

        QPosts posts = QPosts.posts;
        QUser user = QUser.user;


        BooleanExpression projectTitleConditions = null; // 검색어 기반으로 프로젝트를 필터링할 때 쓰는 BooleanExpression 조건
        BooleanExpression studyTitleConditions = null; // 검색어 기반으로 스터디를 필터링할 때 쓰는 BooleanExpression 조건
        BooleanExpression userNicknameConditions = null; // 검색어 기반으로 유저 닉네임을 필터링할 때 쓰는 BooleanExpression 조건


//      검색어 문자열을 공백 기호 기준으로 다 split해서 배열로 만들고, 각 배열 요소에 담긴 키워드 조각들을 and한 결과가 게시물에 있으면 해당 게시물이 추출됨
        if (!searchTerm.isEmpty()) { // 만약 문자열이 공백이 아니라면
            String[] keywords = searchTerm.split("\\s+"); // 공백으로 분리한 검색어 배열 생성

            // 각 단어를 처리하여 BooleanExpression 조건 생성 (프로젝트)
            List<BooleanExpression> keywordConditionsForProject = Arrays.stream(keywords)
                    .map(keyword -> posts.title.lower().like("%" + keyword.toLowerCase() + "%")) // keyword가 포함된 프로젝트 title이 있으면 추출될 게시물로 선정
                    .collect(Collectors.toList()); // 각 키워드 조각들에 대해 title에 포함됨? 에 대한 조건을 모두 만들어 list로 만든다.

            projectTitleConditions = keywordConditionsForProject.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(null); // 모든 조건이 없을 경우 null로 설정

            // 공백 기준으로 나눈 키워드 조각들을 모두 and해서 가장 일치하는 프로젝트 제목 찾기
            if (projectTitleConditions != null) {
                projectTitleConditions = projectTitleConditions.and(posts.postType.eq(PostType.PROJECT));
            }

            // 각 단어를 처리하여 BooleanExpression 조건 생성 (스터디)
            List<BooleanExpression> keywordConditionsForStudy = Arrays.stream(keywords)
                    .map(keyword -> posts.title.lower().like("%" + keyword.toLowerCase() + "%")) // keyword가 포함된 스터디 title이 있으면 추출될 게시물로 선정
                    .collect(Collectors.toList()); // 각 키워드 조각들에 대해 title에 포함됨?  에 대한 조건을 모두 만들어 list로 만든다.

            studyTitleConditions = keywordConditionsForStudy.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(null); // 모든 조건이 없을 경우 null로 설정

            // 공백 기준으로 나눈 키워드 조각들을 모두 and해서 가장 일치하는 스터디 제목 찾기
            if (studyTitleConditions != null) {
                studyTitleConditions = studyTitleConditions.and(posts.postType.eq(PostType.STUDY));
            }

            // 각 단어를 처리하여 BooleanExpression 조건 생성 (유저)
            List<BooleanExpression> keywordConditionsForUser = Arrays.stream(keywords)
                    .map(keyword -> user.nickName.lower().like("%" + keyword.toLowerCase() + "%")) // keyword가 포함된 유저 닉네임이 있으면 추출될 게시물로 선정
                    .collect(Collectors.toList()); // 각 키워드 조각들에 대해 nickName에 포함됨?에 대한 조건을 모두 만들어 list로 만든다.

            // 공백 기준으로 나눈 키워드 조각들을 모두 and해서 가장 일치하는 유저 닉네임 찾기
            userNicknameConditions = keywordConditionsForUser.stream()
                    .reduce(BooleanExpression::and)
                    .orElse(null); // 모든 조건이 없을 경우 null로 설정



        }
        ///////////////////////////////////////////////////////////////////////////////////////////// 이제 쿼리를 실행

        // '데이터'를 가져오는 쿼리
        JPAQuery<Posts> queryForProject = queryFactory.selectFrom(posts);
        JPAQuery<Posts> queryForStudy = queryFactory.selectFrom(posts);
        JPAQuery<User> queryForUser = queryFactory.selectFrom(user);

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (projectTitleConditions != null) {
            queryForProject.where(projectTitleConditions)
                    .orderBy(posts.endDate.asc()) // 마감일자가 가까운 순으로 정렬
                    .limit(5); // 상위 5개만 가져오기
        }

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (studyTitleConditions != null) {
            queryForStudy.where(studyTitleConditions)
                    .orderBy(posts.endDate.asc()) // 마감일자가 가까운 순으로 정렬
                    .limit(5); // 상위 5개만 가져오기
        }

        // 근데 검색어 관련 조건이 null이 아니라면, 검색어 관련 조건이 해당 쿼리문에 where절로 한번 더 엮임
        if (userNicknameConditions != null) {
            queryForUser.where(userNicknameConditions)
                    .orderBy(user.lastAccessDate.desc()) // 로그인 시간이 최근과 가까울 수록 정렬
                    .limit(5); // 상위 5개만 가져오기
        }

        List<Posts> filteredProject = queryForProject.fetch();
        List<Posts> filteredStudy = queryForStudy.fetch();
        List<User> filteredUser = queryForUser.fetch();

        List<ProjectSearchDto> projectSearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성
        List<StudySearchDto> studySearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성
        List<UserSearchDto> userSearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성

        // 프로젝트를 dto에 담기
        for(Posts findPost : filteredProject) {
            ProjectSearchDto projectSearchDto = ProjectSearchDto.builder()
                    .id(findPost.getId())
                    .name(findPost.getTitle())
                    .build();

            projectSearchDtoList.add(projectSearchDto);
        }

        // 스터디를 dto에 담기
        for(Posts findPost : filteredStudy) {
            StudySearchDto studySearchDto = StudySearchDto.builder()
                    .id(findPost.getId())
                    .name(findPost.getTitle())
                    .build();

            studySearchDtoList.add(studySearchDto);
        }

        // 유저를 dto에 담기
        for(User findUser : filteredUser) {
            UserSearchDto userSearchDto = UserSearchDto.builder()
                    .id(findUser.getId())
                    .name(findUser.getNickName())
                    .build();

            userSearchDtoList.add(userSearchDto);
        }

        // 각각 담은 프로젝트 검색결과 배열, 스터디 검색결과 배열, 유저 검색결과 배열을 새로운 dto에 담아 프론트에 반환
        SearchResultDto searchResultDto = SearchResultDto.builder()
                .projectSearchDtoList(projectSearchDtoList)
                .studySearchDtoList(studySearchDtoList)
                .userSearchDtoList(userSearchDtoList)
                .build();



        return searchResultDto; // 동적쿼리의 결과를 반환

    }

}
