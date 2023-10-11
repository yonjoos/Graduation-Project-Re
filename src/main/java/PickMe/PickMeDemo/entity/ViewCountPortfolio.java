package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ViewCountPortfolio extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_count_portfolio_id")
    private Long id; // 포트폴리오 조회수 확인 테이블의 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 포트폴리오를 조회한 회원 - 연관관계의 주인, 회원 table과 엮임(회원의 외래키를 가지고 있음)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio; // 조회 대상이 된 포트폴리오 - 연관관계의 주인, 포트폴리오 table과 엮임(게시물의 외래키를 가지고 있음)
}
