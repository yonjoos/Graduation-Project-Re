package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.Role;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.mapper.UserMapper;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
// 로그인 및 회원 등록을 처리
public class UserService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Storage storage;
    @Value("${spring.cloud.gcp.storage.bucket}") // application.yml에 써둔 bucket 이름
    private String bucketName;



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


    public void signOut(String userEmail, String currentPasswordForSignOut) {
        // userEmail에 해당하는 user를 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // SignOutDto에서 가져온 기존 비밀번호를 암호화해보기
        // user 테이블의 암호화되어 저장되어있는 비밀번호와 currentPasswordForSignOut 암호화 한 값이 같은 경우, 탈퇴 가능
        // 만약 두 값이 다르다면 비밀번호 비밀번호가 틀린 것이므로 예외 반환
        if (!passwordEncoder.matches(currentPasswordForSignOut, user.getPassword())) {
            throw new AppException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        // 회원의 포트폴리오 찾기
        Portfolio portfolio = user.getPortfolio();

        if (portfolio != null) {
            // 포트폴리오가 존재하면, 포트폴리오 먼저 삭제
            portfolioRepository.delete(portfolio);
        }

        // 회원 삭제
        userRepository.delete(user);
    }


    public void updateUserBaseInfo(String userEmail, UserBaseInfoUpdateDto updateDto) {

        // userEmail에 해당하는 user를 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // updateDto에서 가져온 비밀번호를 암호화
        // user 테이블의 암호화되어 저장되어있는 비밀번호가 같은 경우, 닉네임 또는 이름 변경 가능(닉네임은 중복 불가능하므로 닉네임이 중복되면 예외 반환)
        if (passwordEncoder.matches(CharBuffer.wrap(updateDto.getPassword()), user.getPassword())) {

            // 변경하려는 닉네임이 이미 다른 사용자에게서 사용되는지 확인
            Optional<User> existingUserWithNickname = userRepository.findByNickName(updateDto.getNickName());

            //만약 같은 닉네임을 쓰는 회원이 존재하고, 기존에 해당 닉네임을 쓰는 사람이 현재 같은 닉네임으로 바꾸려는 사람의 pk가 다르다면, 이를 거부
//            if (existingUserWithNickname.isPresent() && !existingUserWithNickname.get().getId().equals(user.getId())) {
//                throw new AppException("Nickname already in use", HttpStatus.CONFLICT);
//            }

            //만약 같은 닉네임을 쓰는 회원이 존재하고, 기존에 해당 닉네임을 쓰는 사람이 현재 같은 닉네임으로 바꾸려는 사람의 pk가 다르다면, 이를 거부
            //위의 코드를 아래 코드로 리팩터링
            existingUserWithNickname.ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new AppException("Nickname already in use", HttpStatus.CONFLICT);
                }
            });

            String fileUrl = user.getFileUrl();
            if(fileUrl == null || fileUrl.isEmpty()){
                throw new IllegalArgumentException("No file was uploaded to the server.");
            }
            MultipartFile image = updateDto.getImageUrl();

            String uuid = UUID.randomUUID().toString(); // Google Cloud Storage에 저장될 파일 이름
            System.out.println("uuid = " + uuid);
            String ext = image.getContentType(); // 파일의 형식 ex) JPG
            System.out.println("ext = " + ext);

            try {
                System.out.println("업로드 Blob 전");
                BlobInfo blobInfo = storage.create(
                        BlobInfo.newBuilder(bucketName, uuid)
                                .setContentType(ext)
                                .build(),
                        image.getInputStream()
                );
                System.out.println("업로드 Blob 후");

                user.setfileUrl(uuid);
                user.setfileName(image.getOriginalFilename());
            } catch (IOException e) {
                log.error("Error during file upload: " + e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error: " + e.getMessage());
            }
            System.out.println("업로드 Blob 후");

            user.setfileUrl(uuid);
            user.setfileName(image.getOriginalFilename());


            user.setNickName(updateDto.getNickName()); //변경 감지에 의해 닉네임이 변경되도록 함
            user.setUserName(updateDto.getUserName()); //변경 감지에 의해 이름이 변경되도록 함
            userRepository.save(user); //변경감지 기능 통해 업데이트
        } else {
            throw new AppException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }
    }


    public void updateUserPassword(String userEmail, String currentPassword, String password) {
        //userEmail에 해당하는 회원 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("User not found",HttpStatus.NOT_FOUND));

        // UserPasswordUpdateDto에서 가져온 기존 비밀번호를 암호화해보기
        // user 테이블의 암호화되어 저장되어있는 비밀번호와 currentPassword를 암호화 한 값이 같은 경우, 비밀번호 변경 가능
        // 만약 두 값이 다르다면 비밀번호 비밀번호가 틀린 것이므로 예외 반환
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AppException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        // 패스워드 변경
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(password))); //변경 감지에 의해 패스워드가 변경되도록 함
        userRepository.save(user); //변경감지 기능 통해 업데이트
    }

    @Transactional(readOnly = true) //단순 조회 후에 중복 여부 값만 반환하므로
    public boolean isNicknameAvailable(String nickname) {

        //사용자에게 입력받은 nickname을 기반으로 같은 닉네임을 가진 사용자를 찾아보기
        Optional<User> existingUserWithNickname = userRepository.findByNickName(nickname);

        return existingUserWithNickname.isEmpty(); //비어있으면 available: true / 비어있지 않으면 available: false
    }


    @Transactional(readOnly = true)
    public UserDto getUserProfileImage(Principal principal){
        String email = principal.getName();
        User user = userRepository.findByEmail(email).get();
        String url = user.getFileUrl();

        UserDto userDto = UserDto.builder()
                .imageUrl(url)
                .build();

        return userDto;
    }

    public void uploadProfileImage(ProfileImageUploadDTO updateDto, Principal principal)
            throws IOException, FileNotFoundException {

        System.out.println("업로드 업로드함수입성");
        String email = principal.getName();
        User user = null;

        try {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException("그런 유저가 없다", HttpStatus.NOT_FOUND));
            System.out.println("업로드 트라이 입성");
        } catch (AppException e) {
            log.info(e.getMessage());
            System.out.println("=====================================================");
            System.out.println("UserService error " + e.getMessage());
            System.out.println("업로드 캐치걸림");
        }


        System.out.println("업로드 A");
        String fileUrl = updateDto.getImageUrl().toString();
        System.out.println("업로드 A-" + fileUrl);
        if(fileUrl == null || fileUrl.isEmpty()){
            System.out.println("업로드 B");
            throw new IllegalArgumentException("No file was uploaded to the server.");
        }


        System.out.println("업로드 C");
        MultipartFile image = updateDto.getImageUrl();
        System.out.println("업로드 D");

        String uuid = UUID.randomUUID().toString(); // Google Cloud Storage에 저장될 파일 이름
        System.out.println("uuid = " + uuid);
        String ext = image.getContentType(); // 파일의 형식 ex) JPG
        System.out.println("ext = " + ext);

        System.out.println("업로드 그 뭐냐 ext");
        try {
            System.out.println("업로드 Blob 전");
            BlobInfo blobInfo = storage.create(
                    BlobInfo.newBuilder(bucketName, uuid)
                            .setContentType(ext)
                            .build(),
                    image.getInputStream()
            );
            System.out.println("업로드 Blob 후");

            user.setfileUrl(uuid);
            user.setfileName(image.getOriginalFilename());
        } catch (IOException e) {
            log.error("Error during file upload: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
        }
        System.out.println("업로드 드");

    }
}