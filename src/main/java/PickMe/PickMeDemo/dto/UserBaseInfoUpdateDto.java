package PickMe.PickMeDemo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBaseInfoUpdateDto {

    private String nickName;
    private String userName;
    private String password;

}
