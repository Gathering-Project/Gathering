package nbc_final.gathering.domain.example.attachment.controller;


import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.example.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.example.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.example.attachment.service.UserAttachmentService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/api/vi")

public class UserAttachmentController {

    private final UserAttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;

    public UserAttachmentController(@Lazy UserAttachmentService attachmentService, AttachmentRepository attachmentRepository) {
        this.attachmentService= attachmentService;
        this.attachmentRepository = attachmentRepository;
    }


    @PostMapping("/users/upload/userFile")
    public ResponseEntity<?> userUploadFile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String fileUrl = String.valueOf(attachmentService.userUploadFile(authUser, file));
        AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    @PutMapping("/users/uploadUpdate/userFile")
    public ResponseEntity<?> userUpdateFile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String fileUrl = String.valueOf(attachmentService.userUpdateFile(file, authUser));
        AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    @DeleteMapping("/users/delete/userFile")
    public ResponseEntity<?> userDeleteFile(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        attachmentRepository.findByUser(authUser);
        return ResponseEntity.noContent().build();
    }

}


