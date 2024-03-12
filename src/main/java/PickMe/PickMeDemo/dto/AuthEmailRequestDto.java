package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthEmailRequestDto {

    private boolean sented; // 이메일 발송 여부: true -> 이미 등록되어있는 회원이 없으므로 메일 발송  // 등록되어있는 회원인 경우, 비밀번호 재설정 메일 발송
                            //                 false -> 이미 등록되어있는 회원이 있으므로 메일 발송 불가 // 등록되어있지 않은 회원인 경우, 비밀번호 재설정 메일 발송 불가
}
