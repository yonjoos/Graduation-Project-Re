package PickMe.PickMeDemo.controller;

import PickMe.PickMeDemo.dto.CommentRequestDto;
import PickMe.PickMeDemo.dto.PostsFormDto;
import PickMe.PickMeDemo.entity.Comments;
import PickMe.PickMeDemo.entity.PostType;
import PickMe.PickMeDemo.repository.CommentsRepository;
import PickMe.PickMeDemo.service.CommentsService;
import PickMe.PickMeDemo.service.PostsService;
import PickMe.PickMeDemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class CommentsController {

    private final UserService userService;
    private final PostsService postsService;
    private final CommentsService commentsService;

    @PostMapping("/registerComments")
    public ResponseEntity<String> registerComments(@PathVariable Long postId, @RequestBody @Valid CommentRequestDto commentRequestDto, Principal principal) {

        String userEmail = principal.getName(); // 해당 유저 찾기

        commentsService.registerComment(postId, commentRequestDto, userEmail);

        return ResponseEntity.ok("Comment has been successfully uploaded.");
    }
}
