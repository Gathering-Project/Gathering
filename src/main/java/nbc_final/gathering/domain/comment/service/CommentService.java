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
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static nbc_final.gathering.domain.event.entity.QEvent.event;


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

    @Transactional
    public CommentResponseDto saveComment(CommentRequestDto commentRequestDto, Long gatheringId, Long userId, Long eventId) {
        //소모임 존재 여부 확인
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));

        //이벤트 존재 여부 확인
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        //멤버 확인 여부
        boolean isMember = memberRepository.existsByUserIdAndGatheringId(userId, gatheringId);
        if (!isMember) {
            throw new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER);
        }

        //댓글 작성자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        //댓글 생성
        Comment comment = new Comment(commentRequestDto.getContent(), event, user);
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



        // 댓글 삭제
        commentRepository.deleteById(commentId);
    }

}
