package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPasswordUpdateDto {

    private String currentPassword; //현재 비밀번호
    private String password; //바꾸고자 하는 비밀번호
    private String confirmNewPassword; //바꾸고자 하는 비밀번호 확인

}
