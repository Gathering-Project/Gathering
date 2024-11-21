package nbc_final.gathering.domain.attachment;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import nbc_final.gathering.domain.attachment.service.UserAttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAttachmentServiceTest {

    @InjectMocks
    private UserAttachmentService userAttachmentService;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private UserRepository userRepository;

    private AuthUser authUser;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // User 설정
        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "userRole", UserRole.ROLE_USER);

        // AuthUser 설정
        authUser = new AuthUser(user.getId(), "test@example.com", UserRole.ROLE_USER, "TestUser");

        // bucketName 설정
        ReflectionTestUtils.setField(userAttachmentService, "bucketName", "test-bucket");
    }

    @Test
    void testUserUploadFile_Success() throws IOException {
        // Mock MultipartFile
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test-image.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
        when(file.getInputStream()).thenReturn(inputStream);

        // Mock Repositories
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Mock S3
        doAnswer(invocation -> null)
                .when(amazonS3).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));

        // Act
        AttachmentResponseDto responseDto = userAttachmentService.userUploadFile(authUser, file);

        // Assert
        assertNotNull(responseDto);
        assertEquals("https://test-bucket/profile-images/test-image.jpg", responseDto.getProfileImagePath());
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUserUpdateFile_Success() throws IOException {
        // Mock MultipartFile
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.getOriginalFilename()).thenReturn("updated-image.jpg");
        when(newFile.getContentType()).thenReturn("image/jpeg");
        when(newFile.getSize()).thenReturn(2048L);
        InputStream inputStream = new ByteArrayInputStream("updated data".getBytes());
        when(newFile.getInputStream()).thenReturn(inputStream);

        // Mock Existing Attachment
        Attachment existingAttachment = new Attachment();
        ReflectionTestUtils.setField(existingAttachment, "profileImagePath", "https://test-bucket/profile-images/test-image.jpg");
        ReflectionTestUtils.setField(existingAttachment, "user", user);

        // Mock Repositories
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(attachmentRepository.findByUser(user)).thenReturn(List.of(existingAttachment));

        // Mock S3
        doAnswer(invocation -> null)
                .when(amazonS3).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());

        // Act
        AttachmentResponseDto responseDto = userAttachmentService.userUpdateFile(authUser, newFile);

        // Assert
        assertNotNull(responseDto);
        assertEquals("https://test-bucket/profile-images/updated-image.jpg", responseDto.getProfileImagePath());
        verify(attachmentRepository, times(1)).delete(existingAttachment);
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUserDeleteFile_Success() {
        // Mock Existing Attachment
        Attachment existingAttachment = new Attachment();
        ReflectionTestUtils.setField(existingAttachment, "profileImagePath", "https://test-bucket/profile-images/test-image.jpg");
        ReflectionTestUtils.setField(existingAttachment, "user", user);

        // Mock Repositories
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(attachmentRepository.findByUser(user)).thenReturn(List.of(existingAttachment));

        // Mock S3
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());

        // Act
        userAttachmentService.userDeleteFile(authUser);

        // Assert
        verify(attachmentRepository, times(1)).delete(existingAttachment);
        verify(amazonS3, times(1)).deleteObject("test-bucket", "test-image.jpg");
    }
}