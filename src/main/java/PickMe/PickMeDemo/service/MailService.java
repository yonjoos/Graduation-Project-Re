package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.AuthEmailCodeDto;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Random;


@Service
@Transactional
public class MailService {

    @Autowired
    JavaMailSender emailSender; // Bean 등록해둔 MailConfig 를 emailSender 라는 이름으로 autowired

    @Value("${mail.username}")
    private String configEmail;

    private String ePw; // 인증번호

    // 생성자를 통해 ePw 초기화
    public MailService() {
        this.ePw = createKey();
    }

    // Method to get the generated authentication code
    public String getAuthenticationCode() {
        return ePw;
    }

    // 메일 내용 작성

    public MimeMessage createMessage(String to, String ePw) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = emailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to);// 보내는 대상
        message.setSubject("P!ck Me 회원가입 이메일 인증");// 제목

        String msgg = "";
        msgg += "<div style='margin:100px;'>";
        msgg += "<h1> 안녕하세요</h1>";
        msgg += "<h1> P!ck Me 운영진 입니다</h1>";
        msgg += "CODE : <strong>";
        msgg += ePw + "</strong><div><br/> ";
        msgg += "</div>";
        message.setText(msgg, "utf-8", "html");// 내용, charset 타입, subtype
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

    public AuthEmailCodeDto sendSimpleMessage(String to) throws Exception {
        // 랜덤 인증번호 생성
        // Generate random authentication number
        String generatedEpw = createKey(); // Generate a new authentication code for each email
        MimeMessage message = createMessage(to, generatedEpw); // Send mail with the new authentication code
        try {// 예외처리
            emailSender.send(message);
        } catch (MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }

        return new AuthEmailCodeDto(generatedEpw); // 메일로 보냈던 인증 코드를 서버로 반환
    }
}
