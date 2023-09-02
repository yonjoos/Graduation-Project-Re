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

    private boolean sented; // 이메일 발송 여부: true -> 이미 등록되어있는 회원이 없으므로 메일 발송
                            //                 false -> 이미 등록되어있느 회원이 있으므로 메일 발송 불가
}
