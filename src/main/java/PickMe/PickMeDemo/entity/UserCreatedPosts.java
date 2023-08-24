package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserCreatedPosts extends BaseTimeEntity { //생성일, 수정일 다루는 클래스를 상속

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userCreatedPosts_id") //중간 테이블
    private Long id; //작성한 게시물 테이블의 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; //게시물의 작성한 회원 - 연관관계의 주인, 회원 table과 엮임(회원의 외래키를 가지고 있음)

    // 작성한 게시물에 대해서만 게시물과 1대1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id")
    private Posts posts; //작성한 게시물 - 연관관계의 주인, 게시물 table과 엮임(게시물의 외래키를 가지고 있음)
}
