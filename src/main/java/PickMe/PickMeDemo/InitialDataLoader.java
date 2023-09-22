package PickMe.PickMeDemo;

import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.repository.CategoryRepository;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.PostsRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.CharBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortfolioRepository portfolioRepository;
    private final PostsRepository postsRepository;
    private final CategoryRepository categoryRepository;

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

        userRepository.save(adminUser);


        // 초기 데이터 생성 및 저장(유저)
        User generalUser = User.builder()
                        .userName("user")
                        .nickName("user")
                        .email("user@gmail.com")
                        .password(passwordEncoder.encode("user"))  // 비밀번호 해싱
                        .role(Role.USER)
                        .build();

        userRepository.save(generalUser);

        // 포트폴리오 생성자
        // public Portfolio(User user, Integer web, Integer app, Integer game, Integer ai, String shortIntroduce, String introduce, String fileUrl)
        Portfolio generalPortfolio = Portfolio.builder()
                .user(generalUser)
                .web(4)
                .app(3)
                .game(2)
                .ai(1)
                .shortIntroduce("안녕하세요, 웹과 앱에 관심있는 코딩 꿈나무입니다.")
                .introduce("- 맛있홍 프로젝트 (React + Node.js + Express.js)" +
                        "\n- 픽미 프로젝트 (React + SpringBoot + JPA)")
                .fileUrl("")
                .build();


        portfolioRepository.save(generalPortfolio);



        // 초기 데이터 생성 및 저장(1)
        // 모든 값이 비어있어서 생성 실험 가능한 유저
        User user1 = User.builder()
                .userName("Test")
                .nickName("Test")
                .email("1")
                .password(passwordEncoder.encode("1"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user1);


        // 초기 데이터 생성 및 저장(2)
        User user2 = User.builder()
                .userName("이윤식")
                .nickName("rilato")
                .email("2")
                .password(passwordEncoder.encode("2"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user2);

        Portfolio user2Portfolio = Portfolio.builder()
                .user(user2)
                .web(4)
                .app(0)
                .game(3)
                .ai(0)
                .shortIntroduce("안녕하세요, 웹과 앱에 관심있는 코딩 꿈나무입니다.")
                .introduce("- 맛있홍 프로젝트 (React + Node.js + Express.js) \n- 픽미 프로젝트 (React + SpringBoot + JPA) \n- 코로나 보드 크롤링 프로젝트(Node.js + Express.js)")
                .fileUrl("")
                .build();
        
        portfolioRepository.save(user2Portfolio);

        String initialEndDate1 = "2023-09-30"; // 원하는 종료 날짜를 스트링으로 받음
        DateTimeFormatter dateFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");   // 날짜 포맷터를 사용하여 날짜 문자열을 'LocalDate' 개체로 변환
        LocalDate endDate1 = LocalDate.parse(initialEndDate1, dateFormatter1);

        // Posts 생성자 : (User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate)
        Posts posts1 = Posts.builder()
                .user(user2)
                .postType(PostType.PROJECT)
                .title("졸프 팀원 구해요~")
                .recruitmentCount(3)
                //.counts(1)
                .content("졸업 프로젝트 팀원을 모집합니다.\n현재 저는 풀스택 개발 가능하고, Spring Boot 가능한 백엔드 개발자 한 분과, React 및 Redux 사용 가능한 프론트 개발자 두 분을 모십니다.\n언제든지 연락 주세요!")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate1)
                .build();

        postsRepository.save(posts1);

        Category category1 = Category.builder()
                .posts(posts1)
                .web(true)
                .app(false)
                .game(false)
                .ai(true)
                .build();

        category1.validateFieldCount();
        categoryRepository.save(category1);

        String initialEndDate2 = "2023-10-10";
        DateTimeFormatter dateFormatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate2 = LocalDate.parse(initialEndDate2, dateFormatter2);

        Posts posts2 = Posts.builder()
                .user(user2)
                .postType(PostType.PROJECT)
                .title("토이 플젝 하실분?")
                .recruitmentCount(2)
                //.counts(1)
                .content("토이 프로젝트 팀원을 모집합니다.\n주제는 아직 정해지지 않았습니다.\n현재 저는 백엔드 개발 가능하고, React 및 Redux 사용 가능한 프론트 개발자 두 분을 모십니다.")
                .promoteImageUrl("사진 없음")
                .fileUrl("파일 없음")
                .endDate(endDate2)
                .build();

        postsRepository.save(posts2);

        Category category2 = Category.builder()
                .posts(posts2)
                .web(true)
                .app(true)
                .game(false)
                .ai(false)
                .build();

        category2.validateFieldCount();
        categoryRepository.save(category2);

        String initialEndDate3 = "2023-09-16";
        DateTimeFormatter dateFormatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate3 = LocalDate.parse(initialEndDate3, dateFormatter3);

        Posts posts3 = Posts.builder()
                .user(user2)
                .postType(PostType.STUDY)
                .title("배자구 스터디")
                .recruitmentCount(4)
                //.counts(1)
                .content("배자구 스터디 구합니다.\n감자도 환영합니다.\n저도 자구알못이에요..ㅠㅠ\n같이 자구 공부해요!!")
                .promoteImageUrl("사진")
                .fileUrl("파일")
                .endDate(endDate3)
                .build();

        postsRepository.save(posts3);

        Category category3 = Category.builder()
                .posts(posts3)
                .web(false)
                .app(true)
                .game(false)
                .ai(true)
                .build();

        category3.validateFieldCount();
        categoryRepository.save(category3);

        // 초기 데이터 생성 및 저장(3)
        User user3 = User.builder()
                .userName("박시홍")
                .nickName("freshhongsi")
                .email("3")
                .password(passwordEncoder.encode("3"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user3);

        Portfolio user3Portfolio = Portfolio.builder()
                .user(user3)
                .web(4)
                .app(3)
                .game(0)
                .ai(0)
                .shortIntroduce("풀스택 개발자 그 자체, 홍시입니다.")
                .introduce("- 맛있홍 프로젝트 (React + Node.js + Express.js) \n- 픽미 프로젝트 (React + SpringBoot + JPA) \n- 코로나 보드 크롤링 프로젝트(Node.js + Express.js)")
                .fileUrl("")
                .build();

        portfolioRepository.save(user3Portfolio);

        String initialEndDate4 = "2023-09-12"; // 원하는 종료 날짜를 스트링으로 받음
        DateTimeFormatter dateFormatter4 = DateTimeFormatter.ofPattern("yyyy-MM-dd");   // 날짜 포맷터를 사용하여 날짜 문자열을 'LocalDate' 개체로 변환
        LocalDate endDate4 = LocalDate.parse(initialEndDate4, dateFormatter4);

        // Posts 생성자 : (User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate)
        Posts posts4 = Posts.builder()
                .user(user3)
                .postType(PostType.PROJECT)
                .title("권교수님과 졸프 하실분!")
                .recruitmentCount(3)
                //.counts(1)
                .content("권건우 교수님과 함께할 졸업 프로젝트 팀원을 모집합니다.\n주제는 먹거리 관련 입니다.\n현재 저는 풀스택 개발 가능하고, Spring Boot 가능한 백엔드 개발자 한 분과, React 및 Redux 사용 가능한 프론트 개발자 한 분을 모십니다.")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate4)
                .build();

        postsRepository.save(posts4);

        Category category4 = Category.builder()
                .posts(posts4)
                .web(true)
                .app(false)
                .game(false)
                .ai(true)
                .build();

        category4.validateFieldCount();
        categoryRepository.save(category4);

        String initialEndDate5 = "2023-09-23";
        DateTimeFormatter dateFormatter5 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate5 = LocalDate.parse(initialEndDate5, dateFormatter5);

        Posts posts5 = Posts.builder()
                .user(user3)
                .postType(PostType.STUDY)
                .title("알골 스터디 팀원 구함")
                .recruitmentCount(4)
                //.counts(1)
                .content("알고리즘 스터디 구함.\n란골 배골 곤골 모두 환영.\n백준 플래티넘이 목표.\n알고리즘 마스터가 되어봅시다..")
                .promoteImageUrl("사진 없음")
                .fileUrl("파일 없음")
                .endDate(endDate5)
                .build();

        postsRepository.save(posts5);

        Category category5 = Category.builder()
                .posts(posts5)
                .web(true)
                .app(false)
                .game(false)
                .ai(true)
                .build();

        category5.validateFieldCount();
        categoryRepository.save(category5);


        String initialEndDate6 = "2023-09-20";
        DateTimeFormatter dateFormatter6 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate6 = LocalDate.parse(initialEndDate6, dateFormatter6);

        Posts posts6 = Posts.builder()
                .user(user3)
                .postType(PostType.STUDY)
                .title("프린스 송")
                .recruitmentCount(3)
                //.counts(1)
                .content("송프언 프롤로그, 렉스, 야크, 리스프 과제 같이 고민해요!\n열심히 하시는 분들 환영!")
                .promoteImageUrl("사진")
                .fileUrl("파일")
                .endDate(endDate6)
                .build();

        postsRepository.save(posts6);

        Category category6 = Category.builder()
                .posts(posts6)
                .web(true)
                .app(false)
                .game(true)
                .ai(false)
                .build();

        category6.validateFieldCount();
        categoryRepository.save(category6);

        // 초기 데이터 생성 및 저장(4)
        User user4 = User.builder()
                .userName("Black Consumer")
                .nickName("악 성 유 저")
                .email("4")
                .password(passwordEncoder.encode("4"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user4);

        Portfolio user4Portfolio = Portfolio.builder()
                .user(user4)
                .web(0)
                .app(0)
                .game(0)
                .ai(0)
                .shortIntroduce("나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.짧은소개글이지만길게쓴다.왜냐면난악성유저니까...")
                .introduce("나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.경력도길게쓴다.왜냐면난악성유저니까...")
                .fileUrl("")
                .build();

        portfolioRepository.save(user4Portfolio);

        // 악성유저는 모집 기간이 이미 지난 날짜로 세팅되어있음.
        String initialEndDate7 = "2023-08-15"; // 원하는 종료 날짜를 스트링으로 받음
        DateTimeFormatter dateFormatter7 = DateTimeFormatter.ofPattern("yyyy-MM-dd");   // 날짜 포맷터를 사용하여 날짜 문자열을 'LocalDate' 개체로 변환
        LocalDate endDate7 = LocalDate.parse(initialEndDate7, dateFormatter7);

        // Posts 생성자 : (User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate)
        Posts posts7 = Posts.builder()
                .user(user4)
                .postType(PostType.PROJECT)
                .title("나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.제목도길게쓴다.왜냐면난악성유저니까..." +
                        "제목도내맘대로두번이나쓴다ㅋㅋ.나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.제목도길게쓴다.왜냐면난악성유저니까...")
                .recruitmentCount(2)
                //.counts(1)
                .content("나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까...")
                .promoteImageUrl("나는 악성유저지만 여기서는 착하게 굴겠다. 사진은 경로이므로 띄어쓰기나 개행이 들어가면 안될 것 같다.")
                .fileUrl("나는 악성유저지만 여기서는 착하게 굴겠다. 파일은 경로이므로 띄어쓰기나 개행이 들어가면 안될 것 같다.")
                .endDate(endDate7)
                .build();

        postsRepository.save(posts7);

        Category category7 = Category.builder()
                .posts(posts7)
                .web(false)
                .app(true)
                .game(true)
                .ai(false)
                .build();

        category7.validateFieldCount();
        categoryRepository.save(category7);

        String initialEndDate8 = "2023-08-30";
        DateTimeFormatter dateFormatter8 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate8 = LocalDate.parse(initialEndDate8, dateFormatter8);

        Posts posts8 = Posts.builder()
                .user(user4)
                .postType(PostType.STUDY)
                .title("나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.제목도길게쓴다.왜냐면난악성유저니까..." +
                        "제목도내맘대로두번이나쓴다ㅋㅋ.나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.제목도길게쓴다.왜냐면난악성유저니까...")
                .recruitmentCount(3)
                //.counts(1)
                .content("나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까..." +
                        "나는악성유저ㅋㅋㅋㅋ.띄어쓰기없다.일부러없앴다.사이트에오류를만들거다.아무도날막을수없다.내용도길게쓴다.왜냐면난악성유저니까...")
                .promoteImageUrl("나는 악성유저지만 여기서는 착하게 굴겠다. 사진은 경로이므로 띄어쓰기나 개행이 들어가면 안될 것 같다.")
                .fileUrl("나는 악성유저지만 여기서는 착하게 굴겠다. 파일은 경로이므로 띄어쓰기나 개행이 들어가면 안될 것 같다.")
                .endDate(endDate8)
                .build();

        postsRepository.save(posts8);

        Category category8 = Category.builder()
                .posts(posts8)
                .web(true)
                .app(false)
                .game(true)
                .ai(false)
                .build();

        category8.validateFieldCount();
        categoryRepository.save(category8);


        String initialEndDate9 = "2023-09-20";
        DateTimeFormatter dateFormatter9 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate9 = LocalDate.parse(initialEndDate9, dateFormatter9);

        Posts posts9 = Posts.builder()
                .user(user4)
                .postType(PostType.STUDY)
                .title("악성유저의 정상적인 게시물ㅋㅋ")
                .recruitmentCount(3)
                //.counts(1)
                .content("웬일이래? 내가 정상적인 게시물도 달고 말이야. 고마워해라.")
                .promoteImageUrl("")
                .fileUrl("")
                .endDate(endDate9)
                .build();

        postsRepository.save(posts9);

        Category category9 = Category.builder()
                .posts(posts9)
                .web(true)
                .app(false)
                .game(false)
                .ai(true)
                .build();

        category9.validateFieldCount();
        categoryRepository.save(category9);

        // 초기 데이터 생성 및 저장(5)
        User user5 = User.builder()
                .userName("홍길동")
                .nickName("고길동")
                .email("5")
                .password(passwordEncoder.encode("5"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user5);

        Portfolio user5Portfolio = Portfolio.builder()
                .user(user5)
                .web(0)
                .app(0)
                .game(4)
                .ai(3)
                .shortIntroduce("아버지를 아버지라 부르지 못하고..")
                .introduce("- 둘리 프로젝트 (주연) \n- 최초의 한글소설 프로젝트 (주연)")
                .fileUrl("")
                .build();

        portfolioRepository.save(user5Portfolio);

        String initialEndDate10 = "2023-09-20";
        DateTimeFormatter dateFormatter10 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate10 = LocalDate.parse(initialEndDate10, dateFormatter10);

        // Posts 생성자 : (User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate)
        Posts posts10 = Posts.builder()
                .user(user5)
                .postType(PostType.PROJECT)
                .title("게임이나 만들자.")
                .recruitmentCount(2)
                //.counts(1)
                .content("유니티, 언리얼 사용할 줄 아는 사람 환영.\nC# 잘 쓰고 C++ 잘하는 사람도 환영.")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate10)
                .build();

        postsRepository.save(posts10);

        Category category10 = Category.builder()
                .posts(posts10)
                .web(false)
                .app(false)
                .game(true)
                .ai(false)
                .build();

        category10.validateFieldCount();
        categoryRepository.save(category10);

        String initialEndDate11 = "2023-10-01";
        DateTimeFormatter dateFormatter11 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate11 = LocalDate.parse(initialEndDate11, dateFormatter11);

        Posts posts11 = Posts.builder()
                .user(user5)
                .postType(PostType.PROJECT)
                .title("인공지능 마스터")
                .recruitmentCount(3)
                //.counts(1)
                .content("인공지능 잘 활용하시는 분과 함께 프로젝트 하고 싶어요.\n저와 함께 인공지능 마스터가 되어보아요!")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate11)
                .build();

        postsRepository.save(posts11);

        Category category11 = Category.builder()
                .posts(posts11)
                .web(false)
                .app(false)
                .game(false)
                .ai(true)
                .build();

        category11.validateFieldCount();
        categoryRepository.save(category11);


        String initialEndDate12 = "2023-10-05";
        DateTimeFormatter dateFormatter12 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate12 = LocalDate.parse(initialEndDate12, dateFormatter12);

        Posts posts12 = Posts.builder()
                .user(user5)
                .postType(PostType.STUDY)
                .title("유니티랑 C# 같이 공부하실 분 구합니다.")
                .recruitmentCount(5)
                //.counts(1)
                .content("같이 열심히 공부해서, 플젝도 만들어봐요!!\n포트폴리오 열심히 채웁시다..")
                .promoteImageUrl("사진")
                .fileUrl("파일")
                .endDate(endDate12)
                .build();

        postsRepository.save(posts12);

        Category category12 = Category.builder()
                .posts(posts12)
                .web(false)
                .app(false)
                .game(true)
                .ai(true)
                .build();

        category12.validateFieldCount();
        categoryRepository.save(category12);


        // 초기 데이터 생성 및 저장(6)
        User user6 = User.builder()
                .userName("프로젝트")
                .nickName("onlyProject")
                .email("6")
                .password(passwordEncoder.encode("6"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user6);

        Portfolio user6Portfolio = Portfolio.builder()
                .user(user6)
                .web(4)
                .app(0)
                .game(0)
                .ai(0)
                .shortIntroduce("웹 장인")
                .introduce("- 스타트업 인턴 \n- 개인 토이 프로젝트 \n- 기타 등등")
                .fileUrl("")
                .build();

        portfolioRepository.save(user6Portfolio);

        String initialEndDate13 = "2023-09-19";
        DateTimeFormatter dateFormatter13 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate13 = LocalDate.parse(initialEndDate13, dateFormatter13);

        // Posts 생성자 : (User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate)
        Posts posts13 = Posts.builder()
                .user(user6)
                .postType(PostType.PROJECT)
                .title("프로젝트만 만들음.")
                .recruitmentCount(4)
                //.counts(1)
                .content("프로젝트만 모집할거임.\n스터디 모집 안함.\n내 맘임.")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate13)
                .build();

        postsRepository.save(posts13);

        Category category13 = Category.builder()
                .posts(posts13)
                .web(true)
                .app(false)
                .game(false)
                .ai(false)
                .build();

        category13.validateFieldCount();
        categoryRepository.save(category13);

        String initialEndDate14 = "2023-10-30";
        DateTimeFormatter dateFormatter14 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate14 = LocalDate.parse(initialEndDate14, dateFormatter14);

        Posts posts14 = Posts.builder()
                .user(user6)
                .postType(PostType.PROJECT)
                .title("인공지능 활용한 웹 개발")
                .recruitmentCount(3)
                //.counts(1)
                .content("자바 스프링 잘 쓰시는 분 구해요.\n인공지능 잘 활용하시는 분과 함께 프로젝트 하고 싶어요.\n저와 함께 웹 및 인공지능 마스터가 되어보아요!")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate14)
                .build();

        postsRepository.save(posts14);

        Category category14 = Category.builder()
                .posts(posts14)
                .web(true)
                .app(false)
                .game(false)
                .ai(true)
                .build();

        category14.validateFieldCount();
        categoryRepository.save(category14);


        String initialEndDate15 = "2023-10-07";
        DateTimeFormatter dateFormatter15 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate15 = LocalDate.parse(initialEndDate15, dateFormatter15);

        Posts posts15 = Posts.builder()
                .user(user6)
                .postType(PostType.PROJECT)
                .title("제목 뭐로 하지..")
                .recruitmentCount(5)
                //.counts(1)
                .content("그냥 웹 플젝 할거야..\n포트폴리오 채워보자..")
                .promoteImageUrl("사진")
                .fileUrl("파일")
                .endDate(endDate15)
                .build();

        postsRepository.save(posts15);

        Category category15 = Category.builder()
                .posts(posts15)
                .web(true)
                .app(false)
                .game(false)
                .ai(false)
                .build();

        category15.validateFieldCount();
        categoryRepository.save(category15);


        // 초기 데이터 생성 및 저장(7)
        User user7 = User.builder()
                .userName("정연주")
                .nickName("yonjoos")
                .email("7")
                .password(passwordEncoder.encode("7"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user7);

        Portfolio user7Portfolio = Portfolio.builder()
                .user(user7)
                .web(4)
                .app(0)
                .game(0)
                .ai(3)
                .shortIntroduce("시각디자인과에서의 경험을 바탕으로 미적 감각이 뛰어납니다. 하지만 백엔드를 희망합니다.")
                .introduce("- 맛있홍 프로젝트 (React + Node.js + Express.js \n- 픽미 프로젝트 (React + SpringBoot + JPA) \n- 졸업 프로젝트 (Unity)")
                .fileUrl("")
                .build();

        portfolioRepository.save(user7Portfolio);

        String initialEndDate19 = "2023-09-15"; // 원하는 종료 날짜를 스트링으로 받음
        DateTimeFormatter dateFormatter19 = DateTimeFormatter.ofPattern("yyyy-MM-dd");   // 날짜 포맷터를 사용하여 날짜 문자열을 'LocalDate' 개체로 변환
        LocalDate endDate19 = LocalDate.parse(initialEndDate19, dateFormatter19);

        // Posts 생성자 : (User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate)
        Posts posts19 = Posts.builder()
                .user(user7)
                .postType(PostType.PROJECT)
                .title("유니티 활용한 졸프")
                .recruitmentCount(2)
                //.counts(1)
                .content("유니티 잘 쓸 줄 아시는 분 두 분 구해봐용..\nC#도 잘하면 좋아요..")
                .promoteImageUrl("사진 뭐하지")
                .fileUrl("나도 몰라")
                .endDate(endDate19)
                .build();

        postsRepository.save(posts19);

        Category category19 = Category.builder()
                .posts(posts19)
                .web(false)
                .app(false)
                .game(true)
                .ai(true)
                .build();

        category19.validateFieldCount();
        categoryRepository.save(category19);

        String initialEndDate20 = "2023-09-28";
        DateTimeFormatter dateFormatter20 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate20 = LocalDate.parse(initialEndDate20, dateFormatter20);

        Posts posts20 = Posts.builder()
                .user(user7)
                .postType(PostType.STUDY)
                .title("알골 스터디 하실 분~")
                .recruitmentCount(4)
                //.counts(1)
                .content("알고리즘 스터디 구해여.\n매일 백준 한 문제씩 푸는 것이 목표에여.")
                .promoteImageUrl("사진 없음")
                .fileUrl("파일 없음")
                .endDate(endDate20)
                .build();

        postsRepository.save(posts20);

        Category category20 = Category.builder()
                .posts(posts20)
                .web(false)
                .app(true)
                .game(true)
                .ai(false)
                .build();

        category20.validateFieldCount();
        categoryRepository.save(category20);


        String initialEndDate21 = "2023-09-20";
        DateTimeFormatter dateFormatter21 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate21 = LocalDate.parse(initialEndDate21, dateFormatter21);

        Posts posts21 = Posts.builder()
                .user(user7)
                .postType(PostType.STUDY)
                .title("송하윤 교수님의 길찾기 놀이")
                .recruitmentCount(5)
                //.counts(1)
                .content("두 길이 주어지면, 가운데 길을 예측해서 이어 보아요..\n지리에 관심있는 분 환영.\nAI에 관심있는 분 대 환영")
                .promoteImageUrl("사진")
                .fileUrl("파일")
                .endDate(endDate21)
                .build();

        postsRepository.save(posts21);

        Category category21 = Category.builder()
                .posts(posts21)
                .web(true)
                .app(false)
                .game(false)
                .ai(true)
                .build();

        category21.validateFieldCount();
        categoryRepository.save(category21);

        String initialEndDate22 = "2023-09-20";
        DateTimeFormatter dateFormatter22 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate22 = LocalDate.parse(initialEndDate21, dateFormatter22);

        Posts posts22 = Posts.builder()
                .user(user7)
                .postType(PostType.PROJECT)
                .title("웹 사이트 제작")
                .recruitmentCount(3)
                //.counts(1)
                .content("홍대 주변 맛집 사이트.\n미식에 관심있는 분 좋아요.\n코딩 잘하시는 분 좋아요.")
                .promoteImageUrl("")
                .fileUrl("")
                .endDate(endDate22)
                .build();

        postsRepository.save(posts22);

        Category category22 = Category.builder()
                .posts(posts22)
                .web(true)
                .app(false)
                .game(false)
                .ai(false)
                .build();

        category22.validateFieldCount();
        categoryRepository.save(category22);


        // 초기 데이터 생성 및 저장(8)
        User user8 = User.builder()
                .userName("스터디")
                .nickName("onlyStudy")
                .email("8")
                .password(passwordEncoder.encode("8"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user8);

        Portfolio user8Portfolio = Portfolio.builder()
                .user(user8)
                .web(0)
                .app(4)
                .game(0)
                .ai(0)
                .shortIntroduce("앱 전문가")
                .introduce("- 스타트업 인턴 \n- 학점 4.5 \n- 기타 등등")
                .fileUrl("")
                .build();

        portfolioRepository.save(user8Portfolio);

        String initialEndDate16 = "2023-11-11";
        DateTimeFormatter dateFormatter16 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate16 = LocalDate.parse(initialEndDate16, dateFormatter16);

        // Posts 생성자 : (User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate)
        Posts posts16 = Posts.builder()
                .user(user8)
                .postType(PostType.STUDY)
                .title("스터디만 만들음.")
                .recruitmentCount(4)
                //.counts(1)
                .content("스터디만 모집할거임.\n프로젝트 모집 안함.\n내 맘임.")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate16)
                .build();

        postsRepository.save(posts16);

        Category category16 = Category.builder()
                .posts(posts16)
                .web(true)
                .app(false)
                .game(false)
                .ai(false)
                .build();

        category16.validateFieldCount();
        categoryRepository.save(category16);

        String initialEndDate17 = "2023-11-03";
        DateTimeFormatter dateFormatter17 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate17 = LocalDate.parse(initialEndDate17, dateFormatter17);

        Posts posts17 = Posts.builder()
                .user(user8)
                .postType(PostType.STUDY)
                .title("코틀린 공부")
                .recruitmentCount(3)
                //.counts(1)
                .content("Kotlin 같이 공부해요.\n앱 처음 하시는 분들 저와 함께해요.\n열심히 해서 플젝도 같이 만들어봐요.")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate17)
                .build();

        postsRepository.save(posts17);

        Category category17 = Category.builder()
                .posts(posts17)
                .web(false)
                .app(true)
                .game(false)
                .ai(false)
                .build();

        category17.validateFieldCount();
        categoryRepository.save(category17);


        String initialEndDate18 = "2023-10-02";
        DateTimeFormatter dateFormatter18 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate18 = LocalDate.parse(initialEndDate18, dateFormatter18);

        Posts posts18 = Posts.builder()
                .user(user8)
                .postType(PostType.STUDY)
                .title("안드로이드? IOS?")
                .recruitmentCount(4)
                //.counts(1)
                .content("안드로이드에 관심있는 사람?\nIOS에 관심있는 사람?\n기초부터 차근차근 같이 공부해보자.\n자세한건 옵챗으로 얘기해요")
                .promoteImageUrl("사진")
                .fileUrl("파일")
                .endDate(endDate18)
                .build();

        postsRepository.save(posts18);

        Category category18 = Category.builder()
                .posts(posts18)
                .web(false)
                .app(true)
                .game(false)
                .ai(false)
                .build();

        category18.validateFieldCount();
        categoryRepository.save(category18);


        // 초기 데이터 생성 및 저장(9)
        User user9 = User.builder()
                .userName("게임")
                .nickName("게임 장인")
                .email("9")
                .password(passwordEncoder.encode("9"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user9);

        Portfolio user9Portfolio = Portfolio.builder()
                .user(user9)
                .web(0)
                .app(0)
                .game(4)
                .ai(0)
                .shortIntroduce("게임 개발 장인")
                .introduce("- 대기업 개발자 \n- 학점 4.5 \n- 홍대 폰노이만 \n- 홍대 앨런 튜링")
                .fileUrl("")
                .build();

        portfolioRepository.save(user9Portfolio);

        String initialEndDate23 = "2023-11-12";
        DateTimeFormatter dateFormatter23 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate23 = LocalDate.parse(initialEndDate23, dateFormatter23);

        // Posts 생성자 : (User user, PostType postType, String title, Integer recruitmentCount, String content, String promoteImageUrl, String fileUrl, LocalDate endDate)
        Posts posts23 = Posts.builder()
                .user(user9)
                .postType(PostType.STUDY)
                .title("유니티 스터디 만들었어요.")
                .recruitmentCount(3)
                //.counts(1)
                .content("유니티 스터디.\nbox collider 2D를 아세요?\n모른다면 같이 스터디 ㄱㄱ.")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate23)
                .build();

        postsRepository.save(posts23);

        Category category23 = Category.builder()
                .posts(posts23)
                .web(false)
                .app(true)
                .game(true)
                .ai(false)
                .build();

        category23.validateFieldCount();
        categoryRepository.save(category23);

        String initialEndDate24 = "2023-11-10";
        DateTimeFormatter dateFormatter24 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate24 = LocalDate.parse(initialEndDate24, dateFormatter24);

        Posts posts24 = Posts.builder()
                .user(user9)
                .postType(PostType.STUDY)
                .title("언리얼 공부")
                .recruitmentCount(3)
                //.counts(1)
                .content("언리얼 기초부터 같이 공부하실분 구해요.\n저도 언리얼은 아무것도 몰라요.")
                .promoteImageUrl(null)
                .fileUrl(null)
                .endDate(endDate24)
                .build();

        postsRepository.save(posts24);

        Category category24 = Category.builder()
                .posts(posts24)
                .web(true)
                .app(false)
                .game(true)
                .ai(false)
                .build();

        category24.validateFieldCount();
        categoryRepository.save(category24);


        String initialEndDate25 = "2023-10-16";
        DateTimeFormatter dateFormatter25 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate25 = LocalDate.parse(initialEndDate25, dateFormatter25);

        Posts posts25 = Posts.builder()
                .user(user9)
                .postType(PostType.PROJECT)
                .title("유니티로 앱 게임 만들기.")
                .recruitmentCount(2)
                //.counts(1)
                .content("유니티로 프로젝트 같이할 사람?\n기획, 사운드, 디자인까지 모두 모였음.\n잘하는 분만 모심.\n포트폴리오 볼거임\n너만 오면 바로 시작.")
                .promoteImageUrl("사진")
                .fileUrl("파일")
                .endDate(endDate25)
                .build();

        postsRepository.save(posts25);

        Category category25 = Category.builder()
                .posts(posts25)
                .web(false)
                .app(true)
                .game(true)
                .ai(false)
                .build();

        category25.validateFieldCount();
        categoryRepository.save(category25);


        String initialEndDate26 = "2023-10-26";
        DateTimeFormatter dateFormatter26 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate26 = LocalDate.parse(initialEndDate26, dateFormatter26);

        Posts posts26 = Posts.builder()
                .user(user9)
                .postType(PostType.PROJECT)
                .title("언리얼로 웹 게임 만들기.")
                .recruitmentCount(4)
                //.counts(1)
                .content("언리얼로 프로젝트 같이할 사람?\n기획, 사운드, 디자인까지 모두 모였음.\n잘하는 분만 모심.\n포트폴리오 볼거임\n너만 오면 바로 시작 예정.")
                .promoteImageUrl("사진")
                .fileUrl("파일")
                .endDate(endDate26)
                .build();

        postsRepository.save(posts26);

        Category category26 = Category.builder()
                .posts(posts26)
                .web(false)
                .app(true)
                .game(true)
                .ai(false)
                .build();

        category26.validateFieldCount();
        categoryRepository.save(category26);

        // 초기 데이터 생성 및 저장(10)
        // ai 전문가 생성 예정
        // 게시물은 27부터 (유저 7, 9가 게시물이 4개임. 나머지는 3개.)







        // 추천 시스템 검증 전용 더미 데이터 만들기
        // 추천에 유효한 포폴까지만 생성하도록 하였음.
        // 0000 ~ 4321까지 총 225개의 더미 데이터 생성 필요

//        0000

        User user0000 = User.builder()
                .userName("0000")
                .nickName("0000")
                .email("0000")
                .password(passwordEncoder.encode("0000"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0000);

        Portfolio user0000Portfolio = Portfolio.builder()
                .user(user0000)
                .web(0)
                .app(0)
                .game(0)
                .ai(0)
                .shortIntroduce("0000")
                .introduce("0000")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0000Portfolio);

//        0001

        User user0001 = User.builder()
                .userName("0001")
                .nickName("0001")
                .email("0001")
                .password(passwordEncoder.encode("0001"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0001);

        Portfolio user0001Portfolio = Portfolio.builder()
                .user(user0001)
                .web(0)
                .app(0)
                .game(0)
                .ai(1)
                .shortIntroduce("0001")
                .introduce("0001")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0001Portfolio);

//        0002

        User user0002 = User.builder()
                .userName("0002")
                .nickName("0002")
                .email("0002")
                .password(passwordEncoder.encode("0002"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0002);

        Portfolio user0002Portfolio = Portfolio.builder()
                .user(user0002)
                .web(0)
                .app(0)
                .game(0)
                .ai(2)
                .shortIntroduce("0002")
                .introduce("0002")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0002Portfolio);

//        0003

        User user0003 = User.builder()
                .userName("0003")
                .nickName("0003")
                .email("0003")
                .password(passwordEncoder.encode("0003"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0003);

        Portfolio user0003Portfolio = Portfolio.builder()
                .user(user0003)
                .web(0)
                .app(0)
                .game(0)
                .ai(3)
                .shortIntroduce("0003")
                .introduce("0003")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0003Portfolio);

//        0004

        User user0004 = User.builder()
                .userName("0004")
                .nickName("0004")
                .email("0004")
                .password(passwordEncoder.encode("0004"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0004);

        Portfolio user0004Portfolio = Portfolio.builder()
                .user(user0004)
                .web(0)
                .app(0)
                .game(0)
                .ai(4)
                .shortIntroduce("0004")
                .introduce("0004")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0004Portfolio);

//        0010

        User user0010 = User.builder()
                .userName("0010")
                .nickName("0010")
                .email("0010")
                .password(passwordEncoder.encode("0010"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0010);

        Portfolio user0010Portfolio = Portfolio.builder()
                .user(user0010)
                .web(0)
                .app(0)
                .game(1)
                .ai(0)
                .shortIntroduce("0010")
                .introduce("0010")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0010Portfolio);

//        0012

        User user0012 = User.builder()
                .userName("0012")
                .nickName("0012")
                .email("0012")
                .password(passwordEncoder.encode("0012"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0012);

        Portfolio user0012Portfolio = Portfolio.builder()
                .user(user0012)
                .web(0)
                .app(0)
                .game(1)
                .ai(2)
                .shortIntroduce("0012")
                .introduce("0012")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0012Portfolio);

//        0013

        User user0013 = User.builder()
                .userName("0013")
                .nickName("0013")
                .email("0013")
                .password(passwordEncoder.encode("0013"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0013);

        Portfolio user0013Portfolio = Portfolio.builder()
                .user(user0013)
                .web(0)
                .app(0)
                .game(1)
                .ai(3)
                .shortIntroduce("0013")
                .introduce("0013")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0013Portfolio);

//        0014

        User user0014 = User.builder()
                .userName("0014")
                .nickName("0014")
                .email("0014")
                .password(passwordEncoder.encode("0014"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0014);

        Portfolio user0014Portfolio = Portfolio.builder()
                .user(user0014)
                .web(0)
                .app(0)
                .game(1)
                .ai(4)
                .shortIntroduce("0014")
                .introduce("0014")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0014Portfolio);

//        0020

        User user0020 = User.builder()
                .userName("0020")
                .nickName("0020")
                .email("0020")
                .password(passwordEncoder.encode("0020"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0020);

        Portfolio user0020Portfolio = Portfolio.builder()
                .user(user0020)
                .web(0)
                .app(0)
                .game(2)
                .ai(0)
                .shortIntroduce("0020")
                .introduce("0020")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0020Portfolio);

//        0021

        User user0021 = User.builder()
                .userName("0021")
                .nickName("0021")
                .email("0021")
                .password(passwordEncoder.encode("0021"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0021);

        Portfolio user0021Portfolio = Portfolio.builder()
                .user(user0021)
                .web(0)
                .app(0)
                .game(2)
                .ai(1)
                .shortIntroduce("0021")
                .introduce("0021")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0021Portfolio);

//        0023

        User user0023 = User.builder()
                .userName("0023")
                .nickName("0023")
                .email("0023")
                .password(passwordEncoder.encode("0023"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0023);

        Portfolio user0023Portfolio = Portfolio.builder()
                .user(user0023)
                .web(0)
                .app(0)
                .game(2)
                .ai(3)
                .shortIntroduce("0023")
                .introduce("0023")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0023Portfolio);

//        0024

        User user0024 = User.builder()
                .userName("0024")
                .nickName("0024")
                .email("0024")
                .password(passwordEncoder.encode("0024"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0024);

        Portfolio user0024Portfolio = Portfolio.builder()
                .user(user0024)
                .web(0)
                .app(0)
                .game(2)
                .ai(4)
                .shortIntroduce("0024")
                .introduce("0024")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0024Portfolio);

//        0030

        User user0030 = User.builder()
                .userName("0030")
                .nickName("0030")
                .email("0030")
                .password(passwordEncoder.encode("0030"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0030);

        Portfolio user0030Portfolio = Portfolio.builder()
                .user(user0030)
                .web(0)
                .app(0)
                .game(3)
                .ai(0)
                .shortIntroduce("0030")
                .introduce("0030")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0030Portfolio);

//        0031

        User user0031 = User.builder()
                .userName("0031")
                .nickName("0031")
                .email("0031")
                .password(passwordEncoder.encode("0031"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0031);

        Portfolio user0031Portfolio = Portfolio.builder()
                .user(user0031)
                .web(0)
                .app(0)
                .game(3)
                .ai(1)
                .shortIntroduce("0031")
                .introduce("0031")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0031Portfolio);

//        0032

        User user0032 = User.builder()
                .userName("0032")
                .nickName("0032")
                .email("0032")
                .password(passwordEncoder.encode("0032"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0032);

        Portfolio user0032Portfolio = Portfolio.builder()
                .user(user0032)
                .web(0)
                .app(0)
                .game(3)
                .ai(2)
                .shortIntroduce("0032")
                .introduce("0032")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0032Portfolio);

//        0034

        User user0034 = User.builder()
                .userName("0034")
                .nickName("0034")
                .email("0034")
                .password(passwordEncoder.encode("0034"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0034);

        Portfolio user0034Portfolio = Portfolio.builder()
                .user(user0034)
                .web(0)
                .app(0)
                .game(3)
                .ai(4)
                .shortIntroduce("0034")
                .introduce("0034")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0034Portfolio);

//        0040

        User user0040 = User.builder()
                .userName("0040")
                .nickName("0040")
                .email("0040")
                .password(passwordEncoder.encode("0040"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0040);

        Portfolio user0040Portfolio = Portfolio.builder()
                .user(user0040)
                .web(0)
                .app(0)
                .game(4)
                .ai(0)
                .shortIntroduce("0040")
                .introduce("0040")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0040Portfolio);

//        0041

        User user0041 = User.builder()
                .userName("0041")
                .nickName("0041")
                .email("0041")
                .password(passwordEncoder.encode("0041"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0041);

        Portfolio user0041Portfolio = Portfolio.builder()
                .user(user0041)
                .web(0)
                .app(0)
                .game(4)
                .ai(1)
                .shortIntroduce("0041")
                .introduce("0041")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0041Portfolio);

//        0042

        User user0042 = User.builder()
                .userName("0042")
                .nickName("0042")
                .email("0042")
                .password(passwordEncoder.encode("0042"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0042);

        Portfolio user0042Portfolio = Portfolio.builder()
                .user(user0042)
                .web(0)
                .app(0)
                .game(4)
                .ai(2)
                .shortIntroduce("0042")
                .introduce("0042")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0042Portfolio);

//        0043

        User user0043 = User.builder()
                .userName("0043")
                .nickName("0043")
                .email("0043")
                .password(passwordEncoder.encode("0043"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0043);

        Portfolio user0043Portfolio = Portfolio.builder()
                .user(user0043)
                .web(0)
                .app(0)
                .game(4)
                .ai(3)
                .shortIntroduce("0043")
                .introduce("0043")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0043Portfolio);

//        0100

        User user0100 = User.builder()
                .userName("0100")
                .nickName("0100")
                .email("0100")
                .password(passwordEncoder.encode("0100"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0100);

        Portfolio user0100Portfolio = Portfolio.builder()
                .user(user0100)
                .web(0)
                .app(1)
                .game(0)
                .ai(0)
                .shortIntroduce("0100")
                .introduce("0100")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0100Portfolio);

//        0102

        User user0102 = User.builder()
                .userName("0102")
                .nickName("0102")
                .email("0102")
                .password(passwordEncoder.encode("0102"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0102);

        Portfolio user0102Portfolio = Portfolio.builder()
                .user(user0102)
                .web(0)
                .app(1)
                .game(0)
                .ai(2)
                .shortIntroduce("0102")
                .introduce("0102")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0102Portfolio);

//        0103

        User user0103 = User.builder()
                .userName("0103")
                .nickName("0103")
                .email("0103")
                .password(passwordEncoder.encode("0103"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0103);

        Portfolio user0103Portfolio = Portfolio.builder()
                .user(user0103)
                .web(0)
                .app(1)
                .game(0)
                .ai(3)
                .shortIntroduce("0103")
                .introduce("0103")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0103Portfolio);

//        0104

        User user0104 = User.builder()
                .userName("0104")
                .nickName("0104")
                .email("0104")
                .password(passwordEncoder.encode("0104"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0104);

        Portfolio user0104Portfolio = Portfolio.builder()
                .user(user0104)
                .web(0)
                .app(1)
                .game(0)
                .ai(4)
                .shortIntroduce("0104")
                .introduce("0104")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0104Portfolio);

//        0120

        User user0120 = User.builder()
                .userName("0120")
                .nickName("0120")
                .email("0120")
                .password(passwordEncoder.encode("0120"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0120);

        Portfolio user0120Portfolio = Portfolio.builder()
                .user(user0120)
                .web(0)
                .app(1)
                .game(2)
                .ai(0)
                .shortIntroduce("0120")
                .introduce("0120")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0120Portfolio);

//        0123

        User user0123 = User.builder()
                .userName("0123")
                .nickName("0123")
                .email("0123")
                .password(passwordEncoder.encode("0123"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0123);

        Portfolio user0123Portfolio = Portfolio.builder()
                .user(user0123)
                .web(0)
                .app(1)
                .game(2)
                .ai(3)
                .shortIntroduce("0123")
                .introduce("0123")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0123Portfolio);

//        0124

        User user0124 = User.builder()
                .userName("0124")
                .nickName("0124")
                .email("0124")
                .password(passwordEncoder.encode("0124"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0124);

        Portfolio user0124Portfolio = Portfolio.builder()
                .user(user0124)
                .web(0)
                .app(1)
                .game(2)
                .ai(4)
                .shortIntroduce("0124")
                .introduce("0124")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0124Portfolio);

//        0130

        User user0130 = User.builder()
                .userName("0130")
                .nickName("0130")
                .email("0130")
                .password(passwordEncoder.encode("0130"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0130);

        Portfolio user0130Portfolio = Portfolio.builder()
                .user(user0130)
                .web(0)
                .app(1)
                .game(3)
                .ai(0)
                .shortIntroduce("0130")
                .introduce("0130")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0130Portfolio);

//        0132

        User user0132 = User.builder()
                .userName("0132")
                .nickName("0132")
                .email("0132")
                .password(passwordEncoder.encode("0132"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0132);

        Portfolio user0132Portfolio = Portfolio.builder()
                .user(user0132)
                .web(0)
                .app(1)
                .game(3)
                .ai(2)
                .shortIntroduce("0132")
                .introduce("0132")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0132Portfolio);

//        0134

        User user0134 = User.builder()
                .userName("0134")
                .nickName("0134")
                .email("0134")
                .password(passwordEncoder.encode("0134"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0134);

        Portfolio user0134Portfolio = Portfolio.builder()
                .user(user0134)
                .web(0)
                .app(1)
                .game(3)
                .ai(4)
                .shortIntroduce("0134")
                .introduce("0134")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0134Portfolio);

//        0140

        User user0140 = User.builder()
                .userName("0140")
                .nickName("0140")
                .email("0140")
                .password(passwordEncoder.encode("0140"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0140);

        Portfolio user0140Portfolio = Portfolio.builder()
                .user(user0140)
                .web(0)
                .app(1)
                .game(4)
                .ai(0)
                .shortIntroduce("0140")
                .introduce("0140")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0140Portfolio);

//        0142

        User user0142 = User.builder()
                .userName("0142")
                .nickName("0142")
                .email("0142")
                .password(passwordEncoder.encode("0142"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0142);

        Portfolio user0142Portfolio = Portfolio.builder()
                .user(user0142)
                .web(0)
                .app(1)
                .game(4)
                .ai(2)
                .shortIntroduce("0142")
                .introduce("0142")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0142Portfolio);

//        0143

        User user0143 = User.builder()
                .userName("0143")
                .nickName("0143")
                .email("0143")
                .password(passwordEncoder.encode("0143"))  // 비밀번호 해싱
                .role(Role.USER)
                .build();

        userRepository.save(user0143);

        Portfolio user0143Portfolio = Portfolio.builder()
                .user(user0143)
                .web(0)
                .app(1)
                .game(4)
                .ai(3)
                .shortIntroduce("0143")
                .introduce("0143")
                .fileUrl("")
                .build();

        portfolioRepository.save(user0143Portfolio);

//        0200
//        0201
//        0203
//        0204
//        0210
//        0213
//        0214
//        0230
//        0231
//        0234
//        0240
//        0241
//        0243
//        0300
//        0301
//        0302
//        0304
//        0310
//        0312
//        0314
//        0320
//        0321
//        0324
//        0340
//        0341
//        0342
//        0400
//        0401
//        0402
//        0403
//        0410
//        0412
//        0413
//        0420
//        0421
//        0423
//        0430
//        0431
//        0432
//        1002
//        1003
//        1004
//        1020
//        1023
//        1024
//        1030
//        1032
//        1034
//        1040
//        1042
//        1043
//        1200
//        1203
//        1204
//        1230
//        1234
//        1240
//        1243
//        1300
//        1302
//        1304
//        1320
//        1324
//        1340
//        1342
//        1400
//        1402
//        1403
//        1420
//        1423
//        1430
//        1432
//        2001
//        2003
//        2004
//        2010
//        2013
//        2014
//        2030
//        2031
//        2034
//        2040
//        2041
//        2043
//        2100
//        2103
//        2104
//        2130
//        2134
//        2140
//        2143
//        2300
//        2301
//        2304
//        2310
//        2314
//        2340
//        2341
//        2400
//        2401
//        2403
//        2410
//        2413
//        2430
//        2431
//        3001
//        3002
//        3004
//        3010
//        3012
//        3014
//        3020
//        3021
//        3024
//        3040
//        3041
//        3042
//        3100
//        3102
//        3104
//        3120
//        3124
//        3140
//        3142
//        3200
//        3201
//        3204
//        3210
//        3214
//        3240
//        3241
//        3400
//        3401
//        3402
//        3410
//        3412
//        3420
//        3421
//        4001
//        4002
//        4003
//        4010
//        4012
//        4013
//        4020
//        4021
//        4023
//        4030
//        4031
//        4032
//        4100
//        4102
//        4103
//        4120
//        4123
//        4130
//        4132
//        4200
//        4201
//        4203
//        4210
//        4213
//        4230
//        4231
//        4300
//        4301
//        4302
//        4310
//        4312
//        4320
//        4321
    }
}
