package nbc_final.gathering.domain.attachment;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.domain.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.attachment.service.GatheringAttachmentService;
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

class GatheringAttachmentServiceTest {

    @InjectMocks
    private GatheringAttachmentService gatheringAttachmentService;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberRepository memberRepository;

    private Gathering gathering;
    private Member hostMember;
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
        authUser = new AuthUser(user.getId(), "test@example.com", UserRole.ROLE_USER, "HostUser");

        // Gathering 설정
        gathering = new Gathering();
        ReflectionTestUtils.setField(gathering, "id", 1L);
        ReflectionTestUtils.setField(gathering, "title", "Test Gathering");
        ReflectionTestUtils.setField(gathering, "gatheringMaxCount", 10);

        // Host Member 설정
        hostMember = new Member();
        ReflectionTestUtils.setField(hostMember, "user", user);
        ReflectionTestUtils.setField(hostMember, "gathering", gathering);
        ReflectionTestUtils.setField(hostMember, "role", MemberRole.HOST);
        ReflectionTestUtils.setField(hostMember, "status", MemberStatus.APPROVED);

        ReflectionTestUtils.setField(gathering, "members", List.of(hostMember));

        // bucketName 설정
        ReflectionTestUtils.setField(gatheringAttachmentService, "bucketName", "test-bucket");
    }

    @Test
    void testGatheringUploadFile_Success() throws IOException {
        // Mock MultipartFile
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test-image.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
        when(file.getInputStream()).thenReturn(inputStream);

        // Mock Repositories
        when(gatheringRepository.findById(1L)).thenReturn(Optional.of(gathering));
        when(memberRepository.findByUserAndGathering(user, gathering)).thenReturn(Optional.of(hostMember));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Mock S3
        doAnswer(invocation -> null)
                .when(amazonS3).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));

        // Act
        AttachmentResponseDto responseDto = gatheringAttachmentService.gatheringUploadFile(authUser, 1L, file);

        // Assert
        assertNotNull(responseDto);
        assertEquals("https://test-bucket/profile-images/test-image.jpg", responseDto.getProfileImagePath());
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
    }

    @Test
    void testGatheringUpdateFile_Success() throws IOException {
        // Mock MultipartFile
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.getOriginalFilename()).thenReturn("updated-image.jpg");
        when(newFile.getContentType()).thenReturn("image/jpeg");
        when(newFile.getSize()).thenReturn(2048L);
        InputStream inputStream = new ByteArrayInputStream("updated data".getBytes());
        when(newFile.getInputStream()).thenReturn(inputStream);

        // Mock Existing Attachment
        Attachment existingAttachment = new Attachment();
        ReflectionTestUtils.setField(existingAttachment, "profileImagePath", "https://bucket-name/profile-images/test-image.jpg");
        ReflectionTestUtils.setField(existingAttachment, "user", user);
        ReflectionTestUtils.setField(existingAttachment, "gathering", gathering);

        // Mock Repositories
        when(gatheringRepository.findById(1L)).thenReturn(Optional.of(gathering));
        when(memberRepository.findByUserAndGathering(user, gathering)).thenReturn(Optional.of(hostMember));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(attachmentRepository.findByUserAndGathering(user, gathering)).thenReturn(List.of(existingAttachment));

        // Mock S3
        doAnswer(invocation -> null)
                .when(amazonS3).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());

        // Act
        AttachmentResponseDto responseDto = gatheringAttachmentService.gatheringUpdateFile(authUser, 1L, newFile);

        // Assert
        assertNotNull(responseDto);
        assertEquals("https://test-bucket/profile-images/updated-image.jpg", responseDto.getProfileImagePath());
        verify(attachmentRepository, times(1)).delete(existingAttachment);
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
    }
}