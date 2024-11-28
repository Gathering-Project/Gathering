package nbc_final.gathering.domain.attachment;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void testGatheringProfileUpload() throws IOException {
        // Mock S3 업로드
        PutObjectResult mockPutObjectResult = Mockito.mock(PutObjectResult.class);
        when(amazonS3.putObject(any(), any(), any(), any())).thenReturn(mockPutObjectResult);

        // gatheringUploadFile 조회
        AttachmentResponseDto responseDto = gatheringAttachmentService.gatheringUploadFile(authUser, testGathering.getId(), testFile);

        // response 검증
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getProfileImagePath()).contains("test-image.jpg");

        // S3 상호작용 검증
        verify(amazonS3, Mockito.times(1)).putObject(any(), any(), any(), any());

        // repository 검증
        List<Attachment> savedAttachments = attachmentRepository.findByUserAndGathering(testUser, testGathering);
        assertThat(savedAttachments).hasSize(1);
    }

    @Test
    void testInvalidFileUploadThrowsException() {
        // 무효한 Mock file type
        when(testFile.getContentType()).thenReturn("application/pdf");

        // exception 검증
        ResponseCodeException exception = assertThrows(ResponseCodeException.class, () ->
                userAttachmentService.userUploadFile(authUser, testFile));

        assertThat(exception.getMessage()).isEqualTo("지원하지 않는 파일 형식입니다.");

    }

    @Test
    void testGatheringProfileDelete() {
        // 존재하는 첨부파일 준비
        Attachment attachment = new Attachment(testUser, testGathering, "test-image-path.jpg");
        attachmentRepository.save(attachment);

        // Mock S3 삭제
        doNothing().when(amazonS3).deleteObject(any(), any());

        // gatheringDeleteFile 조회
        gatheringAttachmentService.gatheringDeleteFile(authUser, testGathering.getId());

        // deletion 검증
        List<Attachment> savedAttachments = attachmentRepository.findByUserAndGathering(testUser, testGathering);
        assertThat(savedAttachments).isEmpty();

        // S3 상호작용 검증
        verify(amazonS3, Mockito.times(1)).deleteObject(any(), any());
    }


}