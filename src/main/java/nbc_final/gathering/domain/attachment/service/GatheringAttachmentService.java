package nbc_final.gathering.domain.attachment.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatheringAttachmentService {

  private final AmazonS3 amazonS3;
  private final AttachmentRepository attachmentRepository;
  private final GatheringRepository gatheringRepository;
  private final UserRepository userRepository;
  private final MemberRepository memberRepository;

  @Value("${CLOUD_AWS_S3_BUCKET}")
  private String bucketName;

  // 지원되는 파일 형식과 크기 제한
  private static final List<String> SUPPORTED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

  // 모임 프로필 등록
  @Transactional
  public AttachmentResponseDto gatheringUploadFile(AuthUser authUser, Long gatheringId, MultipartFile file) throws java.io.IOException {
    // 파일 체크
    validateFile(file, authUser);

    Gathering gathering = gatheringRepository.findById(gatheringId)
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

    // 멤버인지, 유저라면 Host인지 체크
    validateMemberAndHost(authUser, gathering);

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
  public AttachmentResponseDto gatheringUpdateFile(AuthUser authUser, Long gatheringId, MultipartFile file) throws java.io.IOException {
    validateFile(file, authUser);

    // Gathering 객체를 조회
    Gathering gathering = gatheringRepository.findById(gatheringId)
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

    // AuthUser에서 User 엔티티 조회
    User user = userRepository.findById(authUser.getUserId())
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

    validateMemberAndHost(authUser, gathering);

    // 유저와 소모임으로 올려진 파일 찾기
    List<Attachment> existingAttachment = attachmentRepository.findByUserAndGathering(user, gathering);
    for (Attachment attachment : existingAttachment) {
      deleteFromS3(attachment.getProfileImagePath());
      attachmentRepository.delete(attachment);
    }
    // 사진 업로드
    AttachmentResponseDto responseDto = gatheringUploadFile(authUser, gatheringId, file);
    return responseDto;
  }

  // 모임 이미지 삭제
  @Transactional
  public void gatheringDeleteFile(AuthUser authUser, Long gatheringId) {

    checkAdminOrEventCreatorForDeletion(authUser.getUserId(), gatheringId);
    // gatheringId를 사용하여 Gathering 엔티티를 조회
    Gathering gathering = gatheringRepository.findById(gatheringId)
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

    // AuthUser에서 User 엔티티 조회
    User user = userRepository.findById(authUser.getUserId())
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

    validateHostAndAdmin(user, gathering);

    // 기존 파일 찾기
    List<Attachment> existingAttachment = attachmentRepository.findByUserAndGathering(user, gathering);
    for (Attachment attachment : existingAttachment) {
      deleteFromS3(attachment.getProfileImagePath());
      attachmentRepository.delete(attachment);
    }

    gathering.setGatheringImage(null);

  }


  // 이미지 예외처리
  private void validateFile(MultipartFile file, AuthUser authUser) {
    if (!SUPPORTED_FILE_TYPES.contains(file.getContentType())) {
      throw new ResponseCodeException(ResponseCode.NOT_SERVICE);
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new ResponseCodeException(ResponseCode.TOO_LARGE_SIZE_FILE);
    }

    User user = userRepository.findById(authUser.getUserId())
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
    boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
    if (isAdmin) {
      throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }
  }

  private static void validateMemberAndHost(AuthUser authUser, Gathering gathering) {
    // 로그인한 유저와 아이디가 같은 멤버 찾기
    Member loginMember = gathering.getMembers().stream()
        .filter(member -> member.getUser().getId().equals(authUser.getUserId()))
        .findFirst()
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER, "해당 소모임의 멤버가 아닙니다."));

    // 로그인한 유저의 역할이 HOST가 아닌 경우 예외 발생
    if (loginMember.getRole() != MemberRole.HOST) {
      throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }
  }

  private void validateHostAndAdmin(User user, Gathering gathering) {
    // ADMIN이면 바로 통과
    if (user.getUserRole() == UserRole.ROLE_ADMIN) {
      return;
    }
    // 소모임 멤버인지 확인
    Member member = memberRepository.findByUserAndGathering(user, gathering)
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER, "해당 소모임의 멤버가 아닙니다."));

    // HOST가 아니면 예외 발생
    if (member.getRole() != MemberRole.HOST) {
      throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }
  }

  // 삭제 메서드
  private void deleteFromS3(String fileUrl) {
    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    amazonS3.deleteObject(bucketName, fileName);
  }

  // s3업로드 메서드
  private String uploadToS3(MultipartFile file) throws java.io.IOException {
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

  // 삭제 권한
  private void checkAdminOrEventCreatorForDeletion(Long userId, Long gatheringId) {
    Gathering gathering = gatheringRepository.findById(gatheringId)
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

    boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
    boolean isHost = (member.getRole().equals(MemberRole.HOST));

    if (!isAdmin && !isHost) {
      throw new ResponseCodeException(ResponseCode.FORBIDDEN);
    }
  }
}
