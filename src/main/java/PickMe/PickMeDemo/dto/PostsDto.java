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
public class PostsDto {

    private Boolean writer;
    private String nickName;
    private String title;
    private Boolean web;
    private Boolean app;
    private Boolean game;
    private Boolean ai;
    private String content;
    private String promoteImageUrl;
    private String fileUrl;
    private Integer recruitmentCount;
    private LocalDate endDate;

}
