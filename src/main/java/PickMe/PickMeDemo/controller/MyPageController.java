package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.UserBaseInfoUpdateDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class MyPageController {

    private final UserService userService;

    public MyPageController(UserService userService) {
        this.userService = userService;
    }

    //유저 정보 가져오기 관련
    @GetMapping("/userInfo")
    public ResponseEntity<UserDto> userInfo(Principal principal) {
        String userEmail = principal.getName(); // Get the email from the JWT token
        //api시 해당 회원의 이메일을 알아와서
        //email기반으로 쿼리를 날리면 됨


        //System.out.println(userEmail);
        //UserDto userDto=(UserDto) principal;
        // Retrieve user information from the userService based on the email
        UserDto userDto = userService.findByEmail(userEmail);

        return ResponseEntity.ok(userDto);
    }

    //닉네임, 이름 변경 관련
    @PutMapping("/updateUserInfo")
    public ResponseEntity<String> updateUserInfo(@RequestBody UserBaseInfoUpdateDto updateDto, Principal principal) {
        String userEmail = principal.getName();

        try {
            // Call a method in your userService to handle the user base info update logic
            userService.updateUserBaseInfo(userEmail, updateDto);
            return ResponseEntity.ok("User information has been successfully updated.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user information.");
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
