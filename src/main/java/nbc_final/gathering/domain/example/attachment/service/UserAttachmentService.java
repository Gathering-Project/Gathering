package nbc_final.gathering.domain.example.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.example.attachment.entity.Attachment;
import nbc_final.gathering.domain.example.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserAttachmentService {

    private final AmazonS3 amazonS3;
    private final AttachmentRepository attachmentRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 지원되는 파일 형식과 크기 제한
    private static final List<String> SUPPORTED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // 유저 프로필 등록
    @Transactional
    public String userUploadFile(AuthUser user, MultipartFile file) throws IOException, java.io.IOException {
        validateFile(file);
        String fileUrl = uploadToS3(file);
        saveAttachment(user, fileUrl);
        return fileUrl;
    }

    // 유저 이미지 수정
    @Transactional
    public String userUpdateFile (MultipartFile file, AuthUser user) throws IOException, java.io.IOException {
        validateFile(file);

        Attachment existingAttachment = attachmentRepository.findByUser(user);
        if (existingAttachment != null) {
            deleteFromS3(existingAttachment.getProfileImagePath());
        }

        return userUploadFile(user, file);
    }

    // 유저 이미지 삭제
    @Transactional
    public void userDeleteFile(AuthUser authUser) {
        // 기존 파일 찾기
        Attachment existingAttachment = attachmentRepository.findByUser(authUser);
        if (existingAttachment != null) {
            deleteFromS3(existingAttachment.getProfileImagePath());
            attachmentRepository.delete(existingAttachment);
        }
    }

    // 유저 이미지 예외처리
    private void validateFile(MultipartFile file) {
        if (!SUPPORTED_FILE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(("파일 크기가 너무 큽니다."));
        }
    }

    // 유저 삭제 메서드
    private void deleteFromS3(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        amazonS3.deleteObject(bucketName, fileName);
    }

    private String uploadToS3(MultipartFile file) throws IOException, java.io.IOException {
        String fileName = file.getOriginalFilename();
        String fileUrl = "https://" + bucketName + "/profile-images/" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);
        return fileUrl;
    }

    private void saveAttachment(AuthUser user, String fileUrl) {
        Attachment attachment = new Attachment(user, fileUrl);
        attachment.setUser(user.getUserId());  // AuthUser에서 실제 User 객체를 가져와 설정
        attachment.setProfileImagePath(fileUrl);  // 파일 URL 설정
        attachmentRepository.save(attachment);
    }


}


