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
public class PostsListDto {

    private Long id;
    private String nickName;
    private String title;
    private Boolean web;
    private Boolean app;
    private Boolean game;
    private Boolean ai;
    private Integer counts;
    private Integer recruitmentCount;
    private LocalDate endDate;
    private String briefContent;
    private Integer viewCount;
    private LocalDateTime finalUpdatedTime; // 최종 게시물 수정 등록 시간
    private String imageUrl;

    public String getImageUrl(){
        if(this.imageUrl == null){
            return "%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%91%E1%85%B3%E1%84%89%E1%85%A1.png";
        }
        else return this.imageUrl;
    }

}
