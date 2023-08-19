package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "app_user")   // User는 PostgreSQL의 기본? 기존? 테이블이므로, 이름을 User로 지정하지 않고, 따로 설정한다.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    @Size(max = 100)
    private String userName;

    @Column(name = "nick_name", nullable = false)
    @Size(max = 100)
    private String nickName;

    @Column(nullable = false)
    @Size(max = 100)
    private String email;

    @Column(nullable = false)
    @Size(max = 100)
    private String password;

    // created date, modified date, last login date 추가하기
    // role 추가하기
    // profile 사진 추가하기
}