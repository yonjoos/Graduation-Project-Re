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
            return "comgongWow.png";
        }
        else return this.imageUrl;
    }

}
