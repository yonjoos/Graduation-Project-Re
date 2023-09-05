package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

}
