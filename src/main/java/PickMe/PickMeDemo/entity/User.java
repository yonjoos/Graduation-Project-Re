package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Setter
@Entity
@Table(name = "app_user")   // User는 PostgreSQL의 기본? 기존? 테이블이므로, 이름을 User로 지정하지 않고, 따로 설정한다.
public class User extends BaseTimeEntity{ //생성일, 수정일 다루는 클래스를 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "user_id")
    private Long id; //회원 테이블의 기본키

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private Portfolio portfolio; //회원의 포트폴리오 - 연관관계의 거울로 작용, 포트폴리오 table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "user")
    private List<Posts> posts = new ArrayList<>();  // 연관관계의 거울로 작용, 게시물 (Posts) table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "user")
    private List<Comments> comments = new ArrayList<>(); //연관관계의 거울로 작용, 게시물 댓글 table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "user")
    private List<ScrapPosts> scrapPosts = new ArrayList<>(); //연관관계의 거울로 작용, 스크랩한 게시물 table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "user")
    private List<UserApplyPosts> userApplyPosts = new ArrayList<>(); // 연관관계의 거울로 작용, 지원한 게시물 table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "user")
    private List<Notifications> notifications = new ArrayList<>(); // 연관관계의 거울로 작용, 알림 table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "user")
    private List<ViewCountPosts> viewCountPosts = new ArrayList<>(); // 연관관계의 거울로 작용, 게시물 조회 수 table과 엮임(db필드에 안들어감)

    @OneToMany(mappedBy = "user")
    private List<ViewCountPortfolio> viewCountPortfolios = new ArrayList<>(); // 연관관계의 거울로 작용, 포트폴리오 조회 수 table과 엮임(db필드에 안들어감)

    @Column(name = "user_name", nullable = false)
    @Size(max = 100)
    private String userName; //회원의 이름

    @Column(name = "nick_name", nullable = false, unique = true)
    @Size(max = 100)
    private String nickName; //회원의 닉네임

    @Column(nullable = false, unique = true) //사실상 fk의 역할을 수행할 예정, 따라서 unique제약조건 달았음
    @Size(max = 100)
    private String email; //회원의 이메일

    @Column(nullable = false)
    @Size(max = 100)
    private String password; //회원의 패스워드

    @Column
    private String profilePictureUrl; //회원의 프로필 사진 경로

    @Enumerated(EnumType.STRING) //사용자 역할 (user/admin)으로 구분
    private Role role; //사용자의 역할

    @Column
    private LocalDateTime lastAccessDate; //마지막 로그인 성공 날짜

    public void modifyLastLoginDate(LocalDateTime now) {
        lastAccessDate=now;
    }

    // created date, modified date, last login date 추가하기
    // role 추가하기
    // profile 사진 추가하기


    public User(Long id, String userName, String nickName, String email, String password, Role role, LocalDateTime lastAccessDate) {
        this.id = id;
        this.userName = userName;
        this.nickName = nickName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.lastAccessDate = lastAccessDate;
    }
}