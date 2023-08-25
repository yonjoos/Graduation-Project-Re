package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Posts extends BaseTimeEntity { //생성일, 수정일 다루는 클래스를 상속

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "posts_id")
    private Long id; //게시물 테이블의 기본키

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "posts")
    private Category category; //해당 게시물의 카테고리 - 연관관계의 거울로 작용, 모집분야(웹,앱,게임,ai) table과 엮임(db필드에 안들어감)

    // 게시물과 유저의 관계는 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "posts")
    private List<Comments> comments = new ArrayList<>(); //연관관계의 거울로 작용, 게시물 댓글 table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "posts")
    private List<ScrapPosts> scrapPosts = new ArrayList<>(); //연관관계의 거울로 작용, 스크랩한 게시물 table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "posts")
    private List<UserApplyPosts> userApplyPosts = new ArrayList<>(); //연관관계의 거울로 작용, 지원한 게시물 table과 엮임(db필드에 안들어감)

    @Enumerated(EnumType.STRING)
    private PostType postType; // 게시물 타입 (project/study)으로 구분

    @Column(name = "title", nullable = false)
    private String title; //게시물 이름

    @Column(name = "recruitmentCount", nullable = false)
    private Integer recruitmentCount; //지원한 사람 수

    @Column(name = "content", nullable = false)
    private String content; //내용

    @Column(name = "promoteImageUrl")
    private String promoteImageUrl; //홍보 사진 url주소

    @Column(name = "fileUrl")
    private String fileUrl; //첨부 파일 경로

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate; //모집 마감 기간

    public Posts(User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate) {
        this.user = user;
        this.postType = postType;
        this.title = title;
        this.recruitmentCount = recruitmentCount;
        this.content = content;
        this.promoteImageUrl = promoteImageUrl;
        this.fileUrl = fileUrl;
        this.endDate = endDate;
    }
}