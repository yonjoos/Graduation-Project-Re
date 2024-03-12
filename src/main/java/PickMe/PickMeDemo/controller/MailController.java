package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.AuthEmailRequestDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.dto.VerifyEmailAuthDto;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.service.MailService;
import PickMe.PickMeDemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;
    private final UserService userService;

    @PostMapping("/mailConfirm") // 프런트엔드에서 전달받은 이메일로 메일 발송하는 컨트롤러
    public ResponseEntity<AuthEmailRequestDto> mailConfirm(@RequestParam(name = "email") String email) throws Exception {

        System.out.println("email = " + email);

        try {
            UserDto existingUser = userService.findByEmail(email);
            if (existingUser != null) { // 이미 존재하는 회원 email이라면
                System.out.println("existingUser = " + existingUser);
                return ResponseEntity.ok(new AuthEmailRequestDto(false)); // 이메일 발송하지 않고, 프런트에 false를 반환
            }
        } catch (AppException ex) {
            // 존재하지 않는 회원 email이라면(userService.findByEmail 메서드 보면, 회원이 없으면 예외를 반환함)
            // 예외가 왔다는 건, 해당 email을 가진 회원이 없다는 것이므로 인증메일을 발송하면 됨

            AuthEmailRequestDto authEmailRequestDto = mailService.sendSimpleMessage(email); // 전달받은 email로 메일 발송
            return ResponseEntity.ok(authEmailRequestDto); // authEmailRequestDto엔 true가 담겨있을 것
        }

        // 그 외의 경우는 존재할 수 없는데, 혹시 몰라서 server error처리하기
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("/verifyCode") // 프런트엔드에서 유저가 인증코드를 입력하면, 그걸 검증하는 컨트롤러

    public ResponseEntity<VerifyEmailAuthDto> verifyCode(
            @RequestParam("code") String code, // 프런트에서 넘어온 인증번호 값
            @RequestParam("email") String email) { // 프런트에서 넘어온 email값

        System.out.println("email = " + email);
        System.out.println("code : " + code);


        // 입력받은 email과 code에 해당하는 redis값이 있는지 확인
        if (mailService.verifyEmailCode(email, code).equals(1)) {
            System.out.println("email = " + email);
            System.out.println("인증 성공");
            mailService.delete(email); // 인증이 완료되었으므로 redis에서 해당 email-인증번호 값 지워주기
            return ResponseEntity.ok(new VerifyEmailAuthDto(1)); // verified: 1반환
        }

        // 인증 시간 만료면 verified == 0
        else if(mailService.verifyEmailCode(email,code).equals(0))
        {
            System.out.println("인증 실패 - 시간 초과");
            return ResponseEntity.ok(new VerifyEmailAuthDto(0));
        }

        else {
            // 인증번호가 틀린 경우
            // verified: 2 반환
            System.out.println("인증 실패 - 코드 불일치");
            return ResponseEntity.ok(new VerifyEmailAuthDto(2));
        }
    }


    @PostMapping("/resetPassword") // 프런트엔드에서 전달받은 이메일로 새로운 비밀번호를 발송하는 컨트롤러
    public ResponseEntity<AuthEmailRequestDto> resetPassword(@RequestParam(name = "email") String email) throws Exception {

        System.out.println("email = " + email);

        try {
            UserDto existingUser = userService.findByEmail(email);

            if (existingUser != null) { // 이미 존재하는 회원 email이라면
                AuthEmailRequestDto authEmailRequestDto = mailService.sendResetPasswordMessage(email); // 전달받은 email로 메일 발송
                return ResponseEntity.ok(authEmailRequestDto); // authEmailRequestDto엔 true가 담겨있을 것
            }
        } catch (AppException ex) {
            // 존재하지 않는 회원 email이라면(userService.findByEmail 메서드 보면, 회원이 없으면 예외를 반환함)
            // 예외가 왔다는 건, 해당 email을 가진 회원이 없다는 것이므로 비밀번호 재설정 메일을 발송하면 안됨

            return ResponseEntity.ok(new AuthEmailRequestDto(false)); // 이메일 발송하지 않고, 프런트에 false를 반환
        }

        // 그 외의 경우는 존재할 수 없는데, 혹시 몰라서 server error처리하기
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
