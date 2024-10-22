package nbc_final.gathering.domain.comment.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentUpdateResponseDto {
    private final Long id;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentUpdateResponseDto(Long id, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
