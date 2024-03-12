package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface CategoryRepository  extends JpaRepository<Category, Long>, QuerydslPredicateExecutor<Category> {

    // Post의 Id로 해당하는 카테고리 찾기
    Optional<Category> findCategoryById(Long postId);
}
