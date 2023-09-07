package PickMe.PickMeDemo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioCardDto {

    // 전체 포트폴리오만 필요할 경우에는 DTO 불러올 때, 유저는 알 필요가 없다.
    // 특정 유저의 포트폴리오가 필요할 때 불러옴
    private String nickName;
    private String email;
    private Integer web;
    private Integer app;
    private Integer game;
    private Integer ai;
    private String shortIntroduce;


}

