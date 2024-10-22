package nbc_final.gathering.domain.comment.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentSaveResponse {
    private final Long id;
    private final String content;
    private final Long userId;
    private final LocalDateTime createdAt;

    public CommentSaveResponse(Long id, String content, Long userId, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
    }
}
