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
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private Portfolio portfolio;

    @OneToMany(mappedBy = "user")
    private List<UserCreatedPosts> userCreatedPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comments> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ScrapPosts> scrapPosts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserApplyPosts> userApplyPosts = new ArrayList<>();

    @Column(name = "user_name", nullable = false)
    @Size(max = 100)
    private String userName;

    @Column(name = "nick_name", nullable = false)
    @Size(max = 100)
    private String nickName;

    @Column(nullable = false, unique = true)
    @Size(max = 100)
    private String email;

    @Column(nullable = false)
    @Size(max = 100)
    private String password;

    @Enumerated(EnumType.STRING) //사용자 역할 (user/admin)으로 구분
    private Role role;

    @Column
    private LocalDateTime lastAccessDate;

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