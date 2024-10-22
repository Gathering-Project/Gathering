package nbc_final.gathering.domain.example.attachment.controller;


import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.example.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.example.attachment.repository.AttachmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/vi")
@RequiredArgsConstructor
public class UserAttachmentController {

    private final UserAttachmentController attachmentService;
    private final AttachmentRepository attachmentRepository;


    @PostMapping("/users/upload/userFile")
    public ResponseEntity<?> userUploadFile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) {
        String fileUrl = String.valueOf(attachmentService.userUploadFile(authUser, file));
        AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/users/uploadUpdate/userFile")
    public ResponseEntity<?> userUpdateFile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) {
        String fileUrl = String.valueOf(attachmentService.userUpdateFile(authUser, file));
        AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/users/delete/userFile")
    public ResponseEntity<?> userDeleteFile(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        attachmentRepository.findByUser(authUser);
        return ResponseEntity.noContent().build();
    }

}


