package PickMe.PickMeDemo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpDto {

    @NotEmpty
    private String userName;

    @NotEmpty
    private String nickName;

    @NotEmpty
    private String email;

    @NotEmpty
    private char[] password;

}