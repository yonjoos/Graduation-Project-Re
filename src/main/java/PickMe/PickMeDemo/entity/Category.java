package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Setter
@Entity
public class Category extends BaseTimeEntity{ //생성일, 수정일 다루는 클래스를 상속

    //임베디드 타입 클래스(값타입 클래스로의 전환 가능성 존재)

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_id")
    private Long id; //모집분야 id

    // cascade 다 걷어냄. 일일이 코드 쳐주는 것으로 정책 변경.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="posts_id")
    private Posts posts; //연관관계의 주인, 게시물 table과 엮임(게시물의 외래키를 가지고 있음)

    // User와 1대1로 걸려있고, cascade 걸려있으므로, nullable = false에서 문제 생길 수 있음 ---- (user가 아니라 post와 연관된 테이블 아닌가?)
    @Column(name = "web", nullable = false)
    private Boolean web; //웹과 관련된 게시물이면 true

    @Column(name = "app", nullable = false)
    private Boolean app; //앱과 관련된 게시물이면 true

    @Column(name = "game", nullable = false)
    private Boolean game; //게임과 관련된 게시물이면 true

    @Column(name = "ai", nullable = false)
    private Boolean ai; //ai와 관련된 게시물이면 true



    public void validateFieldCount() {
        int trueFieldCount = 0;
        if (web) trueFieldCount++;
        if (app) trueFieldCount++;
        if (game) trueFieldCount++;
        if (ai) trueFieldCount++;

        if (trueFieldCount > 2) {
            throw new IllegalArgumentException("Maximum of 2 true values are allowed among web, app, game, and ai fields.");
        }
    }


}
