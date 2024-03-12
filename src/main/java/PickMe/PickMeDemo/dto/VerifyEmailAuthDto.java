package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyEmailAuthDto {

    private Integer verified; //0: 인증시간 초과 1: 코드 일치 2: 코드 불일치
}
