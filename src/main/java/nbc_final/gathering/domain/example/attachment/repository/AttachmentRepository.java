package nbc_final.gathering.domain.example.attachment.repository;

import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.example.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Attachment findByUser(AuthUser user);
    Attachment findByUserAndGroup(AuthUser user, Long groupId);
}