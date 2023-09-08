package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DynamicInsert
@Table(name = "comment")
public class Comments extends BaseTimeEntity { //생성일, 수정일 다루는 클래스를 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id; //댓글 테이블의 기본키

    @Column(nullable = false, length = 1000)
    private String content; //댓글 내용


    @ColumnDefault("FALSE") // isDeleted == true이면 삭제된 댓글, 디폴트 값은 false
    @Column(nullable = false)
    private Boolean isDeleted; //댓글의 삭제 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  //댓글 작성자 - 연관관계의 주인, 회원 table과 엮임(회원의 외래키를 가지고 있음)

    @ManyToOne(fetch = FetchType.LAZY) //댓글의 계층화를 고려해 고안한 구조
    @JoinColumn(name = "parent_id")                                     // 댓글(comment_id)에 대한 id
    private Comments parent; //부모 댓글 엔티티의 id값 - 연관관계의 주인, 자기 자신 table과 엮임

    @OneToMany(mappedBy = "parent", orphanRemoval = true) //댓글의 계층화를 고려해 고안한 구조
    private List<Comments> children = new ArrayList<>(); //해당 댓글의 자식 댓글들 - 연관 관계의 거을, 부모 댓글입장에서 자식 댓글들은 one to many임

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id")
    private Posts posts; //댓글의 대상이 되는 게시물 - 연관관계의 주인, 게시물 table과 엮임(게시물의 외래키를 가지고 있음)

    // 댓글 내용만 생성자로 미리 세팅
    // 부모 자식 관계는 그 후에 세팅
    public Comments(String content) {
        this.content = content;
    }

    public void updateWriter(User user) {
        this.user = user;
    }

    public void updatePosts(Posts posts) {
        this.posts = posts;
    }

    public void updateParent(Comments comments) {
        this.parent = comments;
    }

    public void changeIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void updateContent(String content) { this.content=content;}
}
