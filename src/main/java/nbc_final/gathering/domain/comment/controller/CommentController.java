package nbc_final.gathering.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.comment.dto.request.CommentSaveRequest;
import nbc_final.gathering.domain.comment.dto.request.CommentUpdateRequest;
import nbc_final.gathering.domain.comment.dto.response.CommentSaveResponse;
import nbc_final.gathering.domain.comment.dto.response.CommentUpdateResponse;
import nbc_final.gathering.domain.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/groups/{groupId}/events/{eventId}/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    //댓글 작성
    @PostMapping
    public ResponseEntity<CommentSaveResponse> saveComment(@RequestBody CommentSaveRequest commentSaveRequest, @PathVariable Long groupId, @PathVariable Long eventId, @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        CommentSaveResponse commentSaveResponse = commentService.saveComment(commentSaveRequest,groupId,userId,eventId);
        return ResponseEntity.ok(commentSaveResponse);
    }

    //댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentUpdateResponse> updateComment(@RequestBody CommentUpdateRequest commentUpdateRequest, @PathVariable Long commentId, @PathVariable Long groupId, @PathVariable Long eventId, @AuthenticationPrincipal AuthUser authUser){
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        CommentUpdateResponse response = commentService.updateComment(commentUpdateRequest,commentId,userId,groupId,eventId);
        return ResponseEntity.ok(response);
    }

    //댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,  @PathVariable Long groupId, @PathVariable Long eventId, @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        commentService.deleteComment(commentId,userId,groupId,eventId);
        return ResponseEntity.noContent().build();
    }

}

