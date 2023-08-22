package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Setter
@Entity
public class Category extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="posts_id")
    private Posts posts;

    // User와 1대1로 걸려있고, cascade 걸려있으므로, nullable = false에서 문제 생길 수 있음
    @Column(name = "web", nullable = false)
    private Boolean web;

    @Column(name = "app", nullable = false)
    private Boolean app;

    @Column(name = "game", nullable = false)
    private Boolean game;

    @Column(name = "ai", nullable = false)
    private Boolean ai;
}
