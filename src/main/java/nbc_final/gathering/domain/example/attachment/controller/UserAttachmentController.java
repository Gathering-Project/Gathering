package nbc_final.gathering.domain.example.attachment.controller;


import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.example.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.example.attachment.service.UserAttachmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserAttachmentController {

    private final UserAttachmentService attachmentService;

    /**
     * 사용자 프로필 이미지를 업로드
     *
     * @param authUser 인증된 사용자 정보
     * @param file 업로드할 이미지 파일 (RequestPart로 전달)
     * @return 업로드된 파일의 URL을 포함한 응답
     * @throws IOException 파일 처리 중 발생할 수 있는 예외
     */
    @PostMapping("/users/upload/userFile")
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
     * @param file 수정할 이미지 파일 (RequestPart로 전달)
     * @return 수정된 파일의 URL을 포함한 응답
     * @throws IOException 파일 처리 중 발생할 수 있는 예외
     */
    @PutMapping("/users/uploadUpdate/userFile")
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
    @DeleteMapping("/users/delete/userFile")
    public ResponseEntity<?> userDeleteFile(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        attachmentService.userDeleteFile(authUser);  // 파일 삭제 메서드 호출
        return ResponseEntity.noContent().build();
    }
}

