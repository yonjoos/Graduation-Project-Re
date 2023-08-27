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
public class PostsUpdateFormDto {

    // PostsFormDto와 차이점 : postType이 Boolean 리스트이다.
    // Boolean으로 한 이유 : Backend에서 String 데이터를 넘겨서 프론트에서 처리하는 작업이 매우 어렵다..
    // Boolean은 response.data.postType에서 삼항 연산자를 써야하는 것 때문인 것 같음
    private String title;
    private List<Boolean> postType;
    private Integer recruitmentCount;
    private LocalDate endDate;
    private String content;
    private String promoteImageUrl;
    private String fileUrl;
}
