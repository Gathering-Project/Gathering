package nbc_final.gathering.domain.attachment;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.attachment.service.GatheringAttachmentService;
import nbc_final.gathering.domain.attachment.service.UserAttachmentService;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GatheringAttachmentServiceTest {
    @Autowired
    private GatheringAttachmentService gatheringAttachmentService;

    @Autowired
    private UserAttachmentService userAttachmentService;

    @MockBean
    private AmazonS3 amazonS3;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    private User testUser;
    private AuthUser authUser;
    private Gathering testGathering;
    private MultipartFile testFile;
    private Member testMember;
    
    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        testUser = new User();
        testUser.setId(1L);
        testUser.setPassword("abcd123@");
        testUser.setEmail("test@example.com");
        testUser.setProfileImagePath("test-image-path.jpg");
        testUser.setUserRole(UserRole.ROLE_USER);
        testUser = userRepository.saveAndFlush(testUser);

        // 테스트 소모임 생성
        testGathering = new Gathering();
        testGathering.setId(1L);
        testGathering.setGatheringCount(20);
        testGathering.setGatheringMaxCount(30);
        testGathering.setRating(BigDecimal.valueOf(4));
        testGathering.setLocation("test location");
        testGathering.setTitle("test Title");
        testGathering.setDescription("test description");
        gatheringRepository.save(testGathering);

        // AuthUser 생성
        authUser = new AuthUser(testUser.getId(), testUser.getEmail(), testUser.getUserRole(), "testNick");
        authUser.setId(1L);

        // 테스트 맴버 생성(권한는 HOST)
        testMember = new Member(testUser, testGathering, MemberRole.HOST, MemberStatus.APPROVED);
        memberRepository.saveAndFlush(testMember);

        // 테스트 소모임에 멤버 추가
        testGathering.getMembers().add(testMember);
        gatheringRepository.saveAndFlush(testGathering);

        // mock MultipartFile 생성
        testFile = Mockito.mock(MultipartFile.class);
        when(testFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(testFile.getContentType()).thenReturn("image/jpeg");
        when(testFile.getSize()).thenReturn(1024L);
        try {
            when(testFile.getInputStream()).thenReturn(new ByteArrayInputStream("test-content".getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("소모임 프로필 이미지 업로드 테스트")
    void testGatheringProfileUpload() throws IOException {
        String bucketName = "wearemeetnow";
        String fileName = "test-image.jpg";
        String fileUrl = "https://" + bucketName + "/profile-images/" + fileName; // S3 URL

        // Mock S3 업로드 동작 설정
        when(amazonS3.putObject(
                eq(bucketName),
                eq(fileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        )).thenReturn(new PutObjectResult());
        when(amazonS3.getUrl(eq(bucketName), eq(fileName))).thenReturn(new URL(fileUrl));

        // Act: 소모임 프로필 이미지 업로드 호출
        AttachmentResponseDto responseDto = gatheringAttachmentService.gatheringUploadFile(authUser, testGathering.getId(), testFile);

        // Assert: 반환값 검증
        assertNotNull(responseDto);
        assertEquals(fileUrl, responseDto.getProfileImagePath());

        // Repository 저장 검증
        List<Attachment> savedAttachments = attachmentRepository.findByUserAndGathering(testUser, testGathering);
        assertEquals(1, savedAttachments.size());
        assertEquals(fileUrl, savedAttachments.get(0).getProfileImagePath());

        // S3 업로드 호출 검증
        verify(amazonS3, times(1)).putObject(
                eq(bucketName),
                eq(fileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
    }

    @Test
    @DisplayName("소모임 프로필 이미지 수정 테스트")
    void testGatheringUpdateFile() throws IOException {
        String bucketName = "wearemeetnow";
        String initialFileName = "initial-image.jpg";
        String initialFileUrl = "https://" + bucketName + "/profile-images/" + initialFileName;

        String newFileName = "new-image.jpg";
        String newFileUrl = "https://" + bucketName + "/profile-images/" + newFileName;

        MockMultipartFile initialFile = new MockMultipartFile(
                "file",
                initialFileName,
                "image/jpeg",
                "Initial Content".getBytes()
        );

        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                newFileName,
                "image/jpeg",
                "New Content".getBytes()
        );

        // Mock S3
        when(amazonS3.putObject(
                eq(bucketName),
                eq(initialFileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        )).thenReturn(new PutObjectResult());

        when(amazonS3.putObject(
                eq(bucketName),
                eq(newFileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        )).thenReturn(new PutObjectResult());

        when(amazonS3.getUrl(eq(bucketName), eq(initialFileName)))
                .thenReturn(new URL(initialFileUrl));

        when(amazonS3.getUrl(eq(bucketName), eq(newFileName)))
                .thenReturn(new URL(newFileUrl));

        // Act: Upload initial file
        AttachmentResponseDto initialResponse = userAttachmentService.userUploadFile(authUser, initialFile);

        // Assert: 검증 initial upload
        assertNotNull(initialResponse);
        assertEquals(initialFileUrl, initialResponse.getProfileImagePath());

        // Act: new file 수정
        AttachmentResponseDto updatedResponse = userAttachmentService.userUpdateFile(authUser, newFile);

        // Assert: update 검증
        assertNotNull(updatedResponse);
        assertEquals(newFileUrl, updatedResponse.getProfileImagePath());

        // repository 검증
        List<Attachment> savedAttachments = attachmentRepository.findByUser(testUser);
        assertEquals(1, savedAttachments.size());
        assertEquals(newFileUrl, savedAttachments.get(0).getProfileImagePath());

        // S3 상호작용 검증
        verify(amazonS3, times(1)).putObject(
                eq(bucketName),
                eq(initialFileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
        verify(amazonS3, times(1)).deleteObject(eq(bucketName), eq(initialFileName)); // 기존 파일 삭제
        verify(amazonS3, times(1)).putObject(
                eq(bucketName),
                eq(newFileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
    }

    @Test
    @DisplayName("파일 확장명이 다를때 예외처리 테스트")
    void testInvalidFileUploadThrowsException() {
        // 무효한 Mock file type
        when(testFile.getContentType()).thenReturn("application/pdf");

        // exception 검증
        ResponseCodeException exception = assertThrows(ResponseCodeException.class, () ->
                userAttachmentService.userUploadFile(authUser, testFile));

        assertThat(exception.getMessage()).isEqualTo("지원하지 않는 파일 형식입니다.");

    }

    @Test
    @DisplayName("소모임 프로필 이미지 삭제")
    void testGatheringProfileDelete() {
        String bucketName = "wearemeetnow";
        String fileName = "test-image.jpg";
//        String filePath = "profile-images/" + fileName;

        // 존재하는 첨부파일 준비
        Attachment attachment = new Attachment(testUser, testGathering, "https://" + bucketName + "/profile-images/" + fileName);
        attachmentRepository.saveAndFlush(attachment);

        // Mock S3 삭제 동작 설정
        doNothing().when(amazonS3).deleteObject(eq(bucketName), eq(fileName));

        // Act: 소모임 프로필 이미지 삭제 호출
        gatheringAttachmentService.gatheringDeleteFile(authUser, testGathering.getId());

        // Assert: Repository에서 첨부파일이 삭제되었는지 확인
        List<Attachment> savedAttachments = attachmentRepository.findByUserAndGathering(testUser, testGathering);
        assertThat(savedAttachments).isEmpty();

        // S3 삭제 호출 검증
        verify(amazonS3, times(1)).deleteObject(eq(bucketName), eq(fileName));
    }

}