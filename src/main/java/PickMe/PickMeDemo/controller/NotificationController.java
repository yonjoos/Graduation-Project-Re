package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.UserRepository;
import PickMe.PickMeDemo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping(value = "/subscribe/{nickName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String nickName) {

        System.out.println("========start findUser==========");
        User findUser = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));
        System.out.println("========end findUser==========");

        return notificationService.subscribe(findUser.getId());
    }

    // 알림 메시지를 프론트 창에서 작성하고, 그 메시지를 특정 유저에게 보내는 메서드인듯
    @PostMapping("/sendData/{nickName}")
    public ResponseEntity<String> sendData(@PathVariable String nickName, @RequestBody String message) {

        User findUser = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        notificationService.notify(findUser.getId(), message);

        return ResponseEntity.ok("Notification sent successfully");
    }
}