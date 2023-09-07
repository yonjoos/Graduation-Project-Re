package PickMe.PickMeDemo.dto;

import PickMe.PickMeDemo.entity.Comments;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CommentResponseDto {

    private Long id; // 댓글 또는 답글 id
    private String content; // 댓글 또는 답글 내용
    private String nickName; // 댓글 또는 답글 작성자 닉네임
    private Long userId; // 댓글 또는 답글 작성자 pk
    private boolean commentWriter; // 해당 댓글 또는 답글의 작성자 여부

    @Builder.Default
    private List<CommentResponseDto> children = new ArrayList<>(); // 롬복 도구 활용해서 childen배열을 초기화 보장


}
