package PickMe.PickMeDemo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CommentRequestDto {


    private Long parentId; // 부모 댓글 id
    private String content; // 댓글 내용



}
