package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.mapper.UserMapper;
import PickMe.PickMeDemo.repository.UserRepository;
import PickMe.PickMeDemo.service.UserService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SignOutController {

    private final UserService userService;

}
