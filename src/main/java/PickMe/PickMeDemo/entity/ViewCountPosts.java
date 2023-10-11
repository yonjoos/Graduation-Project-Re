package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ViewCountPosts extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_count_posts_id")
    private Long id; // 게시물 조회수 확인 테이블의 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 글을 조회한 회원 - 연관관계의 주인, 회원 table과 엮임(회원의 외래키를 가지고 있음)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id")
    private Posts posts; // 조회 대상이 된 게시물 - 연관관계의 주인, 게시물 table과 엮임(게시물의 외래키를 가지고 있음)

}
