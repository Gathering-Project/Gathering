//package nbc_final.gathering.domain.example.attachment.service;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import io.jsonwebtoken.io.IOException;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.Value;
//import nbc_final.gathering.common.dto.AuthUser;
//import nbc_final.gathering.common.exception.ResponseCode;
//import nbc_final.gathering.common.exception.ResponseCodeException;
//import nbc_final.gathering.domain.example.attachment.entity.Attachment;
//import nbc_final.gathering.domain.example.attachment.repository.AttachmentRepository;
//import nbc_final.gathering.domain.user.entity.User;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class GroupAttachmentService {
//
//    private final AmazonS3 amazonS3;
//    private final AttachmentRepository attachmentRepository;
//
//    @Value("${S3_BUCKET}")
//    private String bucketName;
//
//    // 지원되는 파일 형식과 크기 제한
//    private static final List<String> SUPPORTED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");
//    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
//
//    // 유저 프로필 등록
//    @Transactional
//    public String groupUploadFile(User user, MultipartFile file) throws IOException {
//        validateFile(file);
//        String fileUrl = uploadToS3(file, user);
//        saveAttachment(user, fileUrl);
//        return fileUrl;
//    }
//
//    // 유저 이미지 수정
//    @Transactional
//    public String groupUploadFile (MultipartFile file, AuthUser user) throws IOException {
//        validateFile(file);
//
//        Attachment existingAttachment = attachmentRepository.findByUser(user);
//        if (existingAttachment != null) {
//            deleteFromS3(existingAttachment.getProfileImagePath());
//        }
//
//        return groupUploadFile(file, user);
//    }
//
//    // 유저 이미지 삭제
//    @Transactional
//    public void groupDeleteFile(AuthUser authUser, Long groupId) {
//        // 기존 파일 찾기
//        Attachment existingAttachment = attachmentRepository.findByUserAndGroup(authUser, groupId);
//        if (existingAttachment != null) {
//            deleteFromS3(existingAttachment.getProfileImagePath());
//            attachmentRepository.delete(existingAttachment);
//        }
//    }
//
//    // 유저 이미지 예외처리
//    private void validateFile(MultipartFile file) {
//        if (!SUPPORTED_FILE_TYPES.contains(file.getContentType())) {
//            throw new ResponseCodeException(ResponseCode.NOT_SERVICE);
//        }
//
//        if (file.getSize() > MAX_FILE_SIZE) {
//            throw new ResponseCodeException(ResponseCode.TOO_LARGE_SIZE_FILE);
//        }
//    }
//
//    // 유저 삭제 메서드
//    private void deleteFromS3(String fileUrl) {
//        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//        amazonS3.deleteObject(bucketName, fileName);
//    }
//
//    private String uploadToS3(MultipartFile file, User user) throws IOException {
//        String fileName = file.getOriginalFilename();
//        String fileUrl = "https://" + bucketName + "/profile-images/" + fileName;
//
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(file.getContentType());
//        metadata.setContentLength(file.getSize());
//
//        amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);
//        return fileUrl;
//    }
//
//    private void saveAttachment(User user, String fileUrl) {
//        Attachment attachment = new Attachment(fileUrl, user);
//        attachmentRepository.save(attachment);
//    }
//}
