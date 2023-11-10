package PickMe.PickMeDemo.controller;


import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final UserService userService;


    @PostMapping("/uploadProfileImage")
    public ResponseEntity<String> uploadProfileImage(@Valid ProfileImageUploadDTO FormDto, Principal principal) throws IOException {
        try{
            userService.updateProfileImage(FormDto, principal);
            System.out.println("성공했어요");
            return ResponseEntity.ok("success");
        }catch (IllegalArgumentException ex) {
            // Handle the exception and send an appropriate HTTP response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

    }

    @GetMapping("/getOtherUsersProfileImage")
    public ResponseEntity<ProfileImageUrlDto> getOtherUsersProfileImage(@RequestParam String nickName) {

        // getUserPortfolio : 유저의 닉네임을 통해 해당 유저의 포트폴리오를 가져오는 함수
        ProfileImageUrlDto imageUrlDto = userService.getUserProfileImageByNickName(nickName);

        return ResponseEntity.ok(imageUrlDto);
    }


    @PutMapping("/removeProfileImage")
    private ResponseEntity<String> removeProfileImage(Principal principal) {

        // Email 찾기
        String userEmail = principal.getName();
        userService.removeProfileUrl(userEmail);
        try {
            //email기반으로 user을 찾고, 입력받은 기존의 비밀번호가 db의 비밀번호와 같다면 바꾸려는 비밀번호로 user의 password를 업데이트한다.
            userService.removeProfileUrl(userEmail);
            return ResponseEntity.ok("success");
        } catch (AppException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }

}
