package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.AuthEmailCodeDto;
import PickMe.PickMeDemo.dto.VerifyEmailAuthDto;
import PickMe.PickMeDemo.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("/mailConfirm")
    public ResponseEntity<AuthEmailCodeDto> mailConfirm(@RequestParam(name = "email") String email) throws Exception {

        System.out.println("email = " + email);
        AuthEmailCodeDto authEmailCodeDto = mailService.sendSimpleMessage(email);
        System.out.println("인증코드 : " + authEmailCodeDto.getCode());
        return ResponseEntity.ok(authEmailCodeDto);
    }
    @PostMapping("/verifyCode")

    public ResponseEntity<VerifyEmailAuthDto> verifyCode(@RequestParam("code") String code) {

        System.out.println("code : "+code);
        System.out.println("mailService = " + mailService.getAuthenticationCode());
        System.out.println("code match : "+ mailService.getAuthenticationCode().equals(code));
        VerifyEmailAuthDto verifyEmailAuthDto;

        if(mailService.getAuthenticationCode().equals(code)) {
            verifyEmailAuthDto = VerifyEmailAuthDto.builder()
                                                    .verified(true)
                                                    .build();
        }
        else {
            verifyEmailAuthDto = VerifyEmailAuthDto.builder()
                                                    .verified(false)
                                                    .build();
        }
        return ResponseEntity.ok(verifyEmailAuthDto);
    }
}
