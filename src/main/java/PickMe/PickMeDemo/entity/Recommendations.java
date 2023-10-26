package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Recommendations extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendations_id")
    private Long id; //추천 테이블의 기본키

    @Column
    private String myInterest;  // 나의 관심사. 0000 ~ 4321까지 들어감. 0000이 0으로 저장되는 것을 방지하기 위해 Integer 대신 String 사용

    @Column
    private String otherInterest;   // 상대방의 관심사. 0000 ~ 4321까지 들어감.

    @Column
    private Double cosineSimilarity;    // 가중치가 부여된 코사인 유사도
}
