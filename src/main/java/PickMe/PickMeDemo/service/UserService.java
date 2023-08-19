package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.CredentialsDto;
import PickMe.PickMeDemo.dto.LogoutDto;
import PickMe.PickMeDemo.dto.SignUpDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.mapper.UserMapper;
import PickMe.PickMeDemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@RequiredArgsConstructor
@Service
// 로그인 및 회원 등록을 처리
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // PasswordEncoder를 사용하여 비밀번호가 일반 텍스트로 저장되는 것을 방지하지만 해싱된 비밀번호는 읽을 수 없음.
    private final UserMapper userMapper;

    public UserDto login(CredentialsDto credentialsDto) {
        // 인자로 넘어온 credentialsDto(아이디, 비번) 중 아이디를 찾아서 로그인 시도. 아이디가 없으면 Exception 발생
        User user = userRepository.findByLogin(credentialsDto.getLogin())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        // 해당 아이디를 가진 유저를 살피되,
        // 입력한 비밀번호를 해싱해서 해싱된 비밀번호가 저장되어있는 해싱된 비밀번호와 동일한지 확인
        // user를 userDto로 변환하여 반환
        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {
            return userMapper.toUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    public UserDto register(SignUpDto userDto) {
        // 동일한 아이디가 있는지 확인(?)
        Optional<User> optionalUser = userRepository.findByLogin(userDto.getLogin());

        if (optionalUser.isPresent()) {
            throw new AppException("Login already exists", HttpStatus.BAD_REQUEST);
        }

        // 비밀번호 필드를 제외한 나머지 부분 채우기
        User user = userMapper.signUpToUser(userDto);
        
        // 암호 인코더를 사용하여 암호를 일반 텍스트로 저장하지 않고, 해싱함.
        // 응용 프로그램에서 암호로 작업할 때 해싱은 매우 중요함.
        // 비밀번호는 따로 해싱하여 세팅
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.getPassword())));

        // 해싱된 비밀번호와 나머지 필드들이 저장된 user를 디비에 저장
        User savedUser = userRepository.save(user);

        // 저장된 유저를 userDto로 변환하여 리턴
        return userMapper.toUserDto(savedUser);
    }

    public UserDto findByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        return userMapper.toUserDto(user);
    }
}