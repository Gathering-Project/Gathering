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


@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    /**
     * 새로운 댓글을 저장
     *
     * @param commentSaveRequestDto 저장할 댓글의 정보를 담은 DTO
     * @param gatheringId            모임의 ID
     * @param eventId                이벤트의 ID
     * @param authUser               인증된 사용자의 정보
     * @return 저장된 댓글의 정보를 포함한 성공 응답
     */
    @PostMapping("/v1/gatherings/{gatheringId}/events/{eventId}/comments")
    public ResponseEntity<ApiResponse<CommentSaveResponseDto>> saveComment(
            @RequestBody CommentSaveRequestDto commentSaveRequestDto,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        CommentSaveResponseDto commentSaveResponseDto = commentService.saveComment(commentSaveRequestDto, gatheringId, userId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(commentSaveResponseDto));
    }

    /**
     * 기존 댓글을 수정
     *
     * @param commentUpdateRequestDto 수정할 댓글의 정보를 담은 DTO
     * @param commentId               수정할 댓글의 ID
     * @param gatheringId             모임의 ID
     * @param eventId                 이벤트의 ID
     * @param authUser                인증된 사용자의 정보
     * @return 수정된 댓글의 정보를 포함한 성공 응답
     */
    @PutMapping("/v1/gatherings/{gatheringId}/events/{eventId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentUpdateResponseDto>> updateComment(
            @RequestBody CommentUpdateRequestDto commentUpdateRequestDto,
            @PathVariable Long commentId,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        CommentUpdateResponseDto commentUpdateResponseDto = commentService.updateComment(commentUpdateRequestDto, commentId, userId, gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(commentUpdateResponseDto));
    }

    /**
     * 기존 댓글을 삭제
     *
     * @param commentId    삭제할 댓글의 ID
     * @param gatheringId  모임의 ID
     * @param eventId      이벤트의 ID
     * @param authUser     인증된 사용자의 정보
     * @return 삭제 성공을 나타내는 응답
     */
    @DeleteMapping("/v1/gatherings/{gatheringId}/events/{eventId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        commentService.deleteComment(commentId, userId, gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }

}

