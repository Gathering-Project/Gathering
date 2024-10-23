package nbc_final.gathering.domain.comment.dto.response;

import lombok.Getter;
import nbc_final.gathering.domain.comment.entity.Comment;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    private final Long id;
    private final String content;
    private final Long userId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CommentResponseDto(Long id, String content, Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CommentResponseDto of(Comment comment) {
      return new CommentResponseDto(
              comment.getId(),
              comment.getContent(),
              comment.getUser().getId(),
              comment.getCreatedAt(),
              comment.getUpdatedAt()
      );
    }
}
