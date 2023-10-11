package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.ViewCountPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewCountPortfolioRepository extends JpaRepository<ViewCountPortfolio, Long> {

    // Id로 해당 포트폴리오 조회 수 카운트 후 반환
    Optional<Integer> countByPortfolio_Id(Long portfolioId);

    // 포트폴리오 Id와 user Email로 해당 유저가 해당 게시물에 방문한 적이 있는지 찾기
    Optional<ViewCountPortfolio> findByPortfolio_IdAndUser_Id(Long portfolioId, Long userId);
}
