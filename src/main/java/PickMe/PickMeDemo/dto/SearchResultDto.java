package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResultDto { // 랜딩페이지에서 실시간 검색 조회를 반환할 dto

    List<ProjectSearchDto> projectSearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성(프로젝트 배열)
    List<StudySearchDto> studySearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성(스터디 배열)
    List<UserSearchDto> userSearchDtoList = new ArrayList<>(); // 빈 컬렉션 생성(유저 배열)


}
