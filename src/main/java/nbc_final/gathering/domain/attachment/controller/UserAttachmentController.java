package nbc_final.gathering.domain.attachment.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.attachment.service.UserAttachmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "User-Attachment API", description = "사용자 이미지 첨부 관련 API 모음입니다.")
public class UserAttachmentController {

    private final UserAttachmentService attachmentService;

    /**
     * 사용자 프로필 이미지를 업로드
     *
     * @param authUser 인증된 사용자 정보
     * @param file     업로드할 이미지 파일 (RequestPart로 전달)
     * @return 업로드된 파일의 URL을 포함한 응답
     * @throws IOException 파일 처리 중 발생할 수 있는 예외
     */
    @Operation(summary = "사용자 프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드합니다.")
    @PostMapping("/v1/users/upload/userFile")
    public ResponseEntity<?> userUploadFile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        AttachmentResponseDto responseDto = attachmentService.userUploadFile(authUser, file);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    /**
     * 사용자 프로필 이미지를 수정
     *
     * @param authUser 인증된 사용자 정보
     * @param file     수정할 이미지 파일 (RequestPart로 전달)
     * @return 수정된 파일의 URL을 포함한 응답
     * @throws IOException 파일 처리 중 발생할 수 있는 예외
     */
    @Operation(summary = "사용자 프로필 이미지 수정", description = "사용자의 프로필 이미지를 수정합니다.")
    @PutMapping("/v1/users/uploadUpdate/userFile")
    public ResponseEntity<?> userUpdateFile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        AttachmentResponseDto responseDto = attachmentService.userUpdateFile(authUser, file);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    /**
     * 사용자 프로필 이미지를 삭제
     *
     * @param authUser 인증된 사용자 정보
     * @return 응답 없이 No Content 상태 반환
     */
    @Operation(summary = "사용자 프로필 이미지 삭제", description = "사용자의 프로필 이미지를 삭제합니다.")
    @DeleteMapping("/v1/users/delete/userFile")
    public ResponseEntity<?> userDeleteFile(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        attachmentService.userDeleteFile(authUser);  // 파일 삭제 메서드 호출
        return ResponseEntity.noContent().build();
    }
}


