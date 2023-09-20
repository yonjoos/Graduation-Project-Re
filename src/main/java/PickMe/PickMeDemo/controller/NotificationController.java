package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.NotificationDto;
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

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 로그인이 완료되면, 클라이언트 쪽에서 일정 시간마다 event를 받기 위한 연결 설정 시도하는데, 연결 설정을 세팅해주는 컨트롤러
    // 사용자 닉네임을 받아서, 연결설정을 한다.
    @GetMapping(value = "/subscribe/{nickName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String nickName) {

        System.out.println("========start findUser==========");
        User findUser = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));
        System.out.println("========end findUser==========");

        return notificationService.subscribe(findUser.getId());
    }

    // 알림 메시지를 프론트 창에서 작성하고, 그 메시지를 특정 유저에게 보내는 메서드인듯
    // 현재에는 따로 이 컨트롤러를 쓰진 않음
    @PostMapping("/sendData/{nickName}")
    public ResponseEntity<String> sendData(@PathVariable String nickName, @RequestBody String message) {

        User findUser = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        notificationService.notify(findUser.getId(), message);

        return ResponseEntity.ok("Notification sent successfully");
    }



    // 위에까진 sse(실시간) 연결 동작을 위한 컨트롤러


    // 각 사용자의 Notification 배너 안에 들어갈 내용을 전부 가져오는 컨트롤러
    @GetMapping("/getNotifications")
    public ResponseEntity<List<NotificationDto>> getNotifications(Principal principal) {

        String userEmail = principal.getName(); // 요청을 한 회원의 email을 찾음

        List<NotificationDto> notificationDto = notificationService.getNotifications(userEmail); // 모든 알림 내용 가져오기 시도

        return ResponseEntity.ok(notificationDto);
    }

    // 회원의 알림을 삭제하는 컨트롤러
    // 프런트에서 해당 알림의 id를 받아서 처리함
    @PostMapping("/deleteNotification/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok("해당 알림이 삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알림 삭제에 실패했습니다.");
        }
    }

}