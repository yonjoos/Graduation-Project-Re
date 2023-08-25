package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.CredentialsDto;
import PickMe.PickMeDemo.dto.SignUpDto;
import PickMe.PickMeDemo.dto.UserBaseInfoUpdateDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.Role;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.mapper.UserMapper;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
// 로그인 및 회원 등록을 처리
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PasswordEncoder passwordEncoder;  // PasswordEncoder를 사용하여 비밀번호가 일반 텍스트로 저장되는 것을 방지하지만 해싱된 비밀번호는 읽을 수 없음.
    private final UserMapper userMapper;

    public UserDto login(CredentialsDto credentialsDto) {
        // 인자로 넘어온 credentialsDto(아이디, 비번) 중 아이디를 찾아서 로그인 시도. 아이디가 없으면 Exception 발생
        User user = userRepository.findByEmail(credentialsDto.getEmail())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        // 해당 아이디를 가진 유저를 살피되,
        // 입력한 비밀번호를 해싱해서 해싱된 비밀번호가 저장되어있는 해싱된 비밀번호와 동일한지 확인
        // user를 userDto로 변환하여 반환
        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {

            // Update the access date for the user and save the changes
            user.modifyLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
            return userMapper.toUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    public UserDto register(SignUpDto userDto) {
        // 동일한 아이디가 있는지 확인
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());

        if (optionalUser.isPresent()) {
            throw new AppException("Login already exists", HttpStatus.BAD_REQUEST);
        }

        // 비밀번호 필드를 제외한 나머지 부분 채우기
        User user = userMapper.signUpToUser(userDto);
        
        // 암호 인코더를 사용하여 암호를 일반 텍스트로 저장하지 않고, 해싱함.
        // 응용 프로그램에서 암호로 작업할 때 해싱은 매우 중요함.
        // 비밀번호는 따로 해싱하여 세팅
        //user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.getPassword())));
        //실제 user 객체 구체화 진행
        User registerUser =
                new User(user.getId(), user.getUserName(), user.getNickName(), user.getEmail(),
                        passwordEncoder.encode(CharBuffer.wrap(userDto.getPassword())), Role.USER, null);
        //회원가입 시엔 최근 접속일자 필드를 null로 세팅함!!
        //user.setRole(Role.USER);

        // 해싱된 비밀번호와 나머지 필드들이 저장된 user를 디비에 저장
        User savedUser = userRepository.save(registerUser);
        //User savedUser = userRepository.save(user);

        // 저장된 유저를 userDto로 변환하여 리턴
        return userMapper.toUserDto(savedUser);
    }

    //email로 레포지토리에서 찾고, userDto로 user를 매핑해서 반환
    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        return userMapper.toUserDto(user);
    }


    public void signOut(String userEmail) {
        // userEmail로 user 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // 회원의 포트폴리오 찾기
        Portfolio portfolio = user.getPortfolio();

        if (portfolio != null) {
            // 포트폴리오가 존재하면, 포트폴리오 먼저 삭제
            portfolioRepository.delete(portfolio);
        }

        // 회원 삭제
        userRepository.delete(user);
    }


    @Transactional
    public void updateUserBaseInfo(String userEmail, UserBaseInfoUpdateDto updateDto) {

        // userEmail에 해당하는 user를 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("User not found",HttpStatus.NOT_FOUND));

        // updateDto에서 가져온 비밀번호를 암호화
        // user 테이블의 암호화되어 저장되어있는 비밀번호가 같은 경우
        if (passwordEncoder.matches(CharBuffer.wrap(updateDto.getPassword()), user.getPassword())) {

            // 변경하려는 닉네임이 이미 다른 사용자에게서 사용되는지 확인
            Optional<User> existingUserWithNickname = userRepository.findByNickName(updateDto.getNickName());

            //만약 같은 닉네임을 쓰는 회원이 존재하고, 기존에 해당 닉네임을 쓰는 사람이 현재 같은 닉네임으로 바꾸려는 사람의 pk가 다르다면, 이를 거부
            if (existingUserWithNickname.isPresent() && !existingUserWithNickname.get().getId().equals(user.getId())) {
                throw new AppException("Nickname already in use", HttpStatus.CONFLICT);
            }

            user.setNickName(updateDto.getNickName());
            user.setUserName(updateDto.getUserName());
            userRepository.save(user); //변경감지 기능 통해 업데이트 되게
        } else {
            throw new AppException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }
    }
}