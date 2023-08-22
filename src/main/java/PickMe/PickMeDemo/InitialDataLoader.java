package PickMe.PickMeDemo;

import PickMe.PickMeDemo.entity.Role;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.CharBuffer;

@Component
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 초기 데이터 생성 및 저장(관리자)
        User adminUser = User.builder()
                .userName("admin")
                .nickName("admin")
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("admin"))  // 비밀번호 해싱
                .role(Role.ADMIN)
                .build();

        // 초기 데이터 생성 및 저장(유저)
        User generalUser = User.builder()
                        .userName("user")
                        .nickName("user")
                        .email("user@gmail.com")
                        .password(passwordEncoder.encode("user"))  // 비밀번호 해싱
                        .role(Role.USER)
                        .build();



        userRepository.save(adminUser);
        userRepository.save(generalUser);
    }
}
