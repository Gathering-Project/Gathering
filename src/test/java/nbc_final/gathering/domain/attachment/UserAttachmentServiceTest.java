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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private MultipartFile testFile;

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
    void testUserUploadFile() throws IOException {
        String bucketName = "wearemeetnow";
        String fileName = "test-image.jpg";
        String fileUrl = "https://" + bucketName + "/profile-images/" + fileName;

        // Mock S3 동작 정의
        when(amazonS3.putObject(
                eq(bucketName),
                eq(fileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        )).thenReturn(new PutObjectResult());

        when(amazonS3.getUrl(eq(bucketName), eq("profile-images/" + fileName)))
                .thenReturn(new URL(fileUrl));

        // Act
        AttachmentResponseDto responseDto = userAttachmentService.userUploadFile(authUser, testFile);

        // Assert
        assertNotNull(responseDto);
        assertEquals(fileUrl, responseDto.getProfileImagePath());

        // Repository 검증
        List<Attachment> savedAttachments = attachmentRepository.findByUser(testUser);
        assertEquals(1, savedAttachments.size());
        assertEquals(fileUrl, savedAttachments.get(0).getProfileImagePath());

        // S3 동작 검증
        verify(amazonS3, times(1)).putObject(
                eq(bucketName),
                eq(fileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
    }

    @Test
    void testUserUpdateFile() throws IOException {
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

        when(amazonS3.getUrl(eq(bucketName), eq("profile-images/" +initialFileName)))
                .thenReturn(new URL(initialFileUrl));

        when(amazonS3.getUrl(eq(bucketName), eq("profile-images/"+newFileName)))
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
        verify(amazonS3, times(1)).deleteObject(eq(bucketName), eq(initialFileName));
        verify(amazonS3, times(1)).putObject(
                eq(bucketName),
                eq(newFileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
    }

    @Test
    void testUserDeleteFile() throws IOException {
        String bucketName = "wearemeetnow";
        String fileName = "test-image.jpg";
        String fileUrl = "https://" + bucketName + "/profile-images/" + fileName;

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                fileName,
                "image/jpeg",
                "Test Image Content".getBytes()
        );

        // Mock AmazonS3
        when(amazonS3.putObject(
                eq(bucketName),
                eq(fileName),
                any(InputStream.class),
                any(ObjectMetadata.class)
        )).thenReturn(new PutObjectResult());

        when(amazonS3.getUrl(eq(bucketName), eq(fileName)))
                .thenReturn(new URL(fileUrl));

        doNothing().when(amazonS3).deleteObject(eq(bucketName), eq(fileName));

        // Act: Upload file
        AttachmentResponseDto responseDto = userAttachmentService.userUploadFile(authUser, mockFile);

        // Assert: upload 검증
        assertNotNull(responseDto);
        assertEquals(fileUrl, responseDto.getProfileImagePath());

        // Act: file 삭제
        userAttachmentService.userDeleteFile(authUser);

        // Assert: repository 삭제 검증
        Optional<Attachment> deletedAttachment = attachmentRepository.findById(responseDto.getId());
        assertTrue(deletedAttachment.isEmpty());

        // S3 삭제 검증
        verify(amazonS3, times(1)).deleteObject(eq(bucketName), eq(fileName));
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
        assertEquals("파일 크기가 너무 큽니다.", exception.getMessage());
    }
}

