package nbc_final.gathering.domain.comment.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.comment.dto.request.CommentRequestDto;
import nbc_final.gathering.domain.comment.dto.response.CommentResponseDto;
import nbc_final.gathering.domain.comment.entity.Comment;
import nbc_final.gathering.domain.comment.repository.CommentRepository;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.event.repository.EventRepository;
import nbc_final.gathering.domain.event.repository.EventRepositoryCustom;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final GatheringRepository gatheringRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final EventRepositoryCustom eventRepositoryCustom;

    //    @Transactional
//    public CommentResponseDto saveComment(CommentRequestDto commentRequestDto, Long gatheringId, Long userId, Long eventId) {
//        //소모임 존재 여부 확인
//        Gathering gathering = gatheringRepository.findById(gatheringId)
//                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));
//
//        //이벤트 존재 여부 확인
//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));
//
//        // 멤버 존재 여부 확인
//        Member member = memberRepository.findByUserIdAndGatheringId(userId, gatheringId)
//                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));
//
//        // 만약 아직 소모임 멤버가 아니고(신청 승인되지 않은 상태라면)
//        if (member.getStatus() != MemberStatus.APPROVED) {
//            // 관리자도 아니라면
//            if (member.getUser().getUserRole() != UserRole.ROLE_ADMIN) {
//                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
//            }
//        }
//
//
//       //댓글 작성자 존재 여부 확인
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
//
//        //댓글 생성
//        Comment comment = new Comment(commentRequestDto.getContent(), event, user);
//        commentRepository.save(comment);
//
//        return CommentResponseDto.of(comment);
//   }
    @Transactional
    public CommentResponseDto saveComment(CommentRequestDto commentRequestDto, Long gatheringId, Long userId, Long eventId) {
        //소모임 존재 여부 확인
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        //이벤트 존재 여부 확인
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        // 멤버 존재 여부 확인
        Optional<Member> optionalMember = memberRepository.findByUserIdAndGatheringId(userId, gatheringId);

        User user = userRepository.findById(userId).get();

        // 참가 신청 권한 없어서 멤버가 아닌(멤버가 없는) 관리자인지 확인 - 유저 7번(관리자) [생성]
        if ((!optionalMember.isPresent()) && user.getUserRole() != UserRole.ROLE_ADMIN) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        // 멤버 신청은 되었지만 아직 승인되지 않은 경우
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            if (member.getStatus() != MemberStatus.APPROVED) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }
        }

        //댓글 생성
        Comment comment = new Comment(commentRequestDto.getContent(), gathering, event, user);
        commentRepository.save(comment);

        return CommentResponseDto.of(comment);
    }


    @Transactional
    public CommentResponseDto updateComment(CommentRequestDto commentRequestDto, Long commentId, Long userId, Long gatheringId, Long eventId) {
        //댓글 존재 여부 확인
        Comment comment = getComment(commentId);

        //소모임 존재 여부 확인
        Gathering gathering = getGathering(gatheringId);

        //이벤트 존재 여부 확인
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        //댓글 작성자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        //댓글 작성자인지 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        //댓글 수정
        comment.updateContent(commentRequestDto.getContent());

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

    private Gathering getGathering(Long groupId) {
        Gathering gathering = gatheringRepository.findById(groupId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));
        return gathering;
    }

    private Comment getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_COMMENT));
        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, Long gatheringId, Long eventId) {

        // 댓글 존재 여부 확인
        Comment comment = getComment(commentId);

        // 댓글 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

            // 권한 확인 (메서드 이름 수정)
            checkAdminOrEventCreatorOrGatheringCreator(userId, eventId, gatheringId);

            // ADMIN 권한을 가진 경우는 추가 검증 없이 삭제 허용
            if (user.getUserRole() != UserRole.ROLE_ADMIN) {
                // 소모임 존재 여부 확인
                Gathering gathering = gatheringRepository.findById(gatheringId)
                        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

                // 이벤트 존재 여부 확인
                Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

                // 멤버 확인
                Member member = memberRepository.findByUserIdAndGatheringId(userId, gatheringId)
                        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

                // 멤버의 역할 확인
                if (member.getRole() != MemberRole.HOST) {
                    throw new ResponseCodeException(ResponseCode.FORBIDDEN);
                }
            }
        }
        // 댓글 삭제
        commentRepository.deleteById(commentId);
    }

        // 삭제 기능 (권한: 어드민, 이벤트 생성자, 소모임 생성자)
        private void checkAdminOrEventCreatorOrGatheringCreator (Long userId, Long eventId, Long gatheringId){
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
            if (isAdmin(user)) {
                return;
            }
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

            Member member = memberRepository.findByUserIdAndGatheringId(userId, gatheringId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER));

            boolean isEventCreator = event.getUser().getId().equals(userId);
            boolean isGatheringCreator = eventRepositoryCustom.isGatheringCreator(userId, gatheringId);
            boolean isHost = isHost(member);

            if (!isEventCreator && !isGatheringCreator && !isHost) {
                    throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }
        }

        private boolean isHost (Member member){
            boolean isHost = member.getRole().equals(MemberRole.HOST);
            return isHost;
        }

        private boolean isAdmin (User user){
            boolean isAdmin = user.getUserRole().equals(UserRole.ROLE_ADMIN);
            return isAdmin;
        }
    }


