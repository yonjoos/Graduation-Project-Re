package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Notifications extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notifications_id") //테이블 이름
    private Long id; // 알림의 기본 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 해당 알림이 누구에 대한 알림인지 - 연관관계의 주인, 회원 table과 엮임(회원의 외래키를 가지고 있음)

    @Column(nullable = false)
    private Boolean checked;    // 해당 알림 배너를 사용자가 읽었는지 여부 -> 만약 읽었으면 checked를 true로 바꾼다


    @Column
    private Long postId; // 어떤 게시물에 대한 알림인지

    @Column
    private String notificationMessage; // 알림 내용 문자열

    @Enumerated(EnumType.STRING)
    private PostType postType; // 게시물 타입 (project/study)으로 구분
}
