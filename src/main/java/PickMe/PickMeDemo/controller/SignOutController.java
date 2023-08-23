package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.mapper.UserMapper;
import PickMe.PickMeDemo.repository.UserRepository;
import PickMe.PickMeDemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
public class SignOutController {

    private final UserService userService;

    public SignOutController(UserService userService) {
        this.userService = userService;
    }
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


    @PostMapping("/signOut")
    public ResponseEntity<String> signOut(Principal principal) {
        String userEmail = principal.getName(); // Get the email from the JWT token

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
