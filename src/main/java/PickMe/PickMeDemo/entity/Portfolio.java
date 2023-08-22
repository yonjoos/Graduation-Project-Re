package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Portfolio extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    @OneToOne (fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="user_id")
    private User user;

    // User와 1대1로 걸려있고, cascade 걸려있으므로, nullable = false에서 문제 생길 수 있음
    @Column(name = "web", nullable = false)
    private Integer web;

    @Column(name = "app", nullable = false)
    private Integer app;

    @Column(name = "game", nullable = false)
    private Integer game;

    @Column(name = "ai", nullable = false)
    private Integer ai;

    private String shortIntroduce;

    private String introduce;

    private String fileUrl;

}
