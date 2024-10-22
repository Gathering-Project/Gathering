package nbc_final.gathering.domain.example.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.example.attachment.entity.Attachment;
import nbc_final.gathering.domain.example.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GatheringAttachmentService {

    private final AmazonS3 amazonS3;
    private final AttachmentRepository attachmentRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 지원되는 파일 형식과 크기 제한
    private static final List<String> SUPPORTED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // 모임 프로필 등록
    @Transactional
    public String gatheringUploadFile(AuthUser authUser, Long gatheringId, MultipartFile file) throws IOException, java.io.IOException {
        validateFile(file);

        // Gathering 객체를 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));

        // S3에 파일 업로드 후 URL 반환
        String fileUrl = uploadToS3(file);

        // Gathering 엔티티의 gatheringImage 필드를 업데이트
        gathering.setGatheringImage(fileUrl);

        gatheringRepository.save(gathering);

        saveGatheringAttachment(authUser, gathering, fileUrl);
        return fileUrl;
    }

    // 모임 이미지 수정
    @Transactional
    public String gatheringUpdateFile (AuthUser authUser, Long gatheringId, MultipartFile file) throws IOException, java.io.IOException {
        validateFile(file);

        // Gathering 객체를 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));

        Attachment existingAttachment = attachmentRepository.findByUserAndGathering(authUser, gathering);
        if (existingAttachment != null) {
            deleteFromS3(existingAttachment.getProfileImagePath());
        }

        return gatheringUploadFile(authUser,gatheringId, file);
    }

    // 모임 이미지 삭제
    @Transactional
    public void gatheringDeleteFile(AuthUser authUser, Long gatheringId) {
        // gatheringId를 사용하여 Gathering 엔티티를 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));

        // 기존 파일 찾기
        Attachment existingAttachment = attachmentRepository.findByUserAndGathering(authUser, gathering);
        if (existingAttachment != null) {
            deleteFromS3(existingAttachment.getProfileImagePath());
            attachmentRepository.delete(existingAttachment);
        }
    }

    // 유저 이미지 예외처리
    private void validateFile(MultipartFile file) {
        if (!SUPPORTED_FILE_TYPES.contains(file.getContentType())) {
            throw new ResponseCodeException(ResponseCode.NOT_SERVICE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseCodeException(ResponseCode.TOO_LARGE_SIZE_FILE);
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

    private void saveGatheringAttachment(AuthUser authUser, Gathering gathering, String fileUrl) {

        // gathering이 제대로 전달되는지 확인
        if (gathering == null) {
            throw new IllegalArgumentException("Gathering cannot be null");
        }

        // User 엔티티 조회
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + authUser.getUserId()));

        if (user == null || gathering == null) {
            throw new IllegalArgumentException("User or Gathering cannot be null.");
        }

        Attachment attachment = new Attachment();
        attachment.setUser(user);  // AuthUser에서 userId 설정
        attachment.setProfileImagePath(fileUrl);
        attachment.setGathering(gathering);
        attachmentRepository.save(attachment);
    }
}
