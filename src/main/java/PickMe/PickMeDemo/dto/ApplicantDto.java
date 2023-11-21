package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicantDto {

    private String nickName;    // 지원자 닉네임
    private String imageUrl;    // 지원자 프사
    private Boolean confirm;    // 승인 여부
    private Integer count;      // 디테일 페이지에서 지원자 수 변동을 실시간으로 보여주기 위해 count를 반환

}
