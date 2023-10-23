package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Recommendations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationsRepository extends JpaRepository<Recommendations, Long> {
}
