package nbc_final.gathering.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.comment.dto.request.CommentRequestDto;
import nbc_final.gathering.domain.comment.dto.response.CommentResponseDto;
import nbc_final.gathering.domain.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Comment API", description = "이벤트 내의 댓글 관련 API 모음입니다.")
public class CommentController {
    private final CommentService commentService;

    /**
     * 새로운 댓글을 저장
     *
     * @param commentRequestDto 저장할 댓글의 정보를 담은 DTO
     * @param gatheringId       모임의 ID
     * @param eventId           이벤트의 ID
     * @param authUser          인증된 사용자의 정보
     * @return 저장된 댓글의 정보를 포함한 성공 응답
     */
    @Operation(summary = "댓글 작성", description = "참여 중인 이벤트에 댓글을 작성합니다.")
    @PostMapping("/v1/gatherings/{gatheringId}/events/{eventId}/comments")
    public ResponseEntity<ApiResponse<CommentResponseDto>> saveComment(
            @RequestBody CommentRequestDto commentRequestDto,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        CommentResponseDto commentResponseDto = commentService.saveComment(commentRequestDto, gatheringId, userId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(commentResponseDto));
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
    @Operation(summary = "댓글 수정", description = "본인이 생성한 댓글을 수정합니다.")
    @PutMapping("/v1/gatherings/{gatheringId}/events/{eventId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @RequestBody CommentRequestDto commentUpdateRequestDto,
            @PathVariable Long commentId,
            @PathVariable Long gatheringId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthUser authUser) {
        Long userId = authUser.getUserId();  // 인증된 사용자의 ID 사용
        CommentResponseDto commentUpdateResponseDto = commentService.updateComment(commentUpdateRequestDto, commentId, userId, gatheringId, eventId);
        return ResponseEntity.ok(ApiResponse.createSuccess(commentUpdateResponseDto));
    }

    /**
     * 기존 댓글을 삭제
     *
     * @param commentId   삭제할 댓글의 ID
     * @param gatheringId 모임의 ID
     * @param eventId     이벤트의 ID
     * @param authUser    인증된 사용자의 정보
     * @return 삭제 성공을 나타내는 응답
     */
    @Operation(summary = "댓글 삭제", description = "본인이 기존에 작성했던 댓글을 삭제합니다.")
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

