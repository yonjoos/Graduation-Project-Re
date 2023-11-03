package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupPostsDto {

    private Long id;
    private String writerNickName;
    private List<String> applyNickNames;
    private String title;
    private String postType;
    private Boolean web;
    private Boolean app;
    private Boolean game;
    private Boolean ai;
    private Integer counts;
    private Integer recruitmentCount;
    private LocalDate endDate;
    private List<Boolean> approved; // 한 게시물에 대해 모든 유저에 대한 승인 여부. writer가 보는 용도로 사용
    private Boolean isApproved; // 특정 유저의 승인 여부. applicant가 보는 용도로 사용
    private Boolean isFull;     // 정원이 모두 찼는지 체크.
    private LocalDateTime finalUpdatedTime; // 최종 게시물 수정 등록 시간
    private String briefContent;
    private Integer viewCount;
    private String nickName;
    private String imageUrl;

    public String getImageUrl(){
        if(this.imageUrl == null){
            return "%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%91%E1%85%B3%E1%84%89%E1%85%A1.png";
        }
        else return this.imageUrl;
    }

}
