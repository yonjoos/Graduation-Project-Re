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

        // 초기 데이터 생성 및 저장(1)
        User user1 = User.builder()
                .userName("1")
                .nickName("1")
                .email("1")
                .password(passwordEncoder.encode("1"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        // 초기 데이터 생성 및 저장(2)
        User user2 = User.builder()
                .userName("2")
                .nickName("2")
                .email("2")
                .password(passwordEncoder.encode("2"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        // 초기 데이터 생성 및 저장(3)
        User user3 = User.builder()
                .userName("3")
                .nickName("3")
                .email("3")
                .password(passwordEncoder.encode("3"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(adminUser);
        userRepository.save(generalUser);
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
    }
}
