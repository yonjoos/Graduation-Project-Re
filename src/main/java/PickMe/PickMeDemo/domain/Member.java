package PickMe.PickMeDemo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id") // 엔티티의 식별자는 id 를 사용하고 PK 컬럼명은 member_id 를 사용. 외래키에 넘겨줄 이름
    private Long id;    // id는 별도의 컬럼 명을 지정해주는 것이 명확하고 좋다. 관례는 테이블명_id

    private String name;    // 회원명
}
