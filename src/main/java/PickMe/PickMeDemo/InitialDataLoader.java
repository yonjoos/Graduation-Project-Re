package PickMe.PickMeDemo;

import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortfolioRepository portfolioRepository;
    private final PostsRepository postsRepository;
    private final CategoryRepository categoryRepository;
    private final VectorSimilarityRepository vectorSimilarityRepository;


    private void createUserAndPortfolio(
            String userName,
            String nickName,
            String email,
            String password,
            int web,
            int app,
            int game,
            int ai,
            String shortIntroduce,
            String introduce,
            String fileUrl
    ) {
        User user = User.builder()
                .userName(userName)
                .nickName(nickName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .lastAccessDate(LocalDateTime.now()).build();

        userRepository.save(user);

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .web(web)
                .app(app)
                .game(game)
                .ai(ai)
                .shortIntroduce(shortIntroduce)
                .introduce(introduce)
                //.fileUrl(fileUrl)
                .build();

        portfolioRepository.save(portfolio);
    }

    // 여기서 createRecommendationsTable 함수는 Recommendations 테이블에 데이터를 저장하도록 되어 있다.

    public static List<List<Integer>> getPermutations(List<Integer> numbers) {
        Collections.sort(numbers);
        List<List<Integer>> permutations = new ArrayList<>();
        do {
            permutations.add(new ArrayList<>(numbers));
        } while (nextPermutation(numbers));
        return permutations;
    }

    public static boolean nextPermutation(List<Integer> arr) {
        int n = arr.size();
        int i = n - 2;
        while (i >= 0 && arr.get(i) >= arr.get(i + 1)) {
            i--;
        }
        if (i < 0) {
            return false;
        }
        int j = n - 1;
        while (arr.get(j) <= arr.get(i)) {
            j--;
        }
        Collections.swap(arr, i, j);
        Collections.reverse(arr.subList(i + 1, n));
        return true;
    }

    public static Integer[] listToVector(List<Integer> list) {
        Integer[] vector = new Integer[list.size()];
        for (int i = 0; i < list.size(); i++) {
            vector[i] = list.get(i);
        }
        return vector;
    }

    private double calculateCosineSimilarity(Integer[] vectorA, Integer[] vectorB) {
        // Calculate the dot product of vectorA and vectorB
        double dotProduct = 0;
        double magnitudeA = 0;
        double magnitudeB = 0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            magnitudeA += Math.pow(vectorA[i], 2);
            magnitudeB += Math.pow(vectorB[i], 2);
        }

        // Calculate the magnitude (Euclidean norm) of each vector
        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        // Calculate the cosine similarity
        if (magnitudeA == 0 || magnitudeB == 0) {
            // Handle the case of zero magnitude (avoid division by zero)
            return 0.0;
        } else {
            return dotProduct / (magnitudeA * magnitudeB);
        }
    }



    @Override
    public void run(String... args) throws Exception {

//        // 초기 벡터값 이니셜라이징
//        int dimension = 4;
//        int maxValue = 4; // Values can range from 0 to 4
//        generateAndSaveSimilarityData(dimension, maxValue);

        String initialImg = "comgongWow.png";

        List<List<Integer>> standardPermutations = new ArrayList<>();
        standardPermutations.add(List.of(0, 0, 0, 0));
        standardPermutations.add(List.of(1, 0, 0, 0));
        standardPermutations.add(List.of(2, 0, 0, 0));
        standardPermutations.add(List.of(3, 0, 0, 0));
        standardPermutations.add(List.of(4, 0, 0, 0));
        standardPermutations.add(List.of(2, 1, 0, 0));
        standardPermutations.add(List.of(3, 1, 0, 0));
        standardPermutations.add(List.of(3, 2, 0, 0));
        standardPermutations.add(List.of(4, 1, 0, 0));
        standardPermutations.add(List.of(4, 2, 0, 0));
        standardPermutations.add(List.of(4, 3, 0, 0));
        standardPermutations.add(List.of(3, 2, 1, 0));
        standardPermutations.add(List.of(4, 2, 1, 0));
        standardPermutations.add(List.of(4, 3, 1, 0));
        standardPermutations.add(List.of(4, 3, 2, 0));
        standardPermutations.add(List.of(4, 3, 2, 1));

        List<List<Integer>> standardPermutationsList = new ArrayList<>();
        for (List<Integer> standardPermutation : standardPermutations) {
            standardPermutationsList.addAll(getPermutations(new ArrayList<>(standardPermutation)));
        }

        List<List<Integer>> numbers = new ArrayList<>();
        numbers.add(List.of(0, 0, 0, 0));
        numbers.add(List.of(1, 0, 0, 0));
        numbers.add(List.of(2, 0, 0, 0));
        numbers.add(List.of(3, 0, 0, 0));
        numbers.add(List.of(4, 0, 0, 0));
        numbers.add(List.of(2, 1, 0, 0));
        numbers.add(List.of(3, 1, 0, 0));
        numbers.add(List.of(3, 2, 0, 0));
        numbers.add(List.of(4, 1, 0, 0));
        numbers.add(List.of(4, 2, 0, 0));
        numbers.add(List.of(4, 3, 0, 0));
        numbers.add(List.of(3, 2, 1, 0));
        numbers.add(List.of(4, 2, 1, 0));
        numbers.add(List.of(4, 3, 1, 0));
        numbers.add(List.of(4, 3, 2, 0));
        numbers.add(List.of(4, 3, 2, 1));

        for (List<Integer> standard : standardPermutationsList) {

            List<List<Integer>> permutations = new ArrayList<>();

            for (List<Integer> number : numbers) {
                permutations.addAll(getPermutations(new ArrayList<>(number)));
            }

            for (List<Integer> permutation : permutations) {
                Integer[] standardArray = listToVector(standard);
                Integer[] permutationArray = listToVector(permutation);

                double similarity = calculateCosineSimilarity(permutationArray, standardArray);

                VectorSimilarity vectorSimilarity = new VectorSimilarity(standardArray, permutationArray, similarity);

                vectorSimilarityRepository.save(vectorSimilarity);
            }
        }


        // 초기 데이터 생성 및 저장(관리자)
        User adminUser = User.builder()
                .userName("admin")
                .nickName("admin")
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("admin"))  // 비밀번호 해싱
                .role(Role.ADMIN)
                .lastAccessDate(LocalDateTime.of(2023, 9, 27, 14, 30, 0))
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
                //.fileUrl("")
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
                .nickName("다섯글자닉다섯글자닉")
                .email("2")
                .password(passwordEncoder.encode("2"))  // 비밀번호 해싱
                .role(Role.USER)
                .lastAccessDate(LocalDateTime.of(2023, 8, 27, 14, 30, 0))
                .imageUrl(initialImg)
                .build();

        userRepository.save(user2);

        Portfolio user2Portfolio = Portfolio.builder()
                .user(user2)
                .web(4)
                .app(3)
                .game(2)
                .ai(1)
                .shortIntroduce("안녕하세요, 웹과 앱에 관심있는 코딩 꿈나무입니다.")
                .introduce("- 맛있홍 프로젝트 (React + Node.js + Express.js) \n- 픽미 프로젝트 (React + SpringBoot + JPA) \n- 코로나 보드 크롤링 프로젝트(Node.js + Express.js)")
                //.fileUrl("")
                .build();

        portfolioRepository.save(user2Portfolio);

        String initialEndDate1 = "2023-11-30"; // 원하는 종료 날짜를 스트링으로 받음
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
                //.promoteImageUrl(null)
                //.fileUrl(null)
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

        String initialEndDate2 = "2023-12-10";
        DateTimeFormatter dateFormatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate2 = LocalDate.parse(initialEndDate2, dateFormatter2);

        Posts posts2 = Posts.builder()
                .user(user2)
                .postType(PostType.PROJECT)
                .title("토이 플젝 하실분?")
                .recruitmentCount(2)
                //.counts(1)
                .content("토이 프로젝트 팀원을 모집합니다.\n주제는 아직 정해지지 않았습니다.\n현재 저는 백엔드 개발 가능하고, React 및 Redux 사용 가능한 프론트 개발자 두 분을 모십니다.")
                //.promoteImageUrl("사진 없음")
                //.fileUrl("파일 없음")
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

        String initialEndDate3 = "2023-11-16";
        DateTimeFormatter dateFormatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate3 = LocalDate.parse(initialEndDate3, dateFormatter3);

        Posts posts3 = Posts.builder()
                .user(user2)
                .postType(PostType.STUDY)
                .title("배자구 스터디")
                .recruitmentCount(4)
                //.counts(1)
                .content("배자구 스터디 구합니다.\n감자도 환영합니다.\n저도 자구알못이에요..ㅠㅠ\n같이 자구 공부해요!!")
                //.promoteImageUrl("사진")
                //.fileUrl("파일")
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
                .lastAccessDate(LocalDateTime.of(2023, 7, 27, 14, 30, 0))
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
                //.fileUrl("")
                .build();

        portfolioRepository.save(user3Portfolio);

        String initialEndDate4 = "2023-12-12"; // 원하는 종료 날짜를 스트링으로 받음
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
                //.promoteImageUrl(null)
                //.fileUrl(null)
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

        String initialEndDate5 = "2023-11-23";
        DateTimeFormatter dateFormatter5 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate5 = LocalDate.parse(initialEndDate5, dateFormatter5);

        Posts posts5 = Posts.builder()
                .user(user3)
                .postType(PostType.STUDY)
                .title("알골 스터디 팀원 구함")
                .recruitmentCount(4)
                //.counts(1)
                .content("알고리즘 스터디 구함.\n란골 배골 곤골 모두 환영.\n백준 플래티넘이 목표.\n알고리즘 마스터가 되어봅시다..")
                //.promoteImageUrl("사진 없음")
                //.fileUrl("파일 없음")
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


        String initialEndDate6 = "2023-12-20";
        DateTimeFormatter dateFormatter6 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate6 = LocalDate.parse(initialEndDate6, dateFormatter6);

        Posts posts6 = Posts.builder()
                .user(user3)
                .postType(PostType.STUDY)
                .title("프린스 송")
                .recruitmentCount(3)
                //.counts(1)
                .content("송프언 프롤로그, 렉스, 야크, 리스프 과제 같이 고민해요!\n열심히 하시는 분들 환영!")
                //.promoteImageUrl("사진")
                //.fileUrl("파일")
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
                .lastAccessDate(LocalDateTime.of(2023, 6, 27, 14, 30, 0))
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
                //.fileUrl("")
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
                //.promoteImageUrl("나는 악성유저지만 여기서는 착하게 굴겠다. 사진은 경로이므로 띄어쓰기나 개행이 들어가면 안될 것 같다.")
                //.fileUrl("나는 악성유저지만 여기서는 착하게 굴겠다. 파일은 경로이므로 띄어쓰기나 개행이 들어가면 안될 것 같다.")
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
                //.promoteImageUrl("나는 악성유저지만 여기서는 착하게 굴겠다. 사진은 경로이므로 띄어쓰기나 개행이 들어가면 안될 것 같다.")
                //.fileUrl("나는 악성유저지만 여기서는 착하게 굴겠다. 파일은 경로이므로 띄어쓰기나 개행이 들어가면 안될 것 같다.")
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


        String initialEndDate9 = "2023-11-20";
        DateTimeFormatter dateFormatter9 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate9 = LocalDate.parse(initialEndDate9, dateFormatter9);

        Posts posts9 = Posts.builder()
                .user(user4)
                .postType(PostType.STUDY)
                .title("악성유저의 정상적인 게시물ㅋㅋ")
                .recruitmentCount(3)
                //.counts(1)
                .content("웬일이래? 내가 정상적인 게시물도 달고 말이야. 고마워해라.")
                //.promoteImageUrl("")
                //.fileUrl("")
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
                .lastAccessDate(LocalDateTime.of(2023, 9, 25, 14, 30, 0))
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
                //.fileUrl("")
                .build();

        portfolioRepository.save(user5Portfolio);

        String initialEndDate10 = "2023-11-20";
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
                //.promoteImageUrl(null)
                //.fileUrl(null)
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

        String initialEndDate11 = "2023-12-01";
        DateTimeFormatter dateFormatter11 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate11 = LocalDate.parse(initialEndDate11, dateFormatter11);

        Posts posts11 = Posts.builder()
                .user(user5)
                .postType(PostType.PROJECT)
                .title("인공지능 마스터")
                .recruitmentCount(3)
                //.counts(1)
                .content("인공지능 잘 활용하시는 분과 함께 프로젝트 하고 싶어요.\n저와 함께 인공지능 마스터가 되어보아요!")
                //.promoteImageUrl(null)
                //.fileUrl(null)
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


        String initialEndDate12 = "2023-11-05";
        DateTimeFormatter dateFormatter12 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate12 = LocalDate.parse(initialEndDate12, dateFormatter12);

        Posts posts12 = Posts.builder()
                .user(user5)
                .postType(PostType.STUDY)
                .title("유니티랑 C# 같이 공부하실 분 구합니다.")
                .recruitmentCount(5)
                //.counts(1)
                .content("같이 열심히 공부해서, 플젝도 만들어봐요!!\n포트폴리오 열심히 채웁시다..")
                //.promoteImageUrl("사진")
                //.fileUrl("파일")
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
                .lastAccessDate(LocalDateTime.of(2023, 9, 2, 14, 30, 0))
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
                //.fileUrl("")
                .build();

        portfolioRepository.save(user6Portfolio);

        String initialEndDate13 = "2023-12-19";
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
                //.promoteImageUrl(null)
                //.fileUrl(null)
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

        String initialEndDate14 = "2023-11-30";
        DateTimeFormatter dateFormatter14 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate14 = LocalDate.parse(initialEndDate14, dateFormatter14);

        Posts posts14 = Posts.builder()
                .user(user6)
                .postType(PostType.PROJECT)
                .title("인공지능 활용한 웹 개발")
                .recruitmentCount(3)
                //.counts(1)
                .content("자바 스프링 잘 쓰시는 분 구해요.\n인공지능 잘 활용하시는 분과 함께 프로젝트 하고 싶어요.\n저와 함께 웹 및 인공지능 마스터가 되어보아요!")
                //.promoteImageUrl(null)
                //.fileUrl(null)
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


        String initialEndDate15 = "2023-12-07";
        DateTimeFormatter dateFormatter15 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate15 = LocalDate.parse(initialEndDate15, dateFormatter15);

        Posts posts15 = Posts.builder()
                .user(user6)
                .postType(PostType.PROJECT)
                .title("제목 뭐로 하지..")
                .recruitmentCount(5)
                //.counts(1)
                .content("그냥 웹 플젝 할거야..\n포트폴리오 채워보자..")
                //.promoteImageUrl("사진")
                //.fileUrl("파일")
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
                .lastAccessDate(LocalDateTime.of(2023, 9, 25, 14, 30, 0))
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
                //.fileUrl("")
                .build();

        portfolioRepository.save(user7Portfolio);

        String initialEndDate19 = "2023-11-15"; // 원하는 종료 날짜를 스트링으로 받음
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
                //.promoteImageUrl("사진 뭐하지")
                //.fileUrl("나도 몰라")
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

        String initialEndDate20 = "2023-12-28";
        DateTimeFormatter dateFormatter20 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate20 = LocalDate.parse(initialEndDate20, dateFormatter20);

        Posts posts20 = Posts.builder()
                .user(user7)
                .postType(PostType.STUDY)
                .title("알골 스터디 하실 분~")
                .recruitmentCount(4)
                //.counts(1)
                .content("알고리즘 스터디 구해여.\n매일 백준 한 문제씩 푸는 것이 목표에여.")
                //.promoteImageUrl("사진 없음")
                //.fileUrl("파일 없음")
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


        String initialEndDate21 = "2023-11-20";
        DateTimeFormatter dateFormatter21 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate21 = LocalDate.parse(initialEndDate21, dateFormatter21);

        Posts posts21 = Posts.builder()
                .user(user7)
                .postType(PostType.STUDY)
                .title("송하윤 교수님의 길찾기 놀이")
                .recruitmentCount(5)
                //.counts(1)
                .content("두 길이 주어지면, 가운데 길을 예측해서 이어 보아요..\n지리에 관심있는 분 환영.\nAI에 관심있는 분 대 환영")
                //.promoteImageUrl("사진")
                //.fileUrl("파일")
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

        String initialEndDate22 = "2023-12-20";
        DateTimeFormatter dateFormatter22 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate22 = LocalDate.parse(initialEndDate21, dateFormatter22);

        Posts posts22 = Posts.builder()
                .user(user7)
                .postType(PostType.PROJECT)
                .title("웹 사이트 제작")
                .recruitmentCount(3)
                //.counts(1)
                .content("홍대 주변 맛집 사이트.\n미식에 관심있는 분 좋아요.\n코딩 잘하시는 분 좋아요.")
                //.promoteImageUrl("")
                //.fileUrl("")
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
                .lastAccessDate(LocalDateTime.of(2023, 4, 27, 14, 30, 0))
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
                //.fileUrl("")
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
                //.promoteImageUrl(null)
                //.fileUrl(null)
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

        String initialEndDate17 = "2023-12-03";
        DateTimeFormatter dateFormatter17 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate17 = LocalDate.parse(initialEndDate17, dateFormatter17);

        Posts posts17 = Posts.builder()
                .user(user8)
                .postType(PostType.STUDY)
                .title("코틀린 공부")
                .recruitmentCount(3)
                //.counts(1)
                .content("Kotlin 같이 공부해요.\n앱 처음 하시는 분들 저와 함께해요.\n열심히 해서 플젝도 같이 만들어봐요.")
                //.promoteImageUrl(null)
                //.fileUrl(null)
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


        String initialEndDate18 = "2023-11-02";
        DateTimeFormatter dateFormatter18 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate18 = LocalDate.parse(initialEndDate18, dateFormatter18);

        Posts posts18 = Posts.builder()
                .user(user8)
                .postType(PostType.STUDY)
                .title("안드로이드? IOS?")
                .recruitmentCount(4)
                //.counts(1)
                .content("안드로이드에 관심있는 사람?\nIOS에 관심있는 사람?\n기초부터 차근차근 같이 공부해보자.\n자세한건 옵챗으로 얘기해요")
                //.promoteImageUrl("사진")
                //.fileUrl("파일")
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
                .lastAccessDate(LocalDateTime.of(2023, 7, 21, 14, 30, 0))
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
                //.fileUrl("")
                .build();

        portfolioRepository.save(user9Portfolio);

        String initialEndDate23 = "2023-12-12";
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
                //.promoteImageUrl(null)
                //.fileUrl(null)
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
                //.promoteImageUrl(null)
                //.fileUrl(null)
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


        String initialEndDate25 = "2023-11-16";
        DateTimeFormatter dateFormatter25 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate25 = LocalDate.parse(initialEndDate25, dateFormatter25);

        Posts posts25 = Posts.builder()
                .user(user9)
                .postType(PostType.PROJECT)
                .title("유니티로 앱 게임 만들기.")
                .recruitmentCount(2)
                //.counts(1)
                .content("유니티로 프로젝트 같이할 사람?\n기획, 사운드, 디자인까지 모두 모였음.\n잘하는 분만 모심.\n포트폴리오 볼거임\n너만 오면 바로 시작.")
                //.promoteImageUrl("사진")
                //.fileUrl("파일")
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


        String initialEndDate26 = "2023-12-12";
        DateTimeFormatter dateFormatter26 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate26 = LocalDate.parse(initialEndDate26, dateFormatter26);

        Posts posts26 = Posts.builder()
                .user(user9)
                .postType(PostType.PROJECT)
                .title("언리얼로 웹 게임 만들기.")
                .recruitmentCount(4)
                //.counts(1)
                .content("언리얼로 프로젝트 같이할 사람?\n기획, 사운드, 디자인까지 모두 모였음.\n잘하는 분만 모심.\n포트폴리오 볼거임\n너만 오면 바로 시작 예정.")
                //.promoteImageUrl("사진")
                //.fileUrl("파일")
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
        // 0000 ~ 4321까지 총 209개의 더미 데이터 생성 필요
//
////        0000
//        createUserAndPortfolio("0000", "0000", "0000", "0000", 0, 0, 0, 0, "0000", "0000", "");
////        0001
//        createUserAndPortfolio("0001", "0001", "0001", "0001", 0, 0, 0, 1, "0001", "0001", "");
////        0002
//        createUserAndPortfolio("0002", "0002", "0002", "0002", 0, 0, 0, 2, "0002", "0002", "");
////        0003
//        createUserAndPortfolio("0003", "0003", "0003", "0003", 0, 0, 0, 3, "0003", "0003", "");
////        0004
//        createUserAndPortfolio("0004", "0004", "0004", "0004", 0, 0, 0, 4, "0004", "0004", "");
////        0010
//        createUserAndPortfolio("0010", "0010", "0010", "0010", 0, 0, 1, 0, "0010", "0010", "");
////        0012
//        createUserAndPortfolio("0012", "0012", "0012", "0012", 0, 0, 1, 2, "0012", "0012", "");
////        0013
//        createUserAndPortfolio("0013", "0013", "0013", "0013", 0, 0, 1, 3, "0013", "0013", "");
////        0014
//        createUserAndPortfolio("0014", "0014", "0014", "0014", 0, 0, 1, 4, "0014", "0014", "");
////        0020
//        createUserAndPortfolio("0020", "0020", "0020", "0020", 0, 0, 2, 0, "0020", "0020", "");
////        0021
//        createUserAndPortfolio("0021", "0021", "0021", "0021", 0, 0, 2, 1, "0021", "0021", "");
////        0023
//        createUserAndPortfolio("0023", "0023", "0023", "0023", 0, 0, 2, 3, "0023", "0023", "");
////        0024
//        createUserAndPortfolio("0024", "0024", "0024", "0024", 0, 0, 2, 4, "0024", "0024", "");
////        0030
//        createUserAndPortfolio("0030", "0030", "0030", "0030", 0, 0, 3, 0, "0030", "0030", "");
////        0031
//        createUserAndPortfolio("0031", "0031", "0031", "0031", 0, 0, 3, 1, "0031", "0031", "");
////        0032
//        createUserAndPortfolio("0032", "0032", "0032", "0032", 0, 0, 3, 2, "0032", "0032", "");
////        0034
//        createUserAndPortfolio("0034", "0034", "0034", "0034", 0, 0, 3, 4, "0034", "0034", "");
////        0040
//        createUserAndPortfolio("0040", "0040", "0040", "0040", 0, 0, 4, 0, "0040", "0040", "");
////        0041
//        createUserAndPortfolio("0041", "0041", "0041", "0041", 0, 0, 4, 1, "0041", "0041", "");
////        0042
//        createUserAndPortfolio("0042", "0042", "0042", "0042", 0, 0, 4, 2, "0042", "0042", "");
////        0043
//        createUserAndPortfolio("0043", "0043", "0043", "0043", 0, 0, 4, 3, "0043", "0043", "");
////        0100
//        createUserAndPortfolio("0100", "0100", "0100", "0100", 0, 1, 0, 0, "0100", "0100", "");
////        0102
//        createUserAndPortfolio("0102", "0102", "0102", "0102", 0, 1, 0, 2, "0102", "0102", "");
////        0103
//        createUserAndPortfolio("0103", "0103", "0103", "0103", 0, 1, 0, 3, "0103", "0103", "");
////        0104
//        createUserAndPortfolio("0104", "0104", "0104", "0104", 0, 1, 0, 4, "0104", "0104", "");
////        0120
//        createUserAndPortfolio("0120", "0120", "0120", "0120", 0, 1, 2, 0, "0120", "0120", "");
////        0123
//        createUserAndPortfolio("0123", "0123", "0123", "0123", 0, 1, 2, 3, "0123", "0123", "");
////        0124
//        createUserAndPortfolio("0124", "0124", "0124", "0124", 0, 1, 2, 4, "0124", "0124", "");
////        0130
//        createUserAndPortfolio("0130", "0130", "0130", "0130", 0, 1, 3, 0, "0130", "0130", "");
////        0132
//        createUserAndPortfolio("0132", "0132", "0132", "0132", 0, 1, 3, 2, "0132", "0132", "");
////        0134
//        createUserAndPortfolio("0134", "0134", "0134", "0134", 0, 1, 3, 4, "0134", "0134", "");
////        0140
//        createUserAndPortfolio("0140", "0140", "0140", "0140", 0, 1, 4, 0, "0140", "0140", "");
////        0142
//        createUserAndPortfolio("0142", "0142", "0142", "0142", 0, 1, 4, 2, "0142", "0142", "");
////        0143
//        createUserAndPortfolio("0143", "0143", "0143", "0143", 0, 1, 4, 3, "0143", "0143", "");
////        0200
//        createUserAndPortfolio("0200", "0200", "0200", "0200", 0, 2, 0, 0, "0200", "0200", "");
////        0201
//        createUserAndPortfolio("0201", "0201", "0201", "0201", 0, 2, 0, 1, "0201", "0201", "");
////        0203
//        createUserAndPortfolio("0203", "0203", "0203", "0203", 0, 2, 0, 3, "0203", "0203", "");
////        0204
//        createUserAndPortfolio("0204", "0204", "0204", "0204", 0, 2, 0, 4, "0204", "0204", "");
////        0210
//        createUserAndPortfolio("0210", "0210", "0210", "0210", 0, 2, 1, 0, "0210", "0210", "");
////        0213
//        createUserAndPortfolio("0213", "0213", "0213", "0213", 0, 2, 1, 3, "0213", "0213", "");
////        0214
//        createUserAndPortfolio("0214", "0214", "0214", "0214", 0, 2, 1, 4, "0214", "0214", "");
////        0230
//        createUserAndPortfolio("0230", "0230", "0230", "0230", 0, 2, 3, 0, "0230", "0230", "");
////        0231
//        createUserAndPortfolio("0231", "0231", "0231", "0231", 0, 2, 3, 1, "0231", "0231", "");
////        0234
//        createUserAndPortfolio("0234", "0234", "0234", "0234", 0, 2, 3, 4, "0234", "0234", "");
////        0240
//        createUserAndPortfolio("0240", "0240", "0240", "0240", 0, 2, 4, 0, "0240", "0240", "");
////        0241
//        createUserAndPortfolio("0241", "0241", "0241", "0241", 0, 2, 4, 1, "0241", "0241", "");
////        0243
//        createUserAndPortfolio("0243", "0243", "0243", "0243", 0, 2, 4, 3, "0243", "0243", "");
////        0300
//        createUserAndPortfolio("0300", "0300", "0300", "0300", 0, 3, 0, 0, "0300", "0300", "");
////        0301
//        createUserAndPortfolio("0301", "0301", "0301", "0301", 0, 3, 0, 1, "0301", "0301", "");
////        0302
//        createUserAndPortfolio("0302", "0302", "0302", "0302", 0, 3, 0, 2, "0302", "0302", "");
////        0304
//        createUserAndPortfolio("0304", "0304", "0304", "0304", 0, 3, 0, 4, "0304", "0304", "");
////        0310
//        createUserAndPortfolio("0310", "0310", "0310", "0310", 0, 3, 1, 0, "0310", "0310", "");
////        0312
//        createUserAndPortfolio("0312", "0312", "0312", "0312", 0, 3, 1, 2, "0312", "0312", "");
////        0314
//        createUserAndPortfolio("0314", "0314", "0314", "0314", 0, 3, 1, 4, "0314", "0314", "");
////        0320
//        createUserAndPortfolio("0320", "0320", "0320", "0320", 0, 3, 2, 0, "0320", "0320", "");
////        0321
//        createUserAndPortfolio("0321", "0321", "0321", "0321", 0, 3, 2, 1, "0321", "0321", "");
////        0324
//        createUserAndPortfolio("0324", "0324", "0324", "0324", 0, 3, 2, 4, "0324", "0324", "");
////        0340
//        createUserAndPortfolio("0340", "0340", "0340", "0340", 0, 3, 4, 0, "0340", "0340", "");
////        0341
//        createUserAndPortfolio("0341", "0341", "0341", "0341", 0, 3, 4, 1, "0341", "0341", "");
////        0342
//        createUserAndPortfolio("0342", "0342", "0342", "0342", 0, 3, 4, 2, "0342", "0342", "");
////        0400
//        createUserAndPortfolio("0400", "0400", "0400", "0400", 0, 4, 0, 0, "0400", "0400", "");
////        0401
//        createUserAndPortfolio("0401", "0401", "0401", "0401", 0, 4, 0, 1, "0401", "0401", "");
////        0402
//        createUserAndPortfolio("0402", "0402", "0402", "0402", 0, 4, 0, 2, "0402", "0402", "");
////        0403
//        createUserAndPortfolio("0403", "0403", "0403", "0403", 0, 4, 0, 3, "0403", "0403", "");
////        0410
//        createUserAndPortfolio("0410", "0410", "0410", "0410", 0, 4, 1, 0, "0410", "0410", "");
////        0412
//        createUserAndPortfolio("0412", "0412", "0412", "0412", 0, 4, 1, 2, "0412", "0412", "");
////        0413
//        createUserAndPortfolio("0413", "0413", "0413", "0413", 0, 4, 1, 3, "0413", "0413", "");
////        0420
//        createUserAndPortfolio("0420", "0420", "0420", "0420", 0, 4, 2, 0, "0420", "0420", "");
////        0421
//        createUserAndPortfolio("0421", "0421", "0421", "0421", 0, 4, 2, 1, "0421", "0421", "");
////        0423
//        createUserAndPortfolio("0423", "0423", "0423", "0423", 0, 4, 2, 3, "0423", "0423", "");
////        0430
//        createUserAndPortfolio("0430", "0430", "0430", "0430", 0, 4, 3, 0, "0430", "0430", "");
////        0431
//        createUserAndPortfolio("0431", "0431", "0431", "0431", 0, 4, 3, 1, "0431", "0431", "");
////        0432
//        createUserAndPortfolio("0432", "0432", "0432", "0432", 0, 4, 3, 2, "0432", "0432", "");
////        1000
//        createUserAndPortfolio("1000", "1000", "1000", "1000", 1, 0, 0, 0, "1000", "1000", "");
////        1002
//        createUserAndPortfolio("1002", "1002", "1002", "1002", 1, 0, 0, 2, "1002", "1002", "");
////        1003
//        createUserAndPortfolio("1003", "1003", "1003", "1003", 1, 0, 0, 3, "1003", "1003", "");
////        1004
//        createUserAndPortfolio("1004", "1004", "1004", "1004", 1, 0, 0, 4, "1004", "1004", "");
////        1020
//        createUserAndPortfolio("1020", "1020", "1020", "1020", 1, 0, 2, 0, "1020", "1020", "");
////        1023
//        createUserAndPortfolio("1023", "1023", "1023", "1023", 1, 0, 2, 3, "1023", "1023", "");
////        1024
//        createUserAndPortfolio("1024", "1024", "1024", "1024", 1, 0, 2, 4, "1024", "1024", "");
////        1030
//        createUserAndPortfolio("1030", "1030", "1030", "1030", 1, 0, 3, 0, "1030", "1030", "");
////        1032
//        createUserAndPortfolio("1032", "1032", "1032", "1032", 1, 0, 3, 2, "1032", "1032", "");
////        1034
//        createUserAndPortfolio("1034", "1034", "1034", "1034", 1, 0, 3, 4, "1034", "1034", "");
////        1040
//        createUserAndPortfolio("1040", "1040", "1040", "1040", 1, 0, 4, 0, "1040", "1040", "");
////        1042
//        createUserAndPortfolio("1042", "1042", "1042", "1042", 1, 0, 4, 2, "1042", "1042", "");
////        1043
//        createUserAndPortfolio("1043", "1043", "1043", "1043", 1, 0, 4, 3, "1043", "1043", "");
////        1200
//        createUserAndPortfolio("1200", "1200", "1200", "1200", 1, 2, 0, 0, "1200", "1200", "");
////        1203
//        createUserAndPortfolio("1203", "1203", "1203", "1203", 1, 2, 0, 3, "1203", "1203", "");
////        1204
//        createUserAndPortfolio("1204", "1204", "1204", "1204", 1, 2, 0, 4, "1204", "1204", "");
////        1230
//        createUserAndPortfolio("1230", "1230", "1230", "1230", 1, 2, 3, 0, "1230", "1230", "");
////        1234
//        createUserAndPortfolio("1234", "1234", "1234", "1234", 1, 2, 3, 4, "1234", "1234", "");
////        1240
//        createUserAndPortfolio("1240", "1240", "1240", "1240", 1, 2, 4, 0, "1240", "1240", "");
////        1243
//        createUserAndPortfolio("1243", "1243", "1243", "1243", 1, 2, 4, 3, "1243", "1243", "");
////        1300
//        createUserAndPortfolio("1300", "1300", "1300", "1300", 1, 3, 0, 0, "1300", "1300", "");
////        1302
//        createUserAndPortfolio("1302", "1302", "1302", "1302", 1, 3, 0, 2, "1302", "1302", "");
////        1304
//        createUserAndPortfolio("1304", "1304", "1304", "1304", 1, 3, 0, 4, "1304", "1304", "");
////        1320
//        createUserAndPortfolio("1320", "1320", "1320", "1320", 1, 3, 2, 0, "1320", "1320", "");
////        1324
//        createUserAndPortfolio("1324", "1324", "1324", "1324", 1, 3, 2, 4, "1324", "1324", "");
////        1340
//        createUserAndPortfolio("1340", "1340", "1340", "1340", 1, 3, 4, 0, "1340", "1340", "");
////        1342
//        createUserAndPortfolio("1342", "1342", "1342", "1342", 1, 3, 4, 2, "1342", "1342", "");
////        1400
//        createUserAndPortfolio("1400", "1400", "1400", "1400", 1, 4, 0, 0, "1400", "1400", "");
////        1402
//        createUserAndPortfolio("1402", "1402", "1402", "1402", 1, 4, 0, 2, "1402", "1402", "");
////        1403
//        createUserAndPortfolio("1403", "1403", "1403", "1403", 1, 4, 0, 3, "1403", "1403", "");
////        1420
//        createUserAndPortfolio("1420", "1420", "1420", "1420", 1, 4, 2, 0, "1420", "1420", "");
////        1423
//        createUserAndPortfolio("1423", "1423", "1423", "1423", 1, 4, 2, 3, "1423", "1423", "");
////        1430
//        createUserAndPortfolio("1430", "1430", "1430", "1430", 1, 4, 3, 0, "1430", "1430", "");
////        1432
//        createUserAndPortfolio("1432", "1432", "1432", "1432", 1, 4, 3, 2, "1432", "1432", "");
////        2000
//        createUserAndPortfolio("2000", "2000", "2000", "2000", 2, 0, 0, 0, "2000", "2000", "");
////        2001
//        createUserAndPortfolio("2001", "2001", "2001", "2001", 2, 0, 0, 1, "2001", "2001", "");
////        2003
//        createUserAndPortfolio("2003", "2003", "2003", "2003", 2, 0, 0, 3, "2003", "2003", "");
////        2004
//        createUserAndPortfolio("2004", "2004", "2004", "2004", 2, 0, 0, 4, "2004", "2004", "");
////        2010
//        createUserAndPortfolio("2010", "2010", "2010", "2010", 2, 0, 1, 0, "2010", "2010", "");
////        2013
//        createUserAndPortfolio("2013", "2013", "2013", "2013", 2, 0, 1, 3, "2013", "2013", "");
////        2014
//        createUserAndPortfolio("2014", "2014", "2014", "2014", 2, 0, 1, 4, "2014", "2014", "");
////        2030
//        createUserAndPortfolio("2030", "2030", "2030", "2030", 2, 0, 3, 0, "2030", "2030", "");
////        2031
//        createUserAndPortfolio("2031", "2031", "2031", "2031", 2, 0, 3, 1, "2031", "2031", "");
////        2034
//        createUserAndPortfolio("2034", "2034", "2034", "2034", 2, 0, 3, 4, "2034", "2034", "");
////        2040
//        createUserAndPortfolio("2040", "2040", "2040", "2040", 2, 0, 4, 0, "2040", "2040", "");
////        2041
//        createUserAndPortfolio("2041", "2041", "2041", "2041", 2, 0, 4, 1, "2041", "2041", "");
////        2043
//        createUserAndPortfolio("2043", "2043", "2043", "2043", 2, 0, 4, 3, "2043", "2043", "");
////        2100
//        createUserAndPortfolio("2100", "2100", "2100", "2100", 2, 1, 0, 0, "2100", "2100", "");
////        2103
//        createUserAndPortfolio("2103", "2103", "2103", "2103", 2, 1, 0, 3, "2103", "2103", "");
////        2104
//        createUserAndPortfolio("2104", "2104", "2104", "2104", 2, 1, 0, 4, "2104", "2104", "");
////        2130
//        createUserAndPortfolio("2130", "2130", "2130", "2130", 2, 1, 3, 0, "2130", "2130", "");
////        2134
//        createUserAndPortfolio("2134", "2134", "2134", "2134", 2, 1, 3, 4, "2134", "2134", "");
////        2140
//        createUserAndPortfolio("2140", "2140", "2140", "2140", 2, 1, 4, 0, "2140", "2140", "");
////        2143
//        createUserAndPortfolio("2143", "2143", "2143", "2143", 2, 1, 4, 3, "2143", "2143", "");
////        2300
//        createUserAndPortfolio("2300", "2300", "2300", "2300", 2, 3, 0, 0, "2300", "2300", "");
////        2301
//        createUserAndPortfolio("2301", "2301", "2301", "2301", 2, 3, 0, 1, "2301", "2301", "");
////        2304
//        createUserAndPortfolio("2304", "2304", "2304", "2304", 2, 3, 0, 4, "2304", "2304", "");
////        2310
//        createUserAndPortfolio("2310", "2310", "2310", "2310", 2, 3, 1, 0, "2310", "2310", "");
////        2314
//        createUserAndPortfolio("2314", "2314", "2314", "2314", 2, 3, 1, 4, "2314", "2314", "");
////        2340
//        createUserAndPortfolio("2340", "2340", "2340", "2340", 2, 3, 4, 0, "2340", "2340", "");
////        2341
//        createUserAndPortfolio("2341", "2341", "2341", "2341", 2, 3, 4, 1, "2341", "2341", "");
////        2400
//        createUserAndPortfolio("2400", "2400", "2400", "2400", 2, 4, 0, 0, "2400", "2400", "");
////        2401
//        createUserAndPortfolio("2401", "2401", "2401", "2401", 2, 4, 0, 1, "2401", "2401", "");
////        2403
//        createUserAndPortfolio("2403", "2403", "2403", "2403", 2, 4, 0, 3, "2403", "2403", "");
////        2410
//        createUserAndPortfolio("2410", "2410", "2410", "2410", 2, 4, 1, 0, "2410", "2410", "");
////        2413
//        createUserAndPortfolio("2413", "2413", "2413", "2413", 2, 4, 1, 3, "2413", "2413", "");
////        2430
//        createUserAndPortfolio("2430", "2430", "2430", "2430", 2, 4, 3, 0, "2430", "2430", "");
////        2431
//        createUserAndPortfolio("2431", "2431", "2431", "2431", 2, 4, 3, 1, "2431", "2431", "");
////        3000
//        createUserAndPortfolio("3000", "3000", "3000", "3000", 3, 0, 0, 0, "3000", "3000", "");
////        3001
//        createUserAndPortfolio("3001", "3001", "3001", "3001", 3, 0, 0, 1, "3001", "3001", "");
////        3002
//        createUserAndPortfolio("3002", "3002", "3002", "3002", 3, 0, 0, 2, "3002", "3002", "");
////        3004
//        createUserAndPortfolio("3004", "3004", "3004", "3004", 3, 0, 0, 4, "3004", "3004", "");
////        3010
//        createUserAndPortfolio("3010", "3010", "3010", "3010", 3, 0, 1, 0, "3010", "3010", "");
////        3012
//        createUserAndPortfolio("3012", "3012", "3012", "3012", 3, 0, 1, 2, "3012", "3012", "");
////        3014
//        createUserAndPortfolio("3014", "3014", "3014", "3014", 3, 0, 1, 4, "3014", "3014", "");
////        3020
//        createUserAndPortfolio("3020", "3020", "3020", "3020", 3, 0, 2, 0, "3020", "3020", "");
////        3021
//        createUserAndPortfolio("3021", "3021", "3021", "3021", 3, 0, 2, 1, "3021", "3021", "");
////        3024
//        createUserAndPortfolio("3024", "3024", "3024", "3024", 3, 0, 2, 4, "3024", "3024", "");
////        3040
//        createUserAndPortfolio("3040", "3040", "3040", "3040", 3, 0, 4, 0, "3040", "3040", "");
////        3041
//        createUserAndPortfolio("3041", "3041", "3041", "3041", 3, 0, 4, 1, "3041", "3041", "");
////        3042
//        createUserAndPortfolio("3042", "3042", "3042", "3042", 3, 0, 4, 2, "3042", "3042", "");
////        3100
//        createUserAndPortfolio("3100", "3100", "3100", "3100", 3, 1, 0, 0, "3100", "3100", "");
////        3102
//        createUserAndPortfolio("3102", "3102", "3102", "3102", 3, 1, 0, 2, "3102", "3102", "");
////        3104
//        createUserAndPortfolio("3104", "3104", "3104", "3104", 3, 1, 0, 4, "3104", "3104", "");
////        3120
//        createUserAndPortfolio("3120", "3120", "3120", "3120", 3, 1, 2, 0, "3120", "3120", "");
////        3124
//        createUserAndPortfolio("3124", "3124", "3124", "3124", 3, 1, 2, 4, "3124", "3124", "");
////        3140
//        createUserAndPortfolio("3140", "3140", "3140", "3140", 3, 1, 4, 0, "3140", "3140", "");
////        3142
//        createUserAndPortfolio("3142", "3142", "3142", "3142", 3, 1, 4, 2, "3142", "3142", "");
////        3200
//        createUserAndPortfolio("3200", "3200", "3200", "3200", 3, 2, 0, 0, "3200", "3200", "");
////        3201
//        createUserAndPortfolio("3201", "3201", "3201", "3201", 3, 2, 0, 1, "3201", "3201", "");
////        3204
//        createUserAndPortfolio("3204", "3204", "3204", "3204", 3, 2, 0, 4, "3204", "3204", "");
////        3210
//        createUserAndPortfolio("3210", "3210", "3210", "3210", 3, 2, 1, 0, "3210", "3210", "");
////        3214
//        createUserAndPortfolio("3214", "3214", "3214", "3214", 3, 2, 1, 4, "3214", "3214", "");
////        3240
//        createUserAndPortfolio("3240", "3240", "3240", "3240", 3, 2, 4, 0, "3240", "3240", "");
////        3241
//        createUserAndPortfolio("3241", "3241", "3241", "3241", 3, 2, 4, 1, "3241", "3241", "");
////        3400
//        createUserAndPortfolio("3400", "3400", "3400", "3400", 3, 4, 0, 0, "3400", "3400", "");
////        3401
//        createUserAndPortfolio("3401", "3401", "3401", "3401", 3, 4, 0, 1, "3401", "3401", "");
////        3402
//        createUserAndPortfolio("3402", "3402", "3402", "3402", 3, 4, 0, 2, "3402", "3402", "");
////        3410
//        createUserAndPortfolio("3410", "3410", "3410", "3410", 3, 4, 1, 0, "3410", "3410", "");
////        3412
//        createUserAndPortfolio("3412", "3412", "3412", "3412", 3, 4, 1, 2, "3412", "3412", "");
////        3420
//        createUserAndPortfolio("3420", "3420", "3420", "3420", 3, 4, 2, 0, "3420", "3420", "");
////        3421
//        createUserAndPortfolio("3421", "3421", "3421", "3421", 3, 4, 2, 1, "3421", "3421", "");
////        4000
//        createUserAndPortfolio("4000", "4000", "4000", "4000", 4, 0, 0, 0, "4000", "4000", "");
////        4001
//        createUserAndPortfolio("4001", "4001", "4001", "4001", 4, 0, 0, 1, "4001", "4001", "");
////        4002
//        createUserAndPortfolio("4002", "4002", "4002", "4002", 4, 0, 0, 2, "4002", "4002", "");
////        4003
//        createUserAndPortfolio("4003", "4003", "4003", "4003", 4, 0, 0, 3, "4003", "4003", "");
////        4010
//        createUserAndPortfolio("4010", "4010", "4010", "4010", 4, 0, 1, 0, "4010", "4010", "");
////        4012
//        createUserAndPortfolio("4012", "4012", "4012", "4012", 4, 0, 1, 2, "4012", "4012", "");
////        4013
//        createUserAndPortfolio("4013", "4013", "4013", "4013", 4, 0, 1, 3, "4013", "4013", "");
////        4020
//        createUserAndPortfolio("4020", "4020", "4020", "4020", 4, 0, 2, 0, "4020", "4020", "");
////        4021
//        createUserAndPortfolio("4021", "4021", "4021", "4021", 4, 0, 2, 1, "4021", "4021", "");
////        4023
//        createUserAndPortfolio("4023", "4023", "4023", "4023", 4, 0, 2, 3, "4023", "4023", "");
////        4030
//        createUserAndPortfolio("4030", "4030", "4030", "4030", 4, 0, 3, 0, "4030", "4030", "");
////        4031
//        createUserAndPortfolio("4031", "4031", "4031", "4031", 4, 0, 3, 1, "4031", "4031", "");
////        4032
//        createUserAndPortfolio("4032", "4032", "4032", "4032", 4, 0, 3, 2, "4032", "4032", "");
////        4100
//        createUserAndPortfolio("4100", "4100", "4100", "4100", 4, 1, 0, 0, "4100", "4100", "");
////        4102
//        createUserAndPortfolio("4102", "4102", "4102", "4102", 4, 1, 0, 2, "4102", "4102", "");
////        4103
//        createUserAndPortfolio("4103", "4103", "4103", "4103", 4, 1, 0, 3, "4103", "4103", "");
////        4120
//        createUserAndPortfolio("4120", "4120", "4120", "4120", 4, 1, 2, 0, "4120", "4120", "");
////        4123
//        createUserAndPortfolio("4123", "4123", "4123", "4123", 4, 1, 2, 3, "4123", "4123", "");
////        4130
//        createUserAndPortfolio("4130", "4130", "4130", "4130", 4, 1, 3, 0, "4130", "4130", "");
////        4132
//        createUserAndPortfolio("4132", "4132", "4132", "4132", 4, 1, 3, 2, "4132", "4132", "");
////        4200
//        createUserAndPortfolio("4200", "4200", "4200", "4200", 4, 2, 0, 0, "4200", "4200", "");
////        4201
//        createUserAndPortfolio("4201", "4201", "4201", "4201", 4, 2, 0, 1, "4201", "4201", "");
////        4203
//        createUserAndPortfolio("4203", "4203", "4203", "4203", 4, 2, 0, 3, "4203", "4203", "");
////        4210
//        createUserAndPortfolio("4210", "4210", "4210", "4210", 4, 2, 1, 0, "4210", "4210", "");
////        4213
//        createUserAndPortfolio("4213", "4213", "4213", "4213", 4, 2, 1, 3, "4213", "4213", "");
////        4230
//        createUserAndPortfolio("4230", "4230", "4230", "4230", 4, 2, 3, 0, "4230", "4230", "");
////        4231
//        createUserAndPortfolio("4231", "4231", "4231", "4231", 4, 2, 3, 1, "4231", "4231", "");
////        4300
//        createUserAndPortfolio("4300", "4300", "4300", "4300", 4, 3, 0, 0, "4300", "4300", "");
////        4301
//        createUserAndPortfolio("4301", "4301", "4301", "4301", 4, 3, 0, 1, "4301", "4301", "");
////        4302
//        createUserAndPortfolio("4302", "4302", "4302", "4302", 4, 3, 0, 2, "4302", "4302", "");
////        4310
//        createUserAndPortfolio("4310", "4310", "4310", "4310", 4, 3, 1, 0, "4310", "4310", "");
////        4312
//        createUserAndPortfolio("4312", "4312", "4312", "4312", 4, 3, 1, 2, "4312", "4312", "");
////        4320
//        createUserAndPortfolio("4320", "4320", "4320", "4320", 4, 3, 2, 0, "4320", "4320", "");
////        4321
//        createUserAndPortfolio("4321", "4321", "4321", "4321", 4, 3, 2, 1, "4321", "4321", "");

        // 초기 데이터 생성 및 저장(2)
        User user7000 = User.builder()
                .userName("최세영")
                .nickName("ssangnamja")
                .email("csy626@g.hongik.ac.kr")
                .password(passwordEncoder.encode("626"))  // 비밀번호 해싱
                .role(Role.USER)
                .lastAccessDate(LocalDateTime.of(2023, 8, 27, 14, 30, 0))
                .imageUrl("profile4.png")
                .build();

        userRepository.save(user7000);

        Portfolio user7000Portfolio = Portfolio.builder()
                .user(user7000)
                .web(4)
                .app(3)
                .game(2)
                .ai(1)
                .shortIntroduce("카카오 네이버? 어림없지 스타트업 차릴 준비합니다.")
                .introduce("렉스, 야크, 프롤로그를 넘어서 콜그래프까지 과제를 제출하였습니다. \n인생에서 그것만큼 뿌듯한 게 없었던 것 같습니다. \n 이러한 열정으로 여러분과 같이 함께 성장해나가겠습니다.")
                //.fileUrl("")
                .build();

        portfolioRepository.save(user7000Portfolio);

        // 초기 데이터 생성 및 저장(2)
        User user7001 = User.builder()
                .userName("이동렬")
                .nickName("홍익키다리")
                .email("ldl515@g.hongik.ac.kr")
                .password(passwordEncoder.encode("515"))  // 비밀번호 해싱
                .role(Role.USER)
                .lastAccessDate(LocalDateTime.of(2023, 8, 27, 14, 30, 0))
                .imageUrl("profile1.png")
                .build();

        userRepository.save(user7001);

        Portfolio user7001Portfolio = Portfolio.builder()
                .user(user7001)
                .web(1)
                .app(2)
                .game(3)
                .ai(4)
                .shortIntroduce("기계과지만, 소프트웨어에 관심있는 4학년입니다.")
                .introduce("자율주행에 관심있다보니, 기계과에서 배우는 것만으로는 부족하단 생각이 들었습니다. \n 자율주행 소프트웨어 공부 같이 하실 분 있으면 꼭 함께해요!")
                //.fileUrl("")
                .build();

        portfolioRepository.save(user7001Portfolio);

        // 초기 데이터 생성 및 저장(2)
        User user7002 = User.builder()
                .userName("노호수")
                .nickName("문산토박이")
                .email("hosoo313@g.hongik.ac.kr")
                .password(passwordEncoder.encode("313"))  // 비밀번호 해싱
                .role(Role.USER)
                .lastAccessDate(LocalDateTime.of(2023, 8, 27, 14, 30, 0))
                .imageUrl("profile2.png")
                .build();

        userRepository.save(user7002);

        Portfolio user7002Portfolio = Portfolio.builder()
                .user(user7002)
                .web(1)
                .app(2)
                .game(3)
                .ai(4)
                .shortIntroduce("현재 군대에 있지만, 내년에 복학하면 같이 열심히 성장할 학우분들을 모십니다.")
                .introduce("NLP와 AI분야에 관심있습니다. \n 여기서 열심히 해서 꼭 네이버 가겠습니다. \n 플젝이든 스터디든 먼저 제 이메일로 연락주세요! 빠르게 답변 드리겠습니다!!")
                //.fileUrl("")
                .build();

        portfolioRepository.save(user7002Portfolio);

        // 초기 데이터 생성 및 저장(2)
        User user7003 = User.builder()
                .userName("이선재")
                .nickName("언리얼장인")
                .email("sundae1226@g.hongik.ac.kr")
                .password(passwordEncoder.encode("1226"))  // 비밀번호 해싱
                .role(Role.USER)
                .lastAccessDate(LocalDateTime.of(2023, 8, 27, 14, 30, 0))
                .imageUrl("profile3.png")
                .build();

        userRepository.save(user7003);

        Portfolio user7003Portfolio = Portfolio.builder()
                .user(user7003)
                .web(1)
                .app(2)
                .game(4)
                .ai(3)
                .shortIntroduce("홍익대학교 컴퓨터공학과에 재학중인, 게임 개발에 관심있는 이선재입니다.")
                .introduce("앞서 말씀드렸다시피, 게임 분야에 큰 관심이 있어, 유니티와 언리얼 관련해서 게임 제작 4건 정도를 해보았습니다. 유니티 같은 경우에는, 초중학생들 대상으로 학원에서 일하며 가르친 적도 있습니다. 요새 유니티 엔진이 핫하다 해서 이쪽으로 좀 더 파고 있는데, 같이 성장하실 분 있으면 같이 공부하면 좋을 것 같아요!. \n \n 플젝이든 스터디든 먼저 제 이메일로 연락주세요! 아 그리고 dm도 가능하니 이쪽으로 연락주세요! \n DM: 1231DASJK123J 입니다")
                //.fileUrl("")
                .build();

        portfolioRepository.save(user7003Portfolio);


    }

}
