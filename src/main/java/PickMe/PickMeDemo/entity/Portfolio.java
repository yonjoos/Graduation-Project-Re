package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Portfolio extends BaseTimeEntity { //생성일, 수정일 다루는 클래스를 상속

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id; //포트폴리오 테이블의 기본키

    // cascade 다 걷어냄. 일일이 코드 쳐주는 것으로 정책 변경.
    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user; //포트폴리오의 대상이 되는 회원 - 연관관계의 주인, 회원 table과 엮임(회원의 외래키를 가지고 있음)

    @OneToMany(mappedBy = "portfolio")
    private List<ViewCountPortfolio> viewCountPortfolios = new ArrayList<>(); // 연관관계의 거울로 작용, 포트폴리오 조회 수 table과 엮임(db필드에 안들어감)

    // User와 1대1로 걸려있고, cascade 걸려있으므로, nullable = false에서 문제 생길 수 있음
    @Column(name = "web", nullable = false)
    private Integer web; //추천 시스템 - 선호도 관련

    @Column(name = "app", nullable = false)
    private Integer app; //추천 시스템 - 선호도 관련

    @Column(name = "game", nullable = false)
    private Integer game; //추천 시스템 - 선호도 관련

    @Column(name = "ai", nullable = false)
    private Integer ai; //추천 시스템 - 선호도 관련



    private String shortIntroduce; //한줄 소개

    @Column(columnDefinition = "TEXT") // @Lob 대신 Text로 변경 -> 1gb까지 저장 가능
    private String introduce; //소개

    private String fileUrl; //첨부 파일



    public Portfolio(User user, Integer web, Integer app, Integer game, Integer ai, String shortIntroduce, String introduce, String fileUrl) {
        this.user = user;
        this.web = web;
        this.app = app;
        this.game = game;
        this.ai = ai;
        this.shortIntroduce = shortIntroduce;
        this.introduce = introduce;
        this.fileUrl = fileUrl;
    }

    public Integer[] getVector(){
        Integer[] vector = new Integer[]{this.web, this.app, this.game, this.ai};

        return vector;
    }

}
