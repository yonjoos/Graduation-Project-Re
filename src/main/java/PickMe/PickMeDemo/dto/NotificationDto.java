package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {

    private Long notificationId; // 해당 알림의 id
    private Long postId; // 게시물 id
    private String notificationMessage; // Notification 배너에 들어갈 카드 내용
    private String postType; // 게시물 type -> PROJECT / STUDY

}
