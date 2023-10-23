package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.config.RedisUtil;
import PickMe.PickMeDemo.dto.AuthEmailRequestDto;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.UserRepository;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.Optional;
import java.util.Random;


@Service
@Transactional
public class MailService {

    @Autowired JavaMailSender emailSender; // Bean 등록해둔 MailConfig 를 emailSender 라는 이름으로 autowired

    @Autowired RedisUtil redisUtil;

    // @Autowired 대신 @RequiredArgsConstructor 쓰고 private final로 바꾸면 안되는건가?
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // PasswordEncoder를 사용하여 비밀번호가 일반 텍스트로 저장되는 것을 방지하지만 해싱된 비밀번호는 읽을 수 없음.

    @Value("${mail.username}")
    private String configEmail;

    // 인증 코드 메일 내용 작성
    public MimeMessage createMessage(String to, String ePw) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to);// 보내는 대상
        message.setSubject("<P!ck Me> 회원가입 - 인증번호");// 제목

        // Use HTML and inline CSS for formatting
        String msgg = "<html><body style='font-family: Arial, sans-serif; margin: 0; padding: 20px; text-align: center;'>";
        msgg += "<h1 style='color: #007BFF;'>안녕하세요,</h1>";
        msgg += "<p style='font-size: 18px;'>P!ck Me에 오신 걸 환영합니다! 회원 가입을 위해 다음 인증 코드를 10분 내에 입력해주세요:</p>";
        msgg += "<p style='font-size: 24px; font-weight: bold; color: #007BFF;'>" + ePw + "</p>";
        msgg += "<p style='font-size: 18px;'>P!ck Me에 방문해주셔서 감사합니다. 운영진 드림</p>";
        msgg += "</body></html>";

        message.setContent(msgg, "text/html; charset=utf-8");
        // 보내는 사람의 이메일 주소, 보내는 사람 이름
        message.setFrom(new InternetAddress(configEmail, "P!ck Me"));// 보내는 사람

        return message;
    }

    // 랜덤 인증 코드 생성
    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤, rnd 값에 따라서 아래 switch 문이 실행됨

            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    // a~z (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    // A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }

        return key.toString();
    }

    // 메일 발송
    // sendSimpleMessage 의 매개변수로 들어온 to 는 곧 이메일 주소가 되고,
    // MimeMessage 객체 안에 내가 전송할 메일의 내용을 담는다.
    // 그리고 bean 으로 등록해둔 javaMail 객체를 사용해서 이메일 send!!
    public AuthEmailRequestDto sendSimpleMessage(String to) throws Exception {

        // 만약 redis에 해당 email에 대한 정보가 있다면, 데이터 파기 -> why? 이메일 인증메일 버튼을 두번 이상 누를수도 있기 떄문
        // 그 경우, redis에 저장되어있는 key-value를 명시적으로 삭제해줘야함
        if (redisUtil.existData(to)) {
            redisUtil.deleteData(to);
        }

        String generatedEpw = createKey(); // 인증코드 생성

        MimeMessage message = createMessage(to, generatedEpw); // 메일 본문 작성

        redisUtil.setDataExpire(to,generatedEpw,60*2L); // 10분이 지나면 인증코드 파기

        try {
            emailSender.send(message); // 메일 발송하기
        } catch (MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }

        return new AuthEmailRequestDto(true); // 프론트에 메일 발송 완료되었다고 알림
    }

    // 인증 번호 검증
    public Integer verifyEmailCode(String email, String code) {

        String codeFoundByEmail = redisUtil.getData(email); // redis에 해당 email-인증번호가 인증시간 내에 있는지 확인
        System.out.println("codeFoundByEmail = " + codeFoundByEmail);


        if (codeFoundByEmail == null) {  //만약 redis에 없다면 -> 인증 시간 초과된 것
            return 0;
        }

        // redis에 있고, 입력받은 code와 실제 인증번호가 같은지 비교
        // 일치하는 경우
        if(codeFoundByEmail.equals(code))
        {
            return 1;
        }

        // 일치하지 않는 경우
        else {
            return 2;
        }

    }

    // redis에서 해당 email-인증코드 값 지우기
    public void delete(String key) {
        redisUtil.deleteData(key);
    }



    // 비밀번호 재설정 메일 내용 작성
    public MimeMessage createPasswordMessage(String to, String ePw) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to);// 보내는 대상
        message.setSubject("<P!ck Me> 새로운 비밀번호");// 제목

        // Use HTML and inline CSS for formatting
        String msgg = "<html><body style='font-family: Arial, sans-serif; margin: 0; padding: 20px; text-align: center;'>";
        msgg += "<h1 style='color: #007BFF;'>안녕하세요,</h1>";
        msgg += "<p style='font-size: 18px;'>P!ck Me에 오신 걸 환영합니다! 새롭게 설정된 비밀번호입니다:</p>";
        msgg += "<p style='font-size: 24px; font-weight: bold; color: #007BFF;'>" + ePw + "</p>";
        msgg += "<p style='font-size: 18px;'>P!ck Me에 방문해주셔서 감사합니다. 운영진 드림</p>";
        msgg += "</body></html>";

        message.setContent(msgg, "text/html; charset=utf-8");
        // 보내는 사람의 이메일 주소, 보내는 사람 이름
        message.setFrom(new InternetAddress(configEmail, "P!ck Me"));// 보내는 사람

        return message;
    }

    // 랜덤 인증 코드 생성
    public static String createPassword() {
        StringBuffer password = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 10; i++) { // 비밀번호 10자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤, rnd 값에 따라서 아래 switch 문이 실행됨

            switch (index) {
                case 0:
                    password.append((char) ((int) (rnd.nextInt(26)) + 97));
                    // a~z (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    password.append((char) ((int) (rnd.nextInt(26)) + 65));
                    // A~Z
                    break;
                case 2:
                    password.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }

        return password.toString();
    }

    // 메일 발송
    // sendSimpleMessage 의 매개변수로 들어온 to 는 곧 이메일 주소가 되고,
    // MimeMessage 객체 안에 내가 전송할 메일의 내용을 담는다.
    // 그리고 bean 으로 등록해둔 javaMail 객체를 사용해서 이메일 send!!
    public AuthEmailRequestDto sendResetPasswordMessage(String to) throws Exception {

        // 만약 redis에 해당 email에 대한 정보가 있다면, 데이터 파기 -> why? 이메일 인증메일 버튼을 두번 이상 누를수도 있기 떄문
        // 그 경우, redis에 저장되어있는 key-value를 명시적으로 삭제해줘야함
        if (redisUtil.existData(to)) {
            redisUtil.deleteData(to);
        }

        String generatedPassword = createPassword(); // 비밀번호 생성

        MimeMessage message = createPasswordMessage(to, generatedPassword); // 메일 본문 작성

        try {
            emailSender.send(message); // 메일 발송하기

            // 생성된 비밀번호를 유저의 새로운 비밀번호로 저장
            User findUser = userRepository.findByEmail(to)
                    .orElseThrow(() -> new AppException("유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

            findUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(generatedPassword)));

            userRepository.save(findUser);
        } catch (MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }

        return new AuthEmailRequestDto(true); // 프론트에 메일 발송 완료되었다고 알림
    }
}
