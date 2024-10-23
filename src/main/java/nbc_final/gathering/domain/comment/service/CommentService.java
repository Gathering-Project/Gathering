package nbc_final.gathering.domain.comment.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.comment.dto.request.CommentSaveRequestDto;
import nbc_final.gathering.domain.comment.dto.request.CommentUpdateRequestDto;
import nbc_final.gathering.domain.comment.dto.response.CommentSaveResponseDto;
import nbc_final.gathering.domain.comment.dto.response.CommentUpdateResponseDto;
import nbc_final.gathering.domain.comment.entity.Comment;
import nbc_final.gathering.domain.comment.repository.CommentRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.dto.response.UserGetResponseDto;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final GatheringRepository gatheringRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentSaveResponseDto saveComment(CommentSaveRequestDto commentSaveRequestDto, Long gatheringId, Long userId, Long eventId) {
        //소모임 존재 여부 확인
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));

        //이벤트 존재 여부 확인
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        //멤버 확인 여부
        boolean isMember = memberRepository.existsByUserIdAndGatheringId(userId,gatheringId);
        if (!isMember) {
            throw new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER);
        }

        //댓글 작성자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        //댓글 생성
        Comment comment = new Comment(commentSaveRequestDto.getContent(), event, user);
        commentRepository.save(comment);

            return CommentSaveResponseDto.of(comment);
        }


    @Transactional
    public CommentUpdateResponseDto updateComment(CommentUpdateRequestDto commentUpdateRequestDto, Long commentId, Long userId, Long groupId, Long eventId) {
        //댓글 존재 여부 확인
        Comment comment = getComment(commentId);

        //소모임 존재 여부 확인
        Gathering gathering = getGathering(groupId);

//        //이벤트 존재 여부 확인
//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        //댓글 작성자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        //댓글 작성자인지 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        //댓글 수정
        comment.updateContent(commentUpdateRequestDto.getContent());

        return new CommentUpdateResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

    private Gathering getGathering(Long groupId) {
        Gathering gathering = gatheringRepository.findById(groupId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));
        return gathering;
    }

    private Comment getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_COMMENT));
        return comment;
    }

    @Transactional
        public void deleteComment(Long commentId, Long userId, Long gatheringId, Long eventId) {
            //댓글 존재 여부 확인
        Comment comment = getComment(commentId);

        //소모임 존재 여부 확인
        Gathering gathering = getGathering1(gatheringId);

        //        //이벤트 존재 여부 확인
//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        //멤버 확인
        memberRepository.findByUserIdAndGatheringId(userId, gatheringId);

        //사용자가 소모임의 멤버인지 확인
            boolean isMember = gathering.getMembers().contains(userId) || gathering.getId().equals(userId);
            if (!isMember) {
                throw new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER);
            }


        //삭제 권한 확인
            boolean isAuthor = comment.getUser().getId().equals(userId);
            boolean isLeader = gathering.getId().equals(userId);
            if (!isAuthor || !isLeader) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }

            //댓글 삭제
            commentRepository.delete(comment);
        }

    private Gathering getGathering1(Long groupId) {
        Gathering gathering = gatheringRepository.findById(groupId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));
        return gathering;
    }
}
