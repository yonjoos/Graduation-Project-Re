package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.PortfolioDto;
import PickMe.PickMeDemo.dto.PortfolioFormDto;
import PickMe.PickMeDemo.dto.UserDto;
import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.Role;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.PortfolioRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository; // Add this if not already defined
    // Portfolio Mapper를 사용하고자 했으나, 이상하게 스프링 빈으로 등록이 안되어서, private final 변수로 사용할 수 없었음.
    
    
    // 포트폴리오 등록
    public PortfolioDto uploadPortfolio(PortfolioFormDto portfolioFormDto, UserDto userDto) {

        // Optional이므로, 해당 유저가 발견되면 유저를 반환, 해당 유저가 없으면 null 반환
        Optional<User> findUser = userRepository.findByEmail(userDto.getEmail());

        // orElseThrow(...): 이 메서드는 Optional 객체에서 호출됩니다.
        // 비어있는 경우 예외 객체를 생성하고 던질 람다 표현식을 받습니다.
        // 이 경우 Optional이 비어있는 경우(사용자를 찾지 못한 경우) "사용자를 찾을 수 없습니다"라는 메시지와 BAD_REQUEST (400) HTTP 상태 코드를 가진 AppException이 생성됩니다.
        User user = findUser.orElseThrow(() -> new AppException("User not found", HttpStatus.BAD_REQUEST));

        Portfolio registerPortfolio =
                new Portfolio(user, portfolioFormDto.getWeb(), portfolioFormDto.getApp(),
                        portfolioFormDto.getGame(), portfolioFormDto.getAi(),
                        portfolioFormDto.getShortIntroduce(), portfolioFormDto.getIntroduce(),
                        portfolioFormDto.getFileUrl());

        Portfolio portfolio = portfolioRepository.save(registerPortfolio);

        // portfolioDto의 필드 : nickName, email, web, app, game, ai, shortIntroduce, introduce, fileUrl

        PortfolioDto portfolioDto = new PortfolioDto(
                userDto.getNickName(), userDto.getEmail(), portfolio.getWeb(), portfolio.getApp(),
                portfolio.getGame(), portfolio.getAi(), portfolio.getShortIntroduce(), portfolioFormDto.getIntroduce(),
                portfolioFormDto.getFileUrl());

        return portfolioDto;
    }



    // 포트폴리오 조회
    @EntityGraph(attributePaths = "user")
    public PortfolioDto getPortfolio(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        Portfolio portfolio = portfolioRepository.findByUser(user)
                .orElseThrow(() -> new AppException("포트폴리오를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        PortfolioDto portfolioDto = PortfolioDto.builder()
                .nickName(user.getNickName())
                .email(user.getEmail())
                .web(portfolio.getWeb())
                .app(portfolio.getApp())
                .game(portfolio.getGame())
                .ai(portfolio.getAi())
                .shortIntroduce(portfolio.getShortIntroduce())
                .introduce(portfolio.getIntroduce())
                .fileUrl(portfolio.getFileUrl())
                .build();

        return portfolioDto;
    }
}
