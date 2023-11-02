package PickMe.PickMeDemo.controller;


import PickMe.PickMeDemo.dto.PortfolioFormDto;
import PickMe.PickMeDemo.dto.PostsFormDto;
import PickMe.PickMeDemo.dto.ProfileImageUploadDTO;
import PickMe.PickMeDemo.dto.UserBaseInfoUpdateDto;
import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

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

}
