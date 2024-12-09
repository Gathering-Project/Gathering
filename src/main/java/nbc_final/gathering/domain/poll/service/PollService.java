package nbc_final.gathering.domain.poll.service;

import io.lettuce.core.output.ScanOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.event.entity.Event;
import nbc_final.gathering.domain.event.repository.EventRepository;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.poll.dto.request.PollCreateRequestDto;
import nbc_final.gathering.domain.poll.dto.response.PollResponseDto;
import nbc_final.gathering.domain.poll.entity.Option;
import nbc_final.gathering.domain.poll.entity.Poll;
import nbc_final.gathering.domain.poll.entity.Vote;
import nbc_final.gathering.domain.poll.repository.OptionRepository;
import nbc_final.gathering.domain.poll.repository.PollRepository;
import nbc_final.gathering.domain.poll.repository.VoteRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PollService {

    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;
    private final GatheringRepository gatheringRepository;
    private final EventRepository eventRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository;

    // 투표 생성
    @Transactional
    public PollResponseDto createPoll(PollCreateRequestDto requestDto, Long gatheringId, Long EventId, Long userId) {

        // 소모임 확인
        Gathering gathering = getGathering(gatheringId);

        // 이벤트 확인
        Event event = getEvent(EventId);

        // 투표 개설은 이벤트 생성 유저만 가능하기 때문에 이벤트 생성한 유저가 맞는지 확인
        validateEventHost(userId, event);

        // 투표 엔티티 생성 및 저장
        Poll poll = Poll.createPoll(requestDto.getAgenda(), gathering, event, requestDto.getOptions());
        pollRepository.save(poll);

        // DTO 변환
        return PollResponseDto.of(poll);
    }

    // 투표 참여(선택지 선택, 취소, 선택지 변경)
    @Transactional
    public void castVote(Long gatheringId, Long eventId, Long userId, Long pollId, int selectedOption) {

        // 소모임 존재 확인
        Gathering gathering = getGathering(gatheringId);

        // 이벤트 존재 확인
        Event event = getEvent(eventId);

        // 이벤트 참가자인지 확인
        isParticipated(userId, event);

        // 투표 존재 확인
        Poll poll = getPoll(pollId);

        // 유저 가져오기
        User user = getUser(userId);

        // 종료되지 않고 진행 중인 투표가 맞는지 확인
        validatePollActive(poll);


        /* 아래 로직은 주석 가독성 위해 공백으로 구분함*/

        Optional<Vote> optionalVote = getVoteIfExists(user, poll); // 이미 투표했는지 여부 확인하고 있으면 표 가져오기
        Option option = optionRepository.findByPollAndOptionNum(poll, selectedOption); // 해당 유저가 표 주고 싶은 선택지

        // 아직 투표하지 않은 상태라면
        if (!optionalVote.isPresent()) {
            Vote vote = Vote.castVote(poll, user, gathering, event, selectedOption); // 표 엔티티 생성
            poll.addVotes(vote);

            option.incrementVoteCount(); // 표 주고 싶은 선택지 득표 수 + 1
            voteRepository.save(vote); // 소모임, 이벤트, 유저, 현재 투표 참여 여부, 현재 선택지 등 저장

            // 이미 투표했지만 투표를 취소하거나 선택지를 수정한다면
        } else {
            Vote vote = optionalVote.get(); // 이미 투표 참여했었기 때문에 표 정보 존재
            if (vote.getSelectedOption() == selectedOption) { // 이전에 투표했었는데 같은 선택지를 한 번 더 선택한 상황

                if (vote.isDone()) { // 선택지가 선택되어 있어 이미 투표 완료된 상태이면 투표 취소
                    cancelVote(option, vote); // 기존 선택지 득표 수 - 1 하고 해당 유저 투표 아직 미완료 상태로 초기화

                } else { // 이전에 투표에는 참여했었지만 취소해서 투표 미참여 상태 및 아직 선택지가 없는 상태에서 직전과 똑같은 선택지 재투표하면
                    option.incrementVoteCount(); // 선택지 득표 수 + 1
                    vote.updateStatus(); // 투표 완료된 상태로 업데이트
                }

            } else { // 이미 투표참여했었는데 바로 직전과 다른 선택지 고르는 상황이라면
                Option existingOption = optionRepository.findByPollAndOptionNum(poll, vote.getSelectedOption()); // 직전 선택지

                if (vote.isDone()) { // 이미 선택 중인 선택지가 있어서 투표 완료 상태
                    option.incrementVoteCount(); // 표 주고 싶은 새로운 선택지 득표 수 + 1
                    existingOption.decreaseVoteCount(); // 기존 선택 중이던 선택지 득표 수 - 1
                    vote.updateSelectedOption(selectedOption); // 표 선택지 정보 수정

                } else { // 현재 선택 중인 선택지는 없고 취소 직전과 다른 선택지 고른 상태
                    option.incrementVoteCount(); // 표 주고 싶은 새로운 선택지 득표 수 + 1
                    vote.updateSelectedOption(selectedOption); // 표 선택지 정보 수정
                    vote.updateStatus(); // 투표 완료 상태로 업데이트
                }
            }
            voteRepository.save(vote);
        }

    }

    // 투표 현황 조회(단건 조회)
    public PollResponseDto getPoll(Long gatheringId, Long eventId, Long userId, Long pollId) {

        // 소모임 존재 확인
        Gathering gathering = getGathering(gatheringId);

        // 이벤트 존재 확인
        Event event = getEvent(eventId);

        // 이벤트 참가자인지 확인
        isParticipated(userId, event);

        // 투표 존재 확인
        Poll poll = getPoll(pollId);

        return PollResponseDto.of(poll);
    }

    // 이벤트 내 모든 투표 조회
    public Page<PollResponseDto> getPolls(Long gatheringId, Long eventId, Long userId, int page, int size) {

        // 소모임 존재 확인
        Gathering gathering = getGathering(gatheringId);

        // 이벤트 존재 확인
        Event event = getEvent(eventId);

        // 이벤트 참가자인지 확인
        isParticipated(userId, event);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Poll> allPoll = pollRepository.findAllByEventId(eventId, pageable); // ( Option을 조회하기 위해 페이지에 존재하는 Poll 숫자만큼 N + 1 문제 발생) => BatchSize 설정으로 해결
//        Page<Poll> allPoll = pollRepository.findAllWithOptionsByEventId(eventId, pageable); // Fetch Join으로 N + 1 문제 해결 => OOM 발생 가능성으로 보류
        return allPoll.map(PollResponseDto::of);
    }

    // 투표 마감
    @Transactional
    public void finishPoll(Long gatheringId, Long eventId, Long pollId, Long userId) {

        // 소모임 존재 확인
        Gathering gathering = getGathering(gatheringId);

        // 이벤트 존재 확인
        Event event = getEvent(eventId);

        // 투표 존재 확인
        Poll poll = getPoll(pollId);

        // 이벤트 개최자가 맞는지 확인
        validateEventHost(userId, event);

        // 종료되지 않고 진행 중인 투표가 맞는지 확인
        validatePollActive(poll);

        // 투표 종료(상태 비활성화)
        poll.finishPoll();
    }

    // 투표 삭제
    @Transactional
    public void deletePoll(Long gatheringId, Long eventId, Long pollId, Long userId) {
        // 소모임 존재 확인
        Gathering gathering = getGathering(gatheringId);

        // 이벤트 존재 확인
        Event event = getEvent(eventId);

        // 투표 안건 존재 확인
        Poll poll = getPoll(pollId);

        // 이벤트 개최자가 맞는지 확인
        validateEventHost(userId, event);

        // 투표 삭제
        pollRepository.delete(poll);
    }


    // ----------- extracted method ------------- //

    // 투표 취소
    public void cancelVote(Option option, Vote vote) {
        option.decreaseVoteCount(); // 투표 취소로 기존 선택지 득표 - 1
        vote.resetStatus(); // 투표 안한 상태로 초기화
    }

    // 이벤트 개최자인지 확인
    public void validateEventHost(Long userId, Event event) {
        if (!event.getUser().getId().equals(userId)) {
            throw new ResponseCodeException(ResponseCode.EVENT_CREATOR_ONLY);
        }
    }

    // 이벤트 참가자인지 확인
    public void isParticipated(Long userId, Event event) {
        if (event.getParticipants().stream()
                .noneMatch(participant -> participant.getUser().getId().equals(userId))) {
            throw new ResponseCodeException(ResponseCode.NOT_PARTICIPATED);
        }
    }

    // 종료되지 않고 진행 중인 투표인지 확인
    public void validatePollActive(Poll poll) {
        if (!poll.isActive()) {
            throw new ResponseCodeException(ResponseCode.DEACTIVATED_POLL);
        }
    }

    // 이미 투표했는지 여부 확인하고 있으면 표 가져오기
    public Optional<Vote> getVoteIfExists(User user, Poll poll) {
        return poll.getVotes().stream()
                .filter(vote -> vote.getUser().getId().equals(user.getId()))
                .findFirst();
    }

    // 유저 가져오기
    public User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
        return user;
    }

    // 투표 존재 확인
    public Poll getPoll(Long pollId) {
        return pollRepository.findById(pollId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_POLL));
    }

    // 이벤트 확인
    public Event getEvent(Long EventId) {
        Event event = eventRepository.findById(EventId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_EVENT));
        return event;
    }

    // 소모임 확인
    public Gathering getGathering(Long gatheringId) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));
        return gathering;
    }

}
