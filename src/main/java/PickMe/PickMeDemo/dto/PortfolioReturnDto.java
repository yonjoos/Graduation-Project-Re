package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioReturnDto {

    // 나중에 회원 사진 추가 필요
    private Boolean isCreated;
    private String nickName;
    private String email;
    private Integer web;
    private Integer app;
    private Integer game;
    private Integer ai;
    private String shortIntroduce;
    private String introduce;
    private String fileUrl;
    private Integer viewCount;

}
