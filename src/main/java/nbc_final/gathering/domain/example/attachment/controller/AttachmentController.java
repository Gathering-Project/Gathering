package nbc_final.gathering.domain.example.attachment.controller;


import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.example.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.example.attachment.entity.Attachment;
import nbc_final.gathering.domain.example.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.example.attachment.service.AttachmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vi")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;


    @PostMapping("/users/upload/userFile")
    public ResponseEntity<AttachmentResponseDto> uploadFile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) {
        String fileUrl = attachmentService.uploadFile(authUser, file);
        AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/users/uploadUpdate/userFile")
    public ResponseEntity<AttachmentResponseDto> updateFile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) {
        String fileUrl = attachmentService.updateFile(authUser, file);
        AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/users/delete/userFile")
    public ResponseEntity<?> deleteFile(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        attachmentRepository.findByUser(authUser, file);
        return ResponseEntity.noContent().build();
    }

}


