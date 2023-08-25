package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository  extends JpaRepository<Category, Long> {
}
