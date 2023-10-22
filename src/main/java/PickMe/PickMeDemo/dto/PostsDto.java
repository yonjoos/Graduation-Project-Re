package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostsDto {

    private Boolean writer;     // 게시물의 작성자인가?
    private Boolean scrap;      // 유저가 스크랩 했나?
    private Boolean applying;      // 게시물에 지원은 했으나, 승인은 안됨
    private Boolean applied;        // 게시물에 지원했고, 승인도 됨
    private String nickName;
    private String title;
    private Boolean web;
    private Boolean app;
    private Boolean game;
    private Boolean ai;
    private String content;
    private List<String> promoteImageUrl;
    private String fileUrl;
    private Integer counts;
    private Integer recruitmentCount;
    private LocalDate endDate;
    private Integer viewCount;

}
