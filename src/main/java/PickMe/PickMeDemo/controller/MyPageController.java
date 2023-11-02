package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final UserService userService;

    //user의 기본정보 가져오기 관련
    @GetMapping("/userInfo")
    public ResponseEntity<UserDto> userInfo(Principal principal) {
        String userEmail = principal.getName(); // jwt토큰을 기반으로 user의 email을 찾는다.

        UserDto userDto = userService.findByEmail(userEmail); //user의 email을 기반으로 user의 정보를 userDto로 변환해 가져온다

        return ResponseEntity.ok(userDto); //userDto를 프론트에 반환한다.
    }

    //user의 닉네임, 이름 변경 관련
    @PutMapping("/updateUserInfo")
    public ResponseEntity<String> updateUserInfo(@RequestBody @Valid UserBaseInfoUpdateDto updateDto, Principal principal) {
        String userEmail = principal.getName(); // jwt토큰을 기반으로 user의 email을 찾는다.

        try {
            userService.updateUserBaseInfo(userEmail, updateDto); //email기반으로 user을 찾고, 입력받은 비밀번호가 db의 비밀번호와 같다면 닉네임 또는 이름 업데이트 진행
            return ResponseEntity.ok("User information has been successfully updated.");
        } catch (AppException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }

    @GetMapping("/userProfileImage") // Use path variable to get project ID from URL
    private ResponseEntity<ProfileImageUrlDto> getUserProfileImage(Principal principal) {

        System.out.println("프사 컨트롤러");
        ProfileImageUrlDto userDto = userService.getUserProfileImage(principal);
        System.out.println("프사 컨트롤러 끝");


        return ResponseEntity.ok(userDto);
    }
    @PostMapping("/updateProfileImage")
    public ResponseEntity<String> updateProfileImage(@Valid ProfileImageUploadDTO FormDto, Principal principal)throws IOException {
        try{
            System.out.println("업로드 드드");
            userService.updateProfileImage(FormDto, principal);
            System.out.println("성공했어요");
            return ResponseEntity.ok("success");
        }catch (IllegalArgumentException ex) {
            // Handle the exception and send an appropriate HTTP response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

    }

    //user의 비밀번호 변경 관련
    @PutMapping("/updatePassword")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid UserPasswordUpdateDto userPasswordUpdateDto, Principal principal){
        String userEmail = principal.getName(); // jwt토큰을 기반으로 user의 email을 찾는다.

        try {
            //email기반으로 user을 찾고, 입력받은 기존의 비밀번호가 db의 비밀번호와 같다면 바꾸려는 비밀번호로 user의 password를 업데이트한다.
            userService.updateUserPassword(userEmail, userPasswordUpdateDto.getCurrentPassword(),userPasswordUpdateDto.getPassword());
            return ResponseEntity.ok("Password updated successfully.");
        } catch (AppException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }


    }



    //user의 회원탈퇴 관련
    @PostMapping("/signOut")
    public ResponseEntity<String> signOut(@RequestBody @Valid SignOutDto signOutDto, Principal principal) {
        String userEmail = principal.getName(); // jwt토큰을 기반으로 user의 email을 찾는다.

        try {
            //email기반으로 user을 찾고, 입력받은 기존의 비밀번호가 db의 비밀번호와 같다면 회원탈퇴를 진행한다
            userService.signOut(userEmail, signOutDto.getCurrentPasswordForSignOut() );
            return ResponseEntity.ok("User has been successfully withdrawn.");
        } catch (AppException ex) {

            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }
}
