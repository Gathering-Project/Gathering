package nbc_final.gathering.domain.attachment;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.attachment.service.UserAttachmentService;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserAttachmentServiceTest {

    @Autowired
    private UserAttachmentService userAttachmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @MockBean
    private AmazonS3 amazonS3;

    private AuthUser authUser;
    private User testUser;

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

        // AuthUser 생성
        authUser = new AuthUser(testUser.getId(), testUser.getEmail(), testUser.getUserRole(), "testNick");
        authUser.setId(1L);
    }

    @Test
    void testUserUploadFile() throws IOException {
        // MockMultipartFile 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "Test Image Content".getBytes()
        );

        // S3 Bucket 정보 및 Mock 동작 정의
        String bucketName = "bucket-name";
        String fileName = "profile-images/test-image.jpg";
        String fileUrl = "https://" + bucketName + "/" + fileName;

        // Mock AmazonS3 동작
        PutObjectResult mockPutObjectResult = new PutObjectResult();
        when(amazonS3.putObject(
                eq(bucketName),
                eq(fileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        )).thenReturn(mockPutObjectResult); // 반환값 정의

        // getUrl 동작 정의
        when(amazonS3.getUrl(eq(bucketName), eq(fileName)))
                .thenReturn(new URL(fileUrl)); // 반환 URL 정의

        // Act: 테스트 메서드 실행
        AttachmentResponseDto responseDto = userAttachmentService.userUploadFile(authUser, mockFile);

        // Assert
        // 반환된 URL 검증
        assertNotNull(responseDto);
        assertEquals(fileUrl, responseDto.getProfileImagePath());

        // 저장된 Attachment 엔티티 검증
        Attachment savedAttachment = attachmentRepository.findById(responseDto.getId()).orElse(null);
        assertNotNull(savedAttachment);
        assertEquals(fileUrl, savedAttachment.getProfileImagePath());
        assertEquals(testUser.getId(), savedAttachment.getUser().getId());

        // AmazonS3 동작 검증
        verify(amazonS3, times(1)).putObject(
                eq(bucketName),
                eq(fileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
        verify(amazonS3, times(1)).getUrl(eq(bucketName), eq(fileName));
    }

    @Test
    void testUserUpdateFile() throws IOException {
        MockMultipartFile initialFile = new MockMultipartFile(
                "file",
                "initial-image.jpg",
                "image/jpeg",
                "Initial Content".getBytes()
        );

        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "new-image.jpg",
                "image/jpeg",
                "New Content".getBytes()
        );

        /// Mock AmazonS3 behavior
        doNothing().when(amazonS3).putObject(eq("bucket-name"), anyString(), any(ByteArrayInputStream.class), any(ObjectMetadata.class));
        doNothing().when(amazonS3).deleteObject(eq("bucket-name"), anyString());

        // Upload initial file
        AttachmentResponseDto initialResponse = userAttachmentService.userUploadFile(authUser, initialFile);

        // Assert initial upload
        assertNotNull(initialResponse);
        assertTrue(initialResponse.getProfileImagePath().contains("initial-image.jpg"));

        // Act: Update with new file
        AttachmentResponseDto updatedResponse = userAttachmentService.userUpdateFile(authUser, newFile);

        // Assert new upload
        assertNotNull(updatedResponse);
        assertTrue(updatedResponse.getProfileImagePath().contains("new-image.jpg"));

        // Verify repository update
        Attachment updatedAttachment = attachmentRepository.findById(updatedResponse.getId()).orElse(null);
        assertNotNull(updatedAttachment);
        assertTrue(updatedAttachment.getProfileImagePath().contains("new-image.jpg"));

        // Verify S3 interactions
        verify(amazonS3, times(2)).putObject(eq("bucket-name"), anyString(), any(ByteArrayInputStream.class), any(ObjectMetadata.class));
        verify(amazonS3, times(1)).deleteObject(eq("bucket-name"), contains("initial-image.jpg"));
    }

    @Test
    void testUserDeleteFile() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "Test Image Content".getBytes()
        );

        // Mock AmazonS3 behavior
        doNothing().when(amazonS3).putObject(eq("bucket-name"), anyString(), any(ByteArrayInputStream.class), any(ObjectMetadata.class));
        doNothing().when(amazonS3).deleteObject(eq("bucket-name"), anyString());

        // Upload file
        AttachmentResponseDto responseDto = userAttachmentService.userUploadFile(authUser, mockFile);

        // Act: Delete file
        userAttachmentService.userDeleteFile(authUser);

        // Assert
        Optional<Attachment> deletedAttachment = attachmentRepository.findById(responseDto.getId());
        assertTrue(deletedAttachment.isEmpty());
    }

    @Test
    void testUploadFile_InvalidFileType() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test-file.txt",
                "text/plain",
                "Invalid Content".getBytes()
        );

        // Act & Assert
        ResponseCodeException exception = assertThrows(ResponseCodeException.class,
                () -> userAttachmentService.userUploadFile(authUser, invalidFile));
        assertEquals("지원하지 않는 파일 형식입니다.",exception.getMessage());
    }

    @Test
    void testUploadFile_FileTooLarge() {
        byte[] largeContent = new byte[(int) (5 * 1024 * 1024) + 1]; // 5MB + 1 byte
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );

        // Act & Assert
        ResponseCodeException exception = assertThrows(ResponseCodeException.class,
                () -> userAttachmentService.userUploadFile(authUser, largeFile));
        assertEquals("TOO_LARGE_SIZE_FILE", exception);
    }
}

