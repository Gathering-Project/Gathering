package nbc_final.gathering.domain.example.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.example.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.example.attachment.entity.Attachment;
import nbc_final.gathering.domain.example.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class AttachmentService {

    private final AmazonS3 amazonS3;
    private final String bucketName;
    private final AttachmentRepository attachmentRepository;


    // 지원되는 파일 형식과 크기 제한
    private static final List<String> SUPPORTED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String uploadFile (AuthUser authUser ,MultipartFile file) throws IOException, java.io.IOException {
        validateFile(file);

        String fileName = file.getOriginalFilename();
        String fileUrl = "http://" + bucketName + "/profile-images/" + fileName;

        ObjectMatadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);

        Attachment attachment = new Attachment(fileUrl, authUser);
        attachmentRepository.save(attachment);

        return fileUrl;
    }

    public String updateFile (MultipartFile file, User user) throws IOException {
        validateFile(file);

        Attachment existingAttachment = attachmentRepository.findByUser(user);
        if (existingAttachment != null) {
            deleteFile(existingAttachment.getProfileImagePath());
        }

        return uploadFile(file, user);
    }

    public void deleteFile(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        amazonS3.deleteObject(bucketName, fileName);
    }



    private void validateFile(MultipartFile file) {
        if (!SUPPORTED_FILE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(("파일 크기가 너무 큽니다."));
        }
    }


}