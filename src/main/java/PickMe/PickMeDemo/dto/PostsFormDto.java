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
public class PostsFormDto {

    private String title;
    private List<String> postType; // Assuming postType is an array of strings
    private Integer recruitmentCount;
    private LocalDate endDate;
    private String content;
    private String promoteImageUrl;
    private String fileUrl;
}
