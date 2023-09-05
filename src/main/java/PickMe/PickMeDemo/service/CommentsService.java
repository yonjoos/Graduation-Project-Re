package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.CommentRequestDto;
import PickMe.PickMeDemo.entity.Comments;
import PickMe.PickMeDemo.entity.Posts;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.CommentsRepository;
import PickMe.PickMeDemo.repository.PostsRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentsService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final CommentsRepository commentsRepository; // 댓글 저장소 추가


    public void registerComment(Long postId, CommentRequestDto commentRequestDTO, String userEmail) {


        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        Posts findPosts = postsRepository.findById(postId)
                .orElseThrow(() -> new AppException("해당하는 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));


        Comments comment = new Comments(commentRequestDTO.getContent()); // 댓글 내용 설정
        Comments parentComment;
        if (commentRequestDTO.getParentId() != null) { //부모 댓글이 있는 경우
            parentComment = commentsRepository.findById(commentRequestDTO.getParentId())
                    .orElseThrow(() -> new AppException("해당 부모댓글을 찾을 수 없습니다." ,HttpStatus.BAD_REQUEST));
            comment.updateParent(parentComment);
        }

        comment.updateWriter(findUser);
        comment.updatePosts(findPosts);

        commentsRepository.save(comment);

    }
}
