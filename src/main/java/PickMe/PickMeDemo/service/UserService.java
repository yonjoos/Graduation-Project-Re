package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.*;
import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.QUser;
import PickMe.PickMeDemo.entity.Role;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.mapper.UserMapper;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
// 로그인 및 회원 등록을 처리
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PasswordEncoder passwordEncoder;  // PasswordEncoder를 사용하여 비밀번호가 일반 텍스트로 저장되는 것을 방지하지만 해싱된 비밀번호는 읽을 수 없음.
    private final UserMapper userMapper;
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;



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


    /*
    ############################## Services for Recommendation ######################################
    ############################## Services for Recommendation ######################################
    ############################## Services for Recommendation ######################################
     */
    public Optional<UserRecommendationDto> getUserForRecommendation(final Long userId){

        User user = userRepository.findById(userId).get();

        UserRecommendationDto userRecommendationDto = UserRecommendationDto.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .fieldsOfInterests(user.getFieldsOfInterests())
                .build();

        return Optional.ofNullable(userRecommendationDto);

    }

    public List<UserRecommendationDto> getUserForRecommendation2(final String nickName){

        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");
        System.out.println("=========A getUserForRecommendation2===========================");


        User user = userRepository.findByNickName(nickName).get();
        Long userId = user.getId();

        UserRecommendationDto userRecommendationDto = UserRecommendationDto.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .fieldsOfInterests(user.getFieldsOfInterests())
                .build();

        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");
        System.out.println("=========B getUserForRecommendation2===========================");

        List<Long> randomUserIds = fetchRandomUserIds2(userId);

        System.out.println("=========CB getUserForRecoxmmendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");
        System.out.println("=========CB getUserForRecommendation2===========================");

        List<UserRecommendationDto> recommendedDtos = calculateSimilarity(user, randomUserIds, 3);



        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");
        System.out.println("=========C getUserForRecommendation2===========================");

        return recommendedDtos;

    }

    private List<Long> fetchRandomUserIds(Long targetUserId) {
        QUser user = QUser.user;
        List<Long> randomUserIds = getRandomUserIds(targetUserId, 5);

        return randomUserIds;
    }

    public List<Long> getRandomUserIds(Long targetUserId, int count) {
        // Fetch 10 random user IDs (excluding the target user if necessary)
        String sql = "SELECT id FROM users WHERE id != ? ORDER BY random() LIMIT ?";
        List<Long> randomUserIds = jdbcTemplate.queryForList(sql, Long.class, targetUserId, count);

        return randomUserIds;
    }

    private List<UserRecommendationDto> calculateSimilarity(User targetUser, List<Long> userIds, int topN) {
        int[] target = targetUser.getFieldsOfInterests();

        List<UserRecommendationDto> users = userIds.stream()
                .map(userId -> getUserForRecommendation(userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<UserSimilarity> userSimilarities = new ArrayList<>();
        for (UserRecommendationDto user : users) {
            int[] userVector = user.getFieldsOfInterests();
            double similarity = calculateCosineSimilarity(target, userVector); // Calculate similarity for each user
            userSimilarities.add(new UserSimilarity(user, similarity));
        }

        Collections.sort(userSimilarities, (a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));
        List<UserSimilarity> top3Similarities = userSimilarities.subList(0, Math.min(3, userSimilarities.size()));

        // Extract the UserRecommendationDto objects from the top 3 similarities
        List<UserRecommendationDto> top3Users = top3Similarities.stream()
                .map(UserSimilarity::getUser)
                .collect(Collectors.toList());

        return top3Users;


        // Now you can proceed to sort and select the top N users based on userSimilarities
    }

    public static double calculateCosineSimilarity(int[] targetUser, int[] userVector) {
        double dotProduct = 0;
        double magnitudeA = 0;
        double magnitudeB = 0;

        for (int i = 0; i < targetUser.length; i++) {
            dotProduct += targetUser[i] * userVector[i];
            magnitudeA += Math.pow(targetUser[i], 2);
            magnitudeB += Math.pow(userVector[i], 2);
        }

        // Calculate the cosine similarity
        double similarity;
        if (magnitudeA == 0 || magnitudeB == 0) {
            // Handle the case of zero magnitude (avoid division by zero)
            similarity = 0.0;
        } else {
            similarity = dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
        }

        return similarity;
    }



    private static class UserSimilarity {
        private final UserRecommendationDto user;
        private final double similarity;

        public UserSimilarity(UserRecommendationDto user, double similarity) {
            this.user = user;
            this.similarity = similarity;
        }

        public UserRecommendationDto getUser() {
            return user;
        }

        public double getSimilarity() {
            return similarity;
        }
    }







    private Random random = new Random();
    private List<Long> fetchRandomUserIds2(Long targetUserId) {

        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");
        System.out.println("=========B-A getUserForRecommendation2===========================");


        // Generate random integers in Java
        List<Integer> randomIntegers = new ArrayList<>();
        int count = 5; // Number of random IDs to generate

        while (randomIntegers.size() < count) {
            System.out.println("=========B-B getUserForRecommendation2===========================");
            System.out.println("=========B-B getUserForRecommendation2===========================");
            System.out.println("=========B-B getUserForRecommendation2===========================");
            System.out.println("=========B-B getUserForRecommendation2===========================");
            System.out.println("=========B-B getUserForRecommendation2===========================");
            System.out.println("=========B-B getUserForRecommendation2===========================");

            int randomInt = getRandomInt(targetUserId.intValue()); // Generate a random integer
            if (randomInt != targetUserId.intValue() && !randomIntegers.contains(randomInt)) {
                randomIntegers.add(randomInt);
            }
        }

        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");
        System.out.println("=========B-C getUserForRecommendation2===========================");




        // Convert the list of random integers to a list of Longs
        List<Long> randomUserIds = randomIntegers.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());




        return randomUserIds;
    }

    private int getRandomInt(int max) {
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");
        System.out.println("=========B-B-A getUserForRecommendation2===========================");


        return random.nextInt(max) + 1;
    }



}
