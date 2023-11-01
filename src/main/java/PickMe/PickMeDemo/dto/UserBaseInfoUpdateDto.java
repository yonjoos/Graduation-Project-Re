package PickMe.PickMeDemo.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBaseInfoUpdateDto {

    private String nickName;
    private String userName;
    private String password;
    private MultipartFile imageUrl;

}
