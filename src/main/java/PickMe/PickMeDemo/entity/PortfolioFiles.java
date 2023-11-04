package PickMe.PickMeDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PortfolioFiles extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_files_id")
    private Long id; //포트폴리오 파일 테이블의 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    private boolean isImage; // 해당 파일이 이미지 파일인지 아닌지 구분하기 위한 구분자

    private String fileUrl; // 구글 드라이브에 저장되는 파일의 uuid값

    private String fileName; // 사용자가 지정한 파일의 이름
}
