package nbc_final.gathering.domain.comment.repository;

import nbc_final.gathering.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository <Comment, Long> {
    List<Comment> findByGroupId(Long groupId);
}
