package nbc_final.gathering.domain.attachment;

import com.amazonaws.services.s3.AmazonS3;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.attachment.dto.AttachmentResponseDto;
import nbc_final.gathering.domain.attachment.entity.Attachment;
import nbc_final.gathering.domain.attachment.repository.AttachmentRepository;
import nbc_final.gathering.domain.attachment.service.GatheringAttachmentService;
import nbc_final.gathering.domain.attachment.service.UserAttachmentService;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.times;

@SpringBootTest
@Disabled
@AutoConfigureMockMvc
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

    private User testUser;
    private AuthUser authUser;
    private Gathering testGathering;
    private MultipartFile testFile;
    
    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setProfileImagePath("test-image-path.jpg");
        userRepository.save(testUser);

        // Create a test gathering
        testGathering = new Gathering();
        testGathering.setId(1L);
        gatheringRepository.save(testGathering);

        // Create AuthUser
        authUser = new AuthUser(1L, "test@example.com", UserRole.ROLE_USER, "testNick");
        authUser.setId(1L);

        // Create mock MultipartFile
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
    void testUserProfileUpload() throws IOException {
        // Mock S3 upload behavior
        doNothing().when(amazonS3).putObject(any(), any(), any(), any());

        // Call userUploadFile
        AttachmentResponseDto responseDto = userAttachmentService.userUploadFile(authUser, testFile);

        // Validate response
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getProfileImagePath()).contains("test-image.jpg");

        // Verify S3 interaction
        verify(amazonS3, times(1)).putObject(any(), any(), any(), any());

        // Validate repository
        List<Attachment> savedAttachments = attachmentRepository.findByUser(testUser);
        assertThat(savedAttachments).hasSize(1);
    }

    @Test
    void testUserProfileDelete() {
        // Prepare existing attachment
        Attachment attachment = new Attachment(testUser, null, "test-image-path.jpg");
        attachmentRepository.save(attachment);

        // Mock S3 deletion
        doNothing().when(amazonS3).deleteObject(any(), any());

        // Call userDeleteFile
        userAttachmentService.userDeleteFile(authUser);

        // Validate deletion
        List<Attachment> savedAttachments = attachmentRepository.findByUser(testUser);
        assertThat(savedAttachments).isEmpty();

        // Verify S3 interaction
        verify(amazonS3, times(1)).deleteObject(any(), any());
    }

    @Test
    void testGatheringProfileUpload() throws IOException {
        // Mock S3 upload behavior
        doNothing().when(amazonS3).putObject(any(), any(), any(), any());

        // Call gatheringUploadFile
        AttachmentResponseDto responseDto = gatheringAttachmentService.gatheringUploadFile(authUser, testGathering.getId(), testFile);

        // Validate response
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getProfileImagePath()).contains("test-image.jpg");

        // Verify S3 interaction
        verify(amazonS3, times(1)).putObject(any(), any(), any(), any());

        // Validate repository
        List<Attachment> savedAttachments = attachmentRepository.findByUserAndGathering(testUser, testGathering);
        assertThat(savedAttachments).hasSize(1);
    }

    @Test
    void testInvalidFileUploadThrowsException() {
        // Mock invalid file type
        when(testFile.getContentType()).thenReturn("application/pdf");

        // Validate exception
        assertThrows(ResponseCodeException.class, () ->
                userAttachmentService.userUploadFile(authUser, testFile));
    }

    @Test
    void testGatheringProfileDelete() {
        // Prepare existing attachment
        Attachment attachment = new Attachment(testUser, testGathering, "test-image-path.jpg");
        attachmentRepository.save(attachment);

        // Mock S3 deletion
        doNothing().when(amazonS3).deleteObject(any(), any());

        // Call gatheringDeleteFile
        gatheringAttachmentService.gatheringDeleteFile(authUser, testGathering.getId());

        // Validate deletion
        List<Attachment> savedAttachments = attachmentRepository.findByUserAndGathering(testUser, testGathering);
        assertThat(savedAttachments).isEmpty();

        // Verify S3 interaction
        verify(amazonS3, times(1)).deleteObject(any(), any());
    }

    private AmazonS3 verify(AmazonS3 amazonS3, ExpectedCount times) {
        return null;
    }


}