package nbc_final.gathering.domain.example.attachment.repository;

import nbc_final.gathering.domain.example.attachment.entity.Attachment;
import nbc_final.gathering.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Attachment findByUser(User user);
}