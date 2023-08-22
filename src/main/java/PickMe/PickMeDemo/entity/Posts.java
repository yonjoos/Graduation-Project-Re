package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Posts extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "posts_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "posts")
    private Category category;

    // 작성한 게시물에 대해서만 게시물과 1대1
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "posts")
    private UserCreatedPosts userCreatedPosts;

    @OneToMany(mappedBy = "posts")
    private List<Comments> comments = new ArrayList<>();

    @OneToMany(mappedBy = "posts")
    private List<ScrapPosts> scrapPosts = new ArrayList<>();

    @OneToMany(mappedBy = "posts")
    private List<UserApplyPosts> userApplyPosts = new ArrayList<>();

    @Enumerated(EnumType.STRING) // 게시물 타입 (project/study)으로 구분
    private PostType postType;

    @Column(name = "title")
    private String title;

    @Column(name = "recruitmentCount")
    private Integer recruitmentCount;

    @Column(name = "content")
    private String content;

    @Column(name = "promoteImageUrl")
    private String promoteImageUrl;

    @Column(name = "fileUrl")
    private String fileUrl;

    @Column(name = "endDate")
    private LocalDateTime endDate;
}
