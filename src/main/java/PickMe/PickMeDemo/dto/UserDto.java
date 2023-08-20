package PickMe.PickMeDemo.dto;

import PickMe.PickMeDemo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String userName;
    private String nickName;
    private String email;
    private Role role;
    private String token;

}