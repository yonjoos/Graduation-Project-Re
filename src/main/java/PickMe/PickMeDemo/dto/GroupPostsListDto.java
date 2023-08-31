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
public class GroupPostsListDto {

    private Long id;
    private String nickName;
    private String title;
    private String postType;
    private Boolean web;
    private Boolean app;
    private Boolean game;
    private Boolean ai;
    private Integer recruitmentCount;
    private LocalDate endDate;

}
