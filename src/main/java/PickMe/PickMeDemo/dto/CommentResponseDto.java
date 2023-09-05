package PickMe.PickMeDemo.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CommentResponseDto {

    private Long id;
    private String content;
    private String nickName;
    private List<CommentResponseDto> children = new ArrayList<>();

    public CommentResponseDto(Long id, String content, String nickName) {
        this.id = id;
        this.content = content;
        this.nickName = nickName;
    }


}
