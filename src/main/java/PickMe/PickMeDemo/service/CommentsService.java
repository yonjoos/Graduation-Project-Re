package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.CommentRequestDto;
import PickMe.PickMeDemo.dto.CommentResponseDto;
import PickMe.PickMeDemo.entity.Comments;
import PickMe.PickMeDemo.entity.Posts;
import PickMe.PickMeDemo.entity.QComments;
import PickMe.PickMeDemo.entity.User;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.CommentsRepository;
import PickMe.PickMeDemo.repository.PostsRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static PickMe.PickMeDemo.entity.QComments.comments;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentsService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final CommentsRepository commentsRepository; // 댓글 저장소 추가
    private final JPAQueryFactory queryFactory;


    // 댓글, 답글 등록 관련 in project 게시물
    public void registerComment(Long projectId, CommentRequestDto commentRequestDTO, String userEmail) {


        // 요청을 보낸 회원을 식별
        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 요청된 게시물을 식별
        Posts findPosts = postsRepository.findById(projectId)
                .orElseThrow(() -> new AppException("해당하는 게시물을 찾을 수 없습니다", HttpStatus.BAD_REQUEST));


        Comments comment = new Comments(commentRequestDTO.getContent()); // 댓글 내용을 미리 생성자 통해 넣어줌
        Comments parentComment; // 부모 댓글 객체 선언

        if (commentRequestDTO.getParentId() != null) { //부모 댓글이 있는 경우
            parentComment = commentsRepository.findById(commentRequestDTO.getParentId())
                    .orElseThrow(() -> new AppException("해당 부모댓글을 찾을 수 없습니다." ,HttpStatus.BAD_REQUEST));

            comment.updateParent(parentComment); // 요청 받은 댓글의 부모 관계를 설정
        }

        comment.updateWriter(findUser); // 요청 받은 댓글에 user정보 매핑
        comment.updatePosts(findPosts); // 요청 받은 댓글에 게시물 정보 매핑

        // 댓글 레포지토리에 저장
        commentsRepository.save(comment);

    }

    // 댓글, 답글 조회 in 프로젝트 게시물
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsForProject(Long projectId, String userEmail) {

        QComments comments = QComments.comments;

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        JPAQuery<Comments> query = queryFactory.selectFrom(comments) // 댓글을 조회할건데,
                .leftJoin(comments.parent).fetchJoin()  // 부모 댓글도 같이 조회할거야
                .where(comments.posts.id.eq(projectId)) // 근데 그 댓글은 이 프로젝트 게시물에 해당하는거고
                .orderBy(comments.parent.id.asc().nullsFirst(), // 부모가 없는 애들이 정렬의 우선순위가 높아
                        comments.createdDate.asc()); // 그리고, 시간이 빠른순으로 정렬

        List<Comments> findComments = query.fetch();

        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>(); // 최종적으로 frontEnd에 나갈 부모자식 댓글 관계 배열
        Map<Long, CommentResponseDto> commentDtoHashMap = new HashMap<>(); // 일차적으로 부모와 자식들을 이어주기 위한 map자료

        findComments.forEach(c -> {
            CommentResponseDto commentResponseDto;

            if (c.getIsDeleted()) { // 댓글의 isDeleted가 true라면

                commentResponseDto = CommentResponseDto.builder()
                        .id(c.getId())
                        .content("삭제된 댓글입니다.")
                        .nickName(null)
                        .userId(null)
                        .commentWriter(false)
                        .build();
            } else { // 해당 댓글의 isDeleted가 false라면

                boolean isCommentWriter = currentUser.getId().equals(c.getUser().getId()); // 해당 댓글을 쓴 user의 id를 식별

                commentResponseDto = CommentResponseDto.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .nickName(c.getUser().getNickName())
                        .userId(c.getUser().getId())
                        .commentWriter(isCommentWriter)
                        .build();
            }

            commentDtoHashMap.put(commentResponseDto.getId(), commentResponseDto); // 부모가 없는 애들이 위에 있으므로 얘네들은 바로 hashmap에 넣어주기
            if (c.getParent() != null) // 부모가 있는 애는 이미 hashmap에 들어있는 부모의 자식으로 넣어주기
            {
                commentDtoHashMap.get(c.getParent().getId()).getChildren().add(commentResponseDto);
            }
            else // 부모가 없으므로, 최종적으로 나갈 list자료형에 바로 추가
            {
                commentResponseDtoList.add(commentResponseDto);
            }
        });
        return commentResponseDtoList;
    }

    // 특정 댓글 또는 답글 삭제 in 프로젝트 게시물
    public void deleteComment(Long commentId, String userEmail) {

        QComments comments = QComments.comments;

        Comments selectedComment = queryFactory.select(comments) // 댓글을 대상으로 할 건데
                .from(comments)
                .leftJoin(comments.parent).fetchJoin() // 부모 댓글도 같이 가져옴
                .where(comments.id.eq(commentId)) // parameter로 받은 댓글 id가 같은 것만 가져올 것
                .fetchOne();

        Optional<Comments> optionalSelectedComment = Optional.ofNullable(selectedComment);

        Comments comment = optionalSelectedComment.orElseThrow(
                () -> new AppException("Could not find comment id: " + commentId, HttpStatus.BAD_REQUEST));


        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));


        if(currentUser.getId().equals(comment.getUser().getId())) // 현재 로그인 한 회원이 댓글의 작성자와 같다면
        {
            if(comment.getChildren().size() != 0) { // 자식이 있으면 상태만 삭제된 상태로 변경
                comment.changeIsDeleted(true);
            } else { // 내 댓글에 자식이 없다면 isDeleted가 true인 부모 댓글이 있는지 보고, 부모의 자식이 1개(나 자신)밖에 없으면 부모도 삭제
                commentsRepository.delete(getDeletableAncestorComment(comment));
            }
        }

    }

    private Comments getDeletableAncestorComment(Comments comment) {
        Comments parent = comment.getParent(); // 현재 댓글의 부모를 구함
        if(parent != null && parent.getChildren().size() == 1 && parent.getIsDeleted())
            // 부모가 있고, 부모의 자식이 1개(지금 삭제하는 댓글)이고, 부모의 삭제 상태가 TRUE인 댓글이라면 재귀
            return getDeletableAncestorComment(parent);
        return comment; // 삭제해야하는 댓글 반환
    }
}
