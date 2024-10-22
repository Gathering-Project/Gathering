package nbc_final.gathering.domain.example.attachment.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.domain.example.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.example.attachment.service.GatheringAttachmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GatheringAttachmentController {

    private final GatheringAttachmentService attachmentService;

    @PostMapping(value = "/gatherings/{gatheringId}/upload/userFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> gatheringUploadFile(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String fileUrl = attachmentService.gatheringUploadFile(authUser, gatheringId, file);
        AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    @PutMapping("/gatherings/{gatheringId}/uploadUpdate/userFile")
    public ResponseEntity<?> gatheringUpdateFile(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String fileUrl = attachmentService.gatheringUpdateFile(authUser, gatheringId, file);
        AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
        return ResponseEntity.ok(ApiResponse.createSuccess(responseDto));
    }

    @DeleteMapping("/gatherings/{gatheringId}/delete/userFile")
    public ResponseEntity<?> gatheringDeleteFile(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long gatheringId
    ) {
        attachmentService.gatheringDeleteFile(authUser, gatheringId);
        return ResponseEntity.noContent().build();
    }
}

