package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUser(User user);

    List<Portfolio> findAll();

    // FOR sending portfolio cards
    // USED in portfolioService
}
