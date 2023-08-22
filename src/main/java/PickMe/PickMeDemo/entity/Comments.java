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
public class Comments extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    // isDeleted == true이면 삭제된 댓글
    @ColumnDefault("FALSE")
    @Column(nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 글쓴이. writer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // 댓글(comment_id)에 대한 id
    private Comments parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Comments> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id")
    private Posts posts;

    public Comments(String content) {
        this.content = content;
    }

    public void updateWriter(User user) {
        this.user = user;
    }

    public void updateBoard(Posts posts) {
        this.posts = posts;
    }

    public void updateParent(Comments comments) {
        this.parent = comments;
    }

    public void changeIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
