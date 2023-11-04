package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Portfolio;
import PickMe.PickMeDemo.entity.PortfolioFiles;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioFilesRepository extends JpaRepository<PortfolioFiles, Long> {

    List<PortfolioFiles> findByPortfolioAndIsImageTrue(Portfolio portfolio);
    List<PortfolioFiles> findByPortfolioAndIsImageFalse(Portfolio portfolio);

    List<PortfolioFiles> findByPortfolio(Portfolio portfolio);
}
