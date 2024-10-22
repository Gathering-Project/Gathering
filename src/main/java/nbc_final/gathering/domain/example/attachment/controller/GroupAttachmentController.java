//package nbc_final.gathering.domain.example.attachment.controller;
//
//import lombok.RequiredArgsConstructor;
//import nbc_final.gathering.common.dto.AuthUser;
//import nbc_final.gathering.domain.example.attachment.dto.AttachmentResponseDto;
//import nbc_final.gathering.domain.example.attachment.repository.AttachmentRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/v1")
//@RequiredArgsConstructor
//public class GroupAttachmentController {
//
//        private final GroupAttachmentController attachmentService;
//        private final AttachmentRepository attachmentRepository;
//
//
//        @PostMapping("/groups/upload/userFile")
//        public ResponseEntity<AttachmentResponseDto> userUploadFile(
//                @AuthenticationPrincipal AuthUser authUser,
//                @PathVariable
//                @RequestPart("file") MultipartFile file
//        ) {
//            String fileUrl = attachmentService.userUploadFile(authUser, groupId, file);
//            AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
//            return ResponseEntity.ok(responseDto);
//        }
//
//        @PutMapping("/groups/uploadUpdate/userFile")
//        public ResponseEntity<AttachmentResponseDto> userUpdateFile(
//                @AuthenticationPrincipal AuthUser authUser,
//                @PathVariable groupId,
//                @RequestPart("file") MultipartFile file
//        ) {
//            String fileUrl = attachmentService.userUpdateFile(authUser, groupId, file);
//            AttachmentResponseDto responseDto = new AttachmentResponseDto(fileUrl);
//            return ResponseEntity.ok(responseDto);
//        }
//
//        @DeleteMapping("/groups/delete/userFile")
//        public ResponseEntity<?> userDeleteFile(
//                @AuthenticationPrincipal AuthUser authUser
//        ) {
//            attachmentRepository.findByUser(authUser);
//            return ResponseEntity.noContent().build();
//        }
//
//    }
//
//
//
//}
