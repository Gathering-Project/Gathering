package nbc_final.gathering.domain.example.attachment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttachmentResponseDto {
    private Long id;
    private String profileImagePath;
    private Long userId;

    public AttachmentResponseDto(String fileUrl) {
    }

//    public AttachmentResponseDto(String attachment) {
//        this.id = attachment.getId();
//        this.profileImagePath = attachment.getProfileImagePath();
//        this.userId = attachment.getUser().getId();
//    }
}
