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
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final GatheringRepository gatheringRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CommentSaveResponseDto saveComment(CommentSaveRequestDto commentSaveRequestDto, Long gatheringId, Long userId, Long eventId) {
        //소모임 존재 여부 확인
        Gathering gathering = groupRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));

        //댓글 작성자 확인
        boolean isMember = gathering.getMembers().contains(userId) || gathering.getLeader().getId().equals(userId);
        ;
        if (!isMember) {
            throw new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER);
        }

        // 유저 내에서 유저의 권한을 확인 -> READ 권한만 있는지 확인
        boolean isReadOnly = userRepository.existsByMemberIdAndUserIdAndUserRole(userId, eventId, UserRole.READ);
        if (isReadOnly) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);
        }

        //이벤트 존재 여부 확인
        Event event = EventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));

        //댓글 작성자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));

        //댓글 생성
        Comment comment = new Comment(commentSaveRequestDto.getContent(), event, userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_COMMENT)));
        commentRepository.save(comment);

        CommentSaveResponseDto a = null;
        try {
            a = new CommentSaveResponseDto(comment.getId(),
                    comment.getContent(),
                    comment.getId(),
                    comment.getCreatedAt());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;

    }


    @Transactional
    public CommentUpdateResponseDto updateComment(CommentUpdateRequestDto commentUpdateRequestDto, Long commentId, Long userId, Long groupId, Long eventId) {
        //댓글 존재 여부 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_COMMENT));
        ;

        //소모임 존재 여부 확인
        Gathering gathering = gatheringRepository.findById(groupId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));

        //사용자가 소모임의 멤버인지 확인
        boolean isMember = gathering.getMembers().contains(userId) || gathering.getLeader().getId().equals(userId);
        if (!isMember) {
            throw new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER);
        }

        //댓글 작성자인지 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.FORBIDDEN);

            //댓글 수정
            comment.setContent(commentUpdateRequestDto.getContent());
            commentRepository.save(comment);

        }
        return new CommentUpdateResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

        @Transactional
        public void deleteComment(Long commentId, Long userId, Long groupId, Long eventId) {
            //댓글 존재 여부 확인
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_COMMENT));

            //소모임 존재 여부 확인
            Gathering gathering = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));
            ;

            //사용자가 소모임의 멤버인지 확인
            boolean isMember = gathering.getMembers().contains(userId) || gathering.getLeader().getId().equals(userId);
            if (!isMember) {
                throw new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER);
            }

            //삭제 권한 확인
            boolean isAuthor = comment.getUser().getId().equals(userId);
            boolean isLeader = gathering.getLeader().getId().equals(userId);
            if (!isAuthor || !isLeader) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }

            //댓글 삭제
            commentRepository.delete(comment);
        }
    }
