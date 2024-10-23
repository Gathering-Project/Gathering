package nbc_final.gathering.domain.example.attachment.repository;

import nbc_final.gathering.domain.example.attachment.entity.Attachment;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
   List<Attachment> findByUser(User user);
   List<Attachment> findByUserAndGathering(User user, Gathering gathering);
}