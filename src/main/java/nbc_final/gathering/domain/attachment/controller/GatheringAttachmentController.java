package nbc_final.gathering.domain.attachment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.attachment.service.GatheringAttachmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Gathering-Attachment API", description = "소모임 이미지 첨부 관련 API 모음입니다.")
public class GatheringAttachmentController {

    private final GatheringAttachmentService attachmentService;

    /**
     * 모임 프로필 이미지 업로드
     *
     * @param authUser    인증된 사용자 정보
     * @param gatheringId 모임 ID
     * @param file        업로드할 파일(requestpart로 전달)
     * @return 업로드된 파일 정보가 담긴 응답 객체
     * @throws IOException 파일 처리 중 예외 발생 시
     */
    @Operation(summary = "소모임 프로필 이미지 업로드", description = "소모임 프로필의 이미지를 업로드합니다.")
    @PostMapping("/v1/gatherings/{gatheringId}/upload/userFile")
    public ResponseEntity<?> gatheringUploadFile(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        AttachmentResponseDto responseDto = attachmentService.gatheringUploadFile(authUser, gatheringId, file);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    /**
     * 모임 프로필 이미지 수정
     *
     * @param authUser    인증된 사용자 정보
     * @param gatheringId 모임 ID
     * @param file        수정할 파일(requestpart로 전달)
     * @return 수정된 파일 정보가 담긴 응답 객체
     * @throws IOException 파일 처리 중 예외 발생 시
     */
    @Operation(summary = "소모임 프로필 이미지 수정", description = "소모임의 프로필의 이미지를 수정합니다.")
    @PutMapping("/v1/gatherings/{gatheringId}/uploadUpdate/userFile")
    public ResponseEntity<?> gatheringUpdateFile(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        AttachmentResponseDto responseDto = attachmentService.gatheringUpdateFile(authUser, gatheringId, file);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    /**
     * 모임 프로필 이미지 삭제
     *
     * @param authUser    인증된 사용자 정보
     * @param gatheringId 모임 ID
     * @return 응답 없음 (HTTP 204 No Content)
     */
    @Operation(summary = "소모임 프로필 이미지 삭제", description = "소모임의 프로필 이미지를 삭제합니다.")
    @DeleteMapping("/v1/gatherings/{gatheringId}/delete/userFile")
    public ResponseEntity<?> gatheringDeleteFile(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId
    ) {
        attachmentService.gatheringDeleteFile(authUser, gatheringId);
        return ResponseEntity.noContent().build();
    }
}


