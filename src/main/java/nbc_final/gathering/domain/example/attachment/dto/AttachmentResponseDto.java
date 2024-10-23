package nbc_final.gathering.domain.example.attachment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc_final.gathering.domain.example.attachment.entity.Attachment;

@Getter
@AllArgsConstructor
public class AttachmentResponseDto {
    private Long id;
    private String profileImagePath;
    private Long userId;
    
    public AttachmentResponseDto(Attachment attachment) {
        this.id = attachment.getId();
        this.profileImagePath = attachment.getProfileImagePath();
        this.userId = attachment.getUser().getId();
    }
}
