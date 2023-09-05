package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<Comments,Long>, CommentsRepositoryCustom {

}
