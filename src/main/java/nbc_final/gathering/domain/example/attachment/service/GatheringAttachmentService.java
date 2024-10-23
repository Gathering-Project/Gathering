package nbc_final.gathering.domain.example.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ApiResponse;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.example.attachment.dto.AttachmentResponseDto;
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
    public AttachmentResponseDto gatheringUploadFile(AuthUser authUser, Long gatheringId, MultipartFile file) throws IOException, java.io.IOException {
        validateFile(file);

        // Gathering 객체를 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        // S3에 파일 업로드 후 URL 반환
        String fileUrl = uploadToS3(file);

        // Gathering 엔티티의 gatheringImage 필드를 업데이트
        gathering.setGatheringImage(fileUrl);

        gatheringRepository.save(gathering);

        Attachment attachment = saveGatheringAttachment(authUser, gathering, fileUrl);
        return new AttachmentResponseDto(attachment);
    }

    // 모임 이미지 수정
    @Transactional
    public AttachmentResponseDto gatheringUpdateFile (AuthUser authUser, Long gatheringId, MultipartFile file) throws IOException, java.io.IOException {
        validateFile(file);

        // Gathering 객체를 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        Attachment existingAttachment = attachmentRepository.findByUserAndGathering(authUser, gathering);
        if (existingAttachment != null) {
            deleteFromS3(existingAttachment.getProfileImagePath());
        }
        AttachmentResponseDto responseDto = gatheringUploadFile(authUser, gatheringId, file);
        return responseDto;
    }

    // 모임 이미지 삭제
    @Transactional
    public void gatheringDeleteFile(AuthUser authUser, Long gatheringId) {
        // gatheringId를 사용하여 Gathering 엔티티를 조회
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        // 기존 파일 찾기
        Attachment existingAttachment = attachmentRepository.findByUserAndGathering(authUser, gathering);
        if (existingAttachment != null) {
            deleteFromS3(existingAttachment.getProfileImagePath());
            attachmentRepository.delete(existingAttachment);
        }
    }

    // 이미지 예외처리
    private void validateFile(MultipartFile file) {
        if (!SUPPORTED_FILE_TYPES.contains(file.getContentType())) {
            throw new ResponseCodeException(ResponseCode.NOT_SERVICE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseCodeException(ResponseCode.TOO_LARGE_SIZE_FILE);
        }
    }

    // 삭제 메서드
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

    // 모임 첨부파일 저장 메서드
    private Attachment saveGatheringAttachment(AuthUser authUser, Gathering gathering, String fileUrl) {

        // gathering이 제대로 전달되는지 확인
        if (gathering == null) {
            throw new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING);
        }

        // User 엔티티 조회
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        if (user == null || gathering == null) {
            throw new ResponseCodeException(ResponseCode.NOT_USER_OR_GATHERING);
        }

        Attachment attachment = new Attachment();
        attachment.setUser(user);
        attachment.setProfileImagePath(fileUrl);
        attachment.setGathering(gathering);
        attachmentRepository.save(attachment);
        return attachment;
    }
}