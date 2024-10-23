package nbc_final.gathering.domain.comment.dto.response;

import lombok.Getter;
import nbc_final.gathering.domain.comment.entity.Comment;
import nbc_final.gathering.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
public class CommentSaveResponseDto {
    private final Long id;
    private final String content;
    private final Long userId;
    private final LocalDateTime createdAt;

    public CommentSaveResponseDto(Long id, String content, Long userId, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static CommentSaveResponseDto of(Comment comment) {
      return new CommentSaveResponseDto(
              comment.getId(),
              comment.getContent(),
              comment.getUser().getId(),
              comment.getCreatedAt()
      );
    }
}
