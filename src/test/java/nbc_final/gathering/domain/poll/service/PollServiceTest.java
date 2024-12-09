package nbc_final.gathering.domain.poll.service;

import jakarta.transaction.Transactional;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.poll.dto.request.PollCreateRequestDto;
import nbc_final.gathering.domain.poll.dto.response.PollResponseDto;
import nbc_final.gathering.domain.poll.entity.Option;
import nbc_final.gathering.domain.poll.entity.Poll;
import nbc_final.gathering.domain.poll.repository.PollRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Transactional
class PollServiceTest {

    @Autowired
    PollService pollService;

    @Autowired
    PollRepository pollRepository;

    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234";
    private static final String DATABASE_NAME = "gathering_test";

    private final Long gatheringId1 = 1L;
    private final Long eventId1 = 1L;
    private final Long userId1 = 1L;
    private final Long pollId1 = 1L;

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withDatabaseName(DATABASE_NAME)
            .withInitScript("testcontainers/poll_data/init.sql");

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        dynamicPropertyRegistry.add("spring.datasource.username", () -> USERNAME);
        dynamicPropertyRegistry.add("spring.datasource.password", () -> PASSWORD);
//        dynamicPropertyRegistry.add("spring.jpa.hibernate.ddl-auto", () -> "create"); // init.sql이랑 동시에 쓰면 안 됨
    }

    @Nested
    class 투표_생성_관련_테스트 {

        @Test
        void 이벤트_내에서_멤버들간_의사결정을_위한_투표를_생성한다() {
            // given
            List<String> optionList = Arrays.asList("선택지1", "선택지2");
            PollCreateRequestDto requestDto = new PollCreateRequestDto("test2", optionList);

            // when
            PollResponseDto poll = pollService.createPoll(requestDto, gatheringId1, eventId1, userId1);

            // then
            assertThat(poll.getPollId()).isEqualTo(4L); // 초기 데이터로 Poll 3개 있음
            assertThat(poll.getGatheringId()).isEqualTo(gatheringId1);
            assertThat(poll.getEventId()).isEqualTo(eventId1);
            assertThat(poll.getAgenda()).isEqualTo(requestDto.getAgenda());
            assertThat(poll.getOptions()).size().isEqualTo(optionList.size());
        }

    }

    @Nested
    class 투표_참여_관련_테스트 {

        @Test
        void 투표에_참여하여_첫_표를_던지는_데에_성공한다() {

            // given
            Long userId2 = 2L; // 투표 미참가자
            int selectedOption = 1; // 두 번째 option은 현재 0표
            Poll poll = pollService.getPoll(pollId1);

            // when
            pollService.castVote(gatheringId1, eventId1, userId2, pollId1, selectedOption);

            // then
            assertThat(poll.getVotes().get(1).isDone()).isTrue();
            assertThat(poll.getOptions().get(selectedOption).getVoteCount()).isEqualTo(1); // 두 번째 선택지 득표 수 1 증가
            assertThat(poll.getVotes().get(1).getSelectedOption()).isEqualTo(selectedOption);
            assertThat(poll.getVotes().get(1).getUser().getId()).isEqualTo(userId2);
            assertThat(poll.getVotes().get(1).getPoll().getId()).isEqualTo(pollId1);
            assertThat(poll.getVotes().get(1).getEvent().getId()).isEqualTo(eventId1);
            assertThat(poll.getVotes().get(1).getGathering().getId()).isEqualTo(gatheringId1);
        }

        @Test
        void 이미_투표한_상태에서_같은_선택지를_한_번_더_고르면_투표가_취소된다() {

            // given
            Poll poll1 = pollService.getPoll(pollId1);
            int selectedOption = 1;

            // when
            pollService.castVote(gatheringId1, eventId1, userId1, pollId1, selectedOption); // 득표 수 + 1
            pollService.castVote(gatheringId1, eventId1, userId1, pollId1, selectedOption); // 득표 수 - 1

            // then
            assertThat(poll1.getOptions().get(selectedOption).getVoteCount()).isEqualTo(0); // 기존 1에서 -1
            assertThat(poll1.getVotes().get(0).isDone()).isFalse(); // 투표 미완료 상태로 롤백
        }

        @Test
        void 이미_투표한_상태에서_다른_선택지를_선택하면_기존_선택지가_취소되고_새로운_선택지가_선택된다() {
            // given
            Poll poll1 = pollService.getPoll(pollId1);

            // when
            // 이미 투표되어 있는 상황에서 다른 선택지 투표
            pollService.castVote(gatheringId1, eventId1, userId1, pollId1, 1); // 옵션1 득표 수 -1 && 옵션2 득표 수 + 1

            assertThat(poll1.getOptions().get(0).getVoteCount()).isEqualTo(0);
            assertThat(poll1.getOptions().get(1).getVoteCount()).isEqualTo(1);
            assertThat(poll1.getVotes().get(0).isDone()).isTrue();
        }

        @Test
        void 투표를_취소한_상태에서_직전과_다른_선택지를_투표하면_득표_수가_반영된다() {
            // given
            Poll poll1 = pollService.getPoll(pollId1);

            // when
            pollService.castVote(gatheringId1, eventId1, userId1, pollId1, 0); // 옵션1(초기 데이터) 투표 취소
            pollService.castVote(gatheringId1, eventId1, userId1, pollId1, 1); // 옵션2 득표 수 + 1

            // then
            assertThat(poll1.getOptions().get(0).getVoteCount()).isEqualTo(0); // 옵션1 득표 : 0
            assertThat(poll1.getOptions().get(1).getVoteCount()).isEqualTo(1); // 옵션2 득표 : 1
            assertThat(poll1.getVotes().get(0).isDone()).isTrue();
        }

        @Test
        void 투표를_취소한_상태에서_직전과_같은_선택지를_투표하면_득표_수가_반영된다() {
            // given
            Long userId1 = 1L; // 투표 미참가자
            Poll poll1 = pollService.getPoll(pollId1);

            // when
            pollService.castVote(gatheringId1, eventId1, userId1, pollId1, 0); // 투표 취소
            pollService.castVote(gatheringId1, eventId1, userId1, pollId1, 0); // 다시 똑같은 선택지 투표

            // then
            assertThat(poll1.getOptions().get(0).getVoteCount()).isEqualTo(1);
            assertThat(poll1.getVotes().get(0).isDone()).isTrue();
        }
    }

    @Nested
    class 투표_삭제_관련_테스트 {

        @Test
        void 투표_생성자가_투표를_삭제한다() {

            // when
            pollService.deletePoll(gatheringId1, eventId1, pollId1, userId1);

            // then
            Optional<Poll> deletedPoll = pollRepository.findById(pollId1);
            assertThat(deletedPoll).isNotPresent();
        }
    }

    @Nested
    class 투표_마감_관련_테스트 {

        @Test
        void 이벤트_주최자_및_투표_생성자가_진행_중인_투표를_마감한다() {

            // given
            Poll poll = pollService.getPoll(pollId1);

            // when & then
            assertThatCode(
                    () -> pollService.finishPoll(gatheringId1, eventId1, pollId1, userId1))
                    .doesNotThrowAnyException();
            assertThat(poll.isActive()).isFalse();
        }

        @Test
        void 이벤트_개최자가_아닌_참가자가_진행_중인_투표를_마감하려고_하면_EVENT_CREATOR_ONLY_가_발생한다() {

            // given
            Poll poll = pollService.getPoll(pollId1);
            Long userId2 = 2L; // 유저 2는 이벤트 개최자가 아니라 참가자

            // when & then
            assertThatThrownBy(
                    () -> pollService.finishPoll(gatheringId1, eventId1, pollId1, userId2)
            )
                    .isInstanceOf(ResponseCodeException.class)
                    .hasMessageContaining(ResponseCode.EVENT_CREATOR_ONLY.getMessage());
        }

        @Test
        void 이벤트_개최자가_아닌_미참가자가_진행_중인_투표를_마감하려고_하면_EVENT_CREATOR_ONLY_가_발생한다() {

            // given
            Poll poll = pollService.getPoll(pollId1);
            Long userId3 = 3L; // 유저 2는 이벤트 개최자가 아니라 참가자

            // when & then
            assertThatThrownBy(
                    () -> pollService.finishPoll(gatheringId1, eventId1, pollId1, userId3)
            )
                    .isInstanceOf(ResponseCodeException.class)
                    .hasMessageContaining(ResponseCode.EVENT_CREATOR_ONLY.getMessage());
        }

        @Test
        void 이미_마감된_투표를_마감하려고_하면_DEACTIVATED_POLL_가_발생한다() {

            // given
            Long pollId3 = 3L;
            Poll deactivedPoll = pollService.getPoll(pollId3); // 이미 마감된 투표
            Long userId1 = 1L; // 유저 1은 이벤트 개최자

            // when & then
            assertThatThrownBy(
                    () -> pollService.finishPoll(gatheringId1, eventId1, pollId3, userId1)
            )
                    .isInstanceOf(ResponseCodeException.class)
                    .hasMessageContaining(ResponseCode.DEACTIVATED_POLL.getMessage());
        }
    }

    @Nested
    class 투표_조회_관련_테스트 {

        @Test
        void 이벤트_멤버가_이벤트_내의_특정_투표를_조회한다() {

            // when
            PollResponseDto poll = pollService.getPoll(gatheringId1, eventId1, userId1, pollId1); // 초기 데이터 Poll

            // then
            assertThat(poll.getPollId()).isEqualTo(pollId1);
            assertThat(poll.getGatheringId()).isEqualTo(gatheringId1);
            assertThat(poll.getGatheringId()).isEqualTo(eventId1);
            assertThat(poll.getAgenda()).isEqualTo("test1"); // test1: 초기 데이터 투표 agenda
            assertThat(poll.getOptions()).size().isEqualTo(3); // 초기 데이터 투표 선택지  수
        }

        @Test
        void 이벤트_멤버가_아니라면_이벤트_내의_단건_투표_조회에_실패한다() {

            // given
            Long userId3 = 3L; // 유저 3는 이벤트 1의 멤버 X

            // when & then
            assertThatThrownBy(
                    () -> pollService.getPoll(gatheringId1, eventId1, userId3, pollId1))
                    .isInstanceOf(ResponseCodeException.class)
                    .hasMessageContaining(ResponseCode.NOT_PARTICIPATED.getMessage());
        }

        @Test
        void 이벤트_멤버가_이벤트_내의_모든_투표들을_조회한다() {

            // given
            int page = 1;
            int size = 10;

            // when
            Page<PollResponseDto> polls = pollService.getPolls(gatheringId1, eventId1, userId1, page, size);

            // then (초기 데이터: POll 3개)
            assertThat(polls).isNotNull();
            assertThat(polls.getTotalPages()).isEqualTo(1);
            assertThat(polls.getContent()).hasSize(3);
            assertThat(polls.getContent().get(0).getAgenda()).isEqualTo("test1");
            assertThat(polls.getContent().get(1).getAgenda()).isEqualTo("test2");
            assertThat(polls.getContent().get(2).getAgenda()).isEqualTo("test3");
        }

        @Test
        void 이벤트_멤버가_아니라면_이벤트_내의_다건_투표_조회에_실패한다() {

            // given
            Long userId3 = 3L; // 유저 3는 이벤트 1의 멤버 X

            // when & then
            assertThatThrownBy(
                    () -> pollService.getPoll(gatheringId1, eventId1, userId3, pollId1))
                    .isInstanceOf(ResponseCodeException.class)
                    .hasMessageContaining(ResponseCode.NOT_PARTICIPATED.getMessage());
        }
    }
}