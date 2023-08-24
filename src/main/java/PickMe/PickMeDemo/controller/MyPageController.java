package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.UserBaseInfoUpdateDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final UserService userService;

    //유저 정보 가져오기 관련
    @GetMapping("/userInfo")
    public ResponseEntity<UserDto> userInfo(Principal principal) {
        String userEmail = principal.getName(); // Get the email from the JWT token

        //System.out.println(userEmail);
        //UserDto userDto=(UserDto) principal;
        // Retrieve user information from the userService based on the email
        UserDto userDto = userService.findByEmail(userEmail);

        return ResponseEntity.ok(userDto);
    }

    //닉네임, 이름 변경 관련
    @PutMapping("/updateUserInfo")
    public ResponseEntity<String> updateUserInfo(@RequestBody @Valid UserBaseInfoUpdateDto updateDto, Principal principal) {
        String userEmail = principal.getName();

        try {
            // Call a method in your userService to handle the user base info update logic
            userService.updateUserBaseInfo(userEmail, updateDto);
            return ResponseEntity.ok("User information has been successfully updated.");
        } catch (AppException ex) {

            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }



    //회원탈퇴 관련
    @PostMapping("/signOut")
    public ResponseEntity<String> signOut(Principal principal) {
        String userEmail = principal.getName(); // Get the email from the JWT token!

        try {
            // Call a method in your userService to handle the user withdrawal logic
            userService.signOut(userEmail);
            return ResponseEntity.ok("User has been successfully withdrawn.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to withdraw user.");
        }
    }
}
