package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.CommentRequestDto;
import PickMe.PickMeDemo.dto.CommentResponseDto;
import PickMe.PickMeDemo.service.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentsController {


    private final CommentsService commentsService;

    // 댓글 또는 답글 등록 in 프로젝트 게시물
    // 첫 댓글(부모)는 CommentRequestDto의 parentId가 null인 상태로 세팅돼서 들어옴
    // 답글(자식)은 CommentRequestDto의의 parentId가 들어있는 상태로 세팅돼서 들어옴
    @PostMapping("/registerComments/{projectId}")
    public ResponseEntity<String> registerComments(@PathVariable Long projectId, @RequestBody CommentRequestDto commentRequestDto, Principal principal) {

        String userEmail = principal.getName(); // 해당 유저 찾기

        commentsService.registerComment(projectId, commentRequestDto, userEmail);

        return ResponseEntity.ok("Comment has been successfully uploaded.");
    }

    // 특정 프로젝트 게시물의 댓글 답글 조회
    @GetMapping("/getCommentData")
    public ResponseEntity<Page<CommentResponseDto>> getCommentsForProject(
            @RequestParam(name = "projectId") Long projectId,
            @RequestParam(name = "page", defaultValue = "0") int page, // 프론트엔드에서 넘어온 선택된 페이지
            @RequestParam(name = "size", defaultValue = "3") int size, //프론트엔드에서 넘어온 한 페이지당 가져올 컨텐츠 수
            Principal principal) {

        String userEmail = principal.getName();
        Pageable pageable = PageRequest.of(page, size);

        // 부모, 자식까지 한번에 조회
        Page<CommentResponseDto> comments = commentsService.getCommentsForProject(projectId,userEmail, pageable);

        return ResponseEntity.ok(comments);
    }

    // 특정 프로젝트 게시물의 댓글 삭제
    // 자식이 있는 부모 댓글의 경우 -> 삭제하면 '삭제된 댓글입니다'로 바뀌고, 자식 댓글들은 살려놓음
    // 자식이 다 삭제된 경우, 부모 댓글도 삭제시킴
    @PostMapping("/deleteComments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, Principal principal) {
        String userEmail = principal.getName(); // 현재 사용자 이메일 가져오기
        commentsService.deleteComment(commentId, userEmail);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }

    @PutMapping("/updateComments/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable Long commentId, @RequestBody CommentRequestDto commentRequestDto, Principal principal) {
        String userEmail = principal.getName(); // 현재 사용자 이메일 가져오기
        commentsService.updateComment(commentId, commentRequestDto, userEmail);
        return ResponseEntity.ok("댓글이 수정되었습니다.");
    }
}
