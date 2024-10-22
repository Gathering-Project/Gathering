package nbc_final.gathering.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.comment.dto.request.CommentSaveRequestDto;
import nbc_final.gathering.domain.comment.dto.request.CommentUpdateRequestDto;
import nbc_final.gathering.domain.comment.dto.response.CommentSaveResponseDto;
import nbc_final.gathering.domain.comment.dto.response.CommentUpdateResponseDto;
import nbc_final.gathering.domain.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/gatherings/{gatheringId}/events/{eventId}/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentSaveResponseDto>> saveComment(@RequestBody CommentSaveRequestDto commentSaveRequestDto, @PathVariable Long gatheringId, @PathVariable Long eventId, @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        CommentSaveResponseDto commentSaveResponseDto = commentService.saveComment(commentSaveRequestDto, gatheringId,userId,eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(commentSaveResponseDto));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentUpdateResponseDto>> updateComment(@RequestBody CommentUpdateRequestDto commentUpdateRequestDto, @PathVariable Long commentId, @PathVariable Long gatheringId, @PathVariable Long eventId, @AuthenticationPrincipal AuthUser authUser){
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        CommentUpdateResponseDto commentUpdateResponseDto = commentService.updateComment(commentUpdateRequestDto,commentId,userId, gatheringId,eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(commentUpdateResponseDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId, @PathVariable Long gatheringId, @PathVariable Long eventId, @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        commentService.deleteComment(commentId,userId, gatheringId,eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

}

