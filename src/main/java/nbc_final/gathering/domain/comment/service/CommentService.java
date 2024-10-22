package nbc_final.gathering.domain.comment.service;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.exception.ErrorCode;
import nbc_final.gathering.domain.comment.dto.request.CommentSaveRequest;
import nbc_final.gathering.domain.comment.dto.request.CommentUpdateRequest;
import nbc_final.gathering.domain.comment.dto.response.CommentSaveResponse;
import nbc_final.gathering.domain.comment.dto.response.CommentUpdateResponse;
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
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CommentSaveResponse saveComment(CommentSaveRequest commentSaveRequest, Long groupId, Long userId, Long eventId) {
        //소모임 존재 여부 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Not Found"));

        //댓글 작성자 확인
        boolean isMember = group.getMembers().contains(userId) || group.getLeader().getId().equals(userId);
        ;
        if (!isMember) {
            throw new IllegalArgumentException("Not Found");
        }

        // 유저 내에서 유저의 권한을 확인 -> READ 권한만 있는지 확인
        boolean isReadOnly = workspaceMemberRepository.existsByMemberIdAndWorkspaceIdAndMemberRole(userId, workspaceId, MemberRole.READ);
        if (isReadOnly) {
            throw new IllegalArgumentException("Not Found");
        }

        //이벤트 존재 여부 확인
        Event event = EventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Not Found"));

        //댓글 작성자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Not Found"));

        //댓글 생성
        Comment comment = new Comment(commentSaveRequest.getContent(), event, userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Not Found")));
        commentRepository.save(comment);

        CommentSaveResponse a = null;
        try {
            a = new CommentSaveResponse(comment.getId(),
                    comment.getContent(),
                    comment.getId(),
                    comment.getCreatedAt());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;

    }


    @Transactional
    public CommentUpdateResponse updateComment(CommentUpdateRequest commentUpdateRequest, Long commentId, Long userId, Long groupId, Long eventId) {
        //댓글 존재 여부 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Not Found"));
        ;

        //소모임 존재 여부 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Not Found"));

        //사용자가 소모임의 멤버인지 확인
        boolean isMember = group.getMembers().contains(userId) || group.getLeader().getId().equals(userId);
        if (!isMember) {
            throw new IllegalArgumentException("Not Found");
        }

        //댓글 작성자인지 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Not Found");

            //댓글 수정
            comment.setContent(commentUpdateRequest.getContent());
            commentRepository.save(comment);

        }
        return new CommentUpdateResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

        @Transactional
        public void deleteComment(Long commentId, Long userId, Long groupId, Long eventId) {
            //댓글 존재 여부 확인
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Not Found"));

            //소모임 존재 여부 확인
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Not Found"));
            ;

            //사용자가 소모임의 멤버인지 확인
            boolean isMember = group.getMembers().contains(userId) || group.getLeader().getId().equals(userId);
            if (!isMember) {
                throw new IllegalArgumentException("Not Found");
            }

            //삭제 권한 확인
            boolean isAuthor = comment.getUser().getId().equals(userId);
            boolean isLeader = group.getLeader().getId().equals(userId);
            if (!isAuthor || !isLeader) {
                throw new IllegalArgumentException("Not Found");
            }

            //댓글 삭제
            commentRepository.delete(comment);
        }
    }
}
