package PickMe.PickMeDemo.repository;

import PickMe.PickMeDemo.dto.CommentResponseDto;
import PickMe.PickMeDemo.entity.Comments;
import PickMe.PickMeDemo.entity.QComments;
import PickMe.PickMeDemo.entity.QPosts;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

import static PickMe.PickMeDemo.dto.CommentResponseDto.convertCommentToDto;

@RequiredArgsConstructor
@Repository
public class CommentsRepositoryImpl implements CommentsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentResponseDto> findByPosts_Id(Long id) {
        return null;
    }

    @Override
    public Optional<Comments> findCommentsByIdWithParent(Long id) {
        return Optional.empty();
    }
}
