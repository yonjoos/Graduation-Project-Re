package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScrapPostsDto {

    private Long id;
    private String nickName;    // 게시물 작성자 닉네임
    private String title;
    private String postType;
    private Boolean web;
    private Boolean app;
    private Boolean game;
    private Boolean ai;
    private Integer counts;
    private Integer recruitmentCount;
    private LocalDate endDate;
    private Boolean isApplied;  // 유저의 지원 여부.
    private Boolean isApproved; // 유저의 승인 여부.
    private LocalDateTime finalUpdatedTime; // 최종 게시물 수정 등록 시간
    private String briefContent;
    private Integer viewCount;
    private String imageUrl;

    public String getImageUrl(){
        if(this.imageUrl == null){
            return "%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%91%E1%85%B3%E1%84%89%E1%85%A1.png";
        }
        else return this.imageUrl;
    }

}
