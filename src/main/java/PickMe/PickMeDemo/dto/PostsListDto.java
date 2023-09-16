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

}
