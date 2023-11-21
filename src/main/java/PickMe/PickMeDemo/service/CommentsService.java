package PickMe.PickMeDemo.service;

import PickMe.PickMeDemo.dto.CommentRequestDto;
import PickMe.PickMeDemo.dto.CommentResponseDto;
import PickMe.PickMeDemo.dto.NotificationMessageDto;
import PickMe.PickMeDemo.entity.*;
import PickMe.PickMeDemo.exception.AppException;
import PickMe.PickMeDemo.repository.CommentsRepository;
import PickMe.PickMeDemo.repository.NotificationsRepository;
import PickMe.PickMeDemo.repository.PostsRepository;
import PickMe.PickMeDemo.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;



@Service
@Transactional
@RequiredArgsConstructor
public class CommentsService {

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final CommentsRepository commentsRepository; // 댓글 저장소 추가
    private final NotificationsRepository notificationsRepository;
    private final NotificationService notificationService;
    private final JPAQueryFactory queryFactory;


    // 댓글, 답글 등록 관련 in project / study 게시물
    public void registerComment(Long postId, CommentRequestDto commentRequestDTO, String userEmail) {


        // 요청을 보낸 회원을 식별
        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다", HttpStatus.BAD_REQUEST));

        // 요청된 게시물을 식별
        Posts findPosts = postsRepository.findById(postId)
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


        /////////////////////////아래부터, 댓글 등록 시 notification, sse관련
        // 1. 달리는 댓글에 부모 댓글이 있는 경우
        // -> 그 댓글의 부모 댓글을 쓴 사람인데,
        // -> 부모 댓글을 쓴 사람과 달릴 댓글을 쓴 사람이 다르고
        // -> 부모 댓글을 쓴 사람이 게시물 작성자가 아닌 경우에 부모 댓글 쓴 사람에게 알림 발송

        // 2. 달리는 댓글의 모든 경우에 대해
        // -> 달리는 댓글의 작성자와 게시물 작성자가 다른 경우에 게시물 작성자에게 알림 발송


        if (comment.getParent() != null) { // 달릴 댓글에게 부모 댓글이 있는 경우


            NotificationMessageDto parentNotificationMessage; // 부모 댓글 작성한 사람에게 보낼 실시간 알림 메세지
            String parentNotifyMessage; // 부모 댓글 작성한 사람에게 보낼 공지될 data

//            NotificationMessageDto postWriterNotificationMessage; // 게시물 작성자에게 보낼 실시간 알림 메세지
//            String postWriterNotifyMessage; // 게시물 작성자에게 보낼 공지될 data

            if (PostType.PROJECT.equals(findPosts.getPostType())) { // 프로젝트 게시물일 경우

                // parentNotificationMessage : 실시간 알림 카드에 들어갈 내용(부모 댓글 작성자에게 갈 내용)
                // parentNotifyMessage : Notification 배너 안에 들어갈 카드 내용(부모 댓글 작성자에게 갈 공지

                parentNotificationMessage = new NotificationMessageDto("project/detail/" + findPosts.getId() + ": 프로젝트 게시물 : \"" + findPosts.getTitle() + "\"의 회원님의 댓글에 \"" + findUser.getNickName() + "\"님이 답글을 남겼습니다.\"" + commentRequestDTO.getContent()+"\""); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
                parentNotifyMessage = "프로젝트 게시물 : \"" + findPosts.getTitle() + "\"의 회원님의 댓글에 \"" + findUser.getNickName() + "\"님이 답글을 남겼습니다.\"" + commentRequestDTO.getContent()+"\"";


            }
            else  { // 스터디 게시물일 경우
                parentNotificationMessage = new NotificationMessageDto("study/detail/" + findPosts.getId() + ": 스터디 게시물 : \"" + findPosts.getTitle() + "\"의 회원님의 댓글에 \"" + findUser.getNickName() + "\"님이 답글을 남겼습니다.\"" + commentRequestDTO.getContent()+"\""); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
                parentNotifyMessage = "스터디 게시물 : \"" + findPosts.getTitle() + "\"의 회원님의 댓글에 \"" + findUser.getNickName() + "\"님이 답글을 남겼습니다.\"" + commentRequestDTO.getContent()+"\"";


            }


            if(!comment.getParent().getUser().getId().equals(findUser.getId())) // 현재 답글을 쓴 사용자가 그 댓글의 부모와 다른 사람인데,
            {

                if(!comment.getParent().getUser().getId().equals(findPosts.getUser().getId())) // 부모 댓글을 쓴 사용자가 게시물 작성자가 아닌 경우에 sse, 공지 보냄
                {
                    Optional<User> parentUser = userRepository.findById(comment.getParent().getUser().getId()); // 부모 댓글 작성자 찾기
                    if(parentUser.isPresent())
                    {
                        Notifications parentCommentNotification = Notifications.builder()
                                .user(parentUser.get()) // 부모 댓글 작성자에게 보내기
                                .postId(findPosts.getId())
                                .notificationMessage(parentNotifyMessage)
                                .postType(findPosts.getPostType())
                                .checked(false)
                                .build();

                        Notifications savedParentCommentNotification = notificationsRepository.save(parentCommentNotification);
                        parentNotificationMessage.setMessage(parentNotificationMessage.getMessage() + savedParentCommentNotification.getId().toString());
                        notificationService.notify(comment.getParent().getUser().getId(), parentNotificationMessage); // 부모 대댓글 작성자에게 실시간 알림 전송
                    }
                }

            }


        }

         // 부모 댓글이 있든 없든, 게시물 작성자에게는 댓글 달렸다고 sse, 공지 보내기

        if(!findPosts.getUser().getId().equals(findUser.getId())) // 현재 댓글 또는 답글을 단 사람이 게시물 작성자가 아닐 경우에 실행
        {
            NotificationMessageDto postWriterNotificationMessage; // 게시물 작성자에게 보낼 실시간 알림
            String postWriterNotifyMessage; // 게시물 작성자에게 보낼 공지

            if (PostType.PROJECT.equals(findPosts.getPostType())) {

                postWriterNotificationMessage = new NotificationMessageDto("project/detail/" + findPosts.getId() + ": 프로젝트 게시물 : \"" + findPosts.getTitle() + "\"에 \"" + findUser.getNickName() + "\"님이 댓글을 남겼습니다.\"" + commentRequestDTO.getContent()+"\""); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
                postWriterNotifyMessage = "프로젝트 게시물 : \"" + findPosts.getTitle() + "\"에 \"" + findUser.getNickName() + "\"님이 댓글을 남겼습니다.\n\"" + commentRequestDTO.getContent()+"\"";

            }
            else  {
                postWriterNotificationMessage = new NotificationMessageDto("study/detail/" + findPosts.getId() + ": 스터디 게시물 : \"" + findPosts.getTitle() + "\"에 \"" + findUser.getNickName() + "\"님이 댓글을 남겼습니다.\"" + commentRequestDTO.getContent()+"\""); // 실제 구현 완료되면, 여기가 아니라 notification으로 라우팅 걸어주자
                postWriterNotifyMessage = "스터디 게시물 : \"" + findPosts.getTitle() + "\"에 \"" + findUser.getNickName() + "\"님이 댓글을 남겼습니다.\n\"" + commentRequestDTO.getContent()+"\"";

            }

            Notifications postWriterCommentNotification = Notifications.builder()
                .user(findPosts.getUser()) // 게시물 작성자에게 보내기
                .postId(findPosts.getId())
                .notificationMessage(postWriterNotifyMessage)
                .postType(findPosts.getPostType())
                .checked(false)
                .build();

            Notifications savedPostWriterCommentNotification = notificationsRepository.save(postWriterCommentNotification);
            postWriterNotificationMessage.setMessage(postWriterNotificationMessage.getMessage() + savedPostWriterCommentNotification.getId().toString());
            notificationService.notify(findPosts.getUser().getId(), postWriterNotificationMessage); // 부모 대댓글 작성자에게 실시간 알림 전송
            }




    }

    // 댓글, 답글 조회 in 프로젝트 / 스터디 게시물
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsForPost(Long postId, String userEmail, Pageable pageable) {

        QComments comments = QComments.comments;

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        String url = currentUser.getImageUrl();

        JPAQuery<Comments> query = queryFactory.selectFrom(comments) // 댓글을 조회할건데,
                .leftJoin(comments.parent).fetchJoin()  // 부모 댓글도 같이 조회할거야
                .where(comments.posts.id.eq(postId)) // 근데 그 댓글은 이 프로젝트 게시물에 해당하는거고
                .orderBy(comments.parent.id.asc().nullsFirst(), // 부모가 없는 애들이 정렬의 우선순위가 높아
                        comments.createdDate.asc()); // 그리고, 시간이 빠른순으로 정렬


        List<Comments> findComments = query.fetch();

        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>(); // 최종적으로 frontEnd에 나갈 부모자식 댓글 관계 배열
        Map<Long, CommentResponseDto> commentDtoHashMap = new HashMap<>(); // 일차적으로 부모와 자식들을 이어주기 위한 map자료

        findComments.forEach(c -> {
            CommentResponseDto commentResponseDto;

            if (c.getIsDeleted()) { // 댓글의 isDeleted가 true라면

                if(c.getParent()!=null) // 해당 댓글에 부모가 있다면
                {
                    commentResponseDto = CommentResponseDto.builder()
                            .id(c.getId())
                            .content("삭제된 댓글입니다.")
                            .nickName(null)
                            .userId(null)
                            .commentWriter(false)
                            .finalCommentedTime(c.getLastModifiedDate())
                            .isTopLevel(false)
                            .imageUrl("noResult.png")
                            .build();
                }

                else { // 해당 댓글에 부모가 없다면
                    commentResponseDto = CommentResponseDto.builder()
                            .id(c.getId())
                            .content("삭제된 댓글입니다.")
                            .nickName(null)
                            .userId(null)
                            .commentWriter(false)
                            .finalCommentedTime(c.getLastModifiedDate())
                            .isTopLevel(true)
                            .imageUrl("noResult.png")
                            .build();
                }


            } else { // 해당 댓글의 isDeleted가 false라면

                boolean isCommentWriter = currentUser.getId().equals(c.getUser().getId()); // 해당 댓글을 쓴 user의 id를 식별

                if(c.getParent() != null) // 해당 댓글의 부모가 있다면
                {
                    commentResponseDto = CommentResponseDto.builder()
                            .id(c.getId())
                            .content(c.getContent())
                            .nickName(c.getUser().getNickName())
                            .userId(c.getUser().getId())
                            .commentWriter(isCommentWriter)
                            .finalCommentedTime(c.getLastModifiedDate())
                            .isTopLevel(false)
                            .imageUrl(c.getUser().getImageUrl())
                            .build();
                }

                else { // 해당 댓글의 부모가 없다면
                    commentResponseDto = CommentResponseDto.builder()
                            .id(c.getId())
                            .content(c.getContent())
                            .nickName(c.getUser().getNickName())
                            .userId(c.getUser().getId())
                            .commentWriter(isCommentWriter)
                            .finalCommentedTime(c.getLastModifiedDate())
                            .isTopLevel(true)
                            .imageUrl(c.getUser().getImageUrl())
                            .build();
                }

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

        // 변환한 데이터의 총 개수 계산
        long total = commentResponseDtoList.size();

        // 페이지네이션을 위한 offset과 pageSize 계산, 예외 상황 처리
        int offset = (int) pageable.getOffset();    // 현재 페이지 정보 가져오기
        int pageSize = pageable.getPageSize();      // 현재 페이지에 몇 개를 띄울건지 size 정보 가져오기

        // offset이 데이터의 총 개수를 초과하면 더 이상 데이터를 가져올 필요 없음
        if (offset >= total) {
            offset = 0;
            pageSize = 0;
        }
        // 현재 페이지 + 현재 페이지에서 보고있는 게시물의 개수 > 총 게시물의 개수라면, 페이지 사이즈 재조정
        else if (offset + pageSize > total) {
            pageSize = (int) (total - offset);
        }
        //return commentResponseDtoList;
        Page<CommentResponseDto> page = new PageImpl<>(commentResponseDtoList.subList(offset, offset + pageSize), pageable, total);
        return page;
    }

    // 특정 댓글 또는 답글 삭제
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


    // 특정 댓글 또는 답글 업데이트
    public void updateComment(Long commentId, CommentRequestDto commentRequestDTO, String userEmail) {

        QComments comments = QComments.comments;

        Comments selectedComment = queryFactory.select(comments) // 댓글을 대상으로 할 건데
                .from(comments)
                .where(comments.id.eq(commentId)) // parameter로 받은 댓글 id가 같은 것만 가져올 것
                .fetchOne();

        Optional<Comments> optionalSelectedComment = Optional.ofNullable(selectedComment);

        Comments comment = optionalSelectedComment.orElseThrow(
                () -> new AppException("Could not find comment id: " + commentId, HttpStatus.BAD_REQUEST));


        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));


        if (currentUser.getId().equals(comment.getUser().getId())) {
            comment.updateContent(commentRequestDTO.getContent()); // 댓글 내용 업데이트
            commentsRepository.save(comment); // 댓글 저장
        } else {
            throw new AppException("댓글 작성자만 업데이트할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

    }
}
