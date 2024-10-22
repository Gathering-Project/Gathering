package nbc_final.gathering.domain.comment.dto.request;

import jakarta.persistence.GeneratedValue;
import lombok.Getter;

@Getter
public class CommentSaveRequest {
    private String content;
}
