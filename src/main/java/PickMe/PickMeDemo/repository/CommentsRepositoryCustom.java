package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.dto.CommentResponseDto;
import PickMe.PickMeDemo.entity.Comments;

import java.util.List;
import java.util.Optional;

public interface CommentsRepositoryCustom {

    List<CommentResponseDto> findByPosts_Id(Long id);

    Optional<Comments> findCommentsByIdWithParent(Long id);
}
