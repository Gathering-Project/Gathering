package nbc_final.gathering.domain.gathering.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc_final.gathering.common.config.redis.RedisLimiter;
import nbc_final.gathering.common.dto.AuthUser;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.common.kafka.util.KafkaNotificationUtil;
import nbc_final.gathering.domain.gathering.dto.request.GatheringRequestDto;
import nbc_final.gathering.domain.gathering.dto.response.GatheringResponseDto;
import nbc_final.gathering.domain.gathering.dto.response.GatheringWithCountResponseDto;
import nbc_final.gathering.domain.gathering.entity.Gathering;
import nbc_final.gathering.domain.gathering.repository.GatheringRepository;
import nbc_final.gathering.domain.member.entity.Member;
import nbc_final.gathering.domain.member.enums.MemberRole;
import nbc_final.gathering.domain.member.enums.MemberStatus;
import nbc_final.gathering.domain.member.repository.MemberRepository;
import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import nbc_final.gathering.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
//    private final KafkaNotificationUtil kafkaNotificationUtil;


    private static final String TODAY_RANKING_KEY = "todayGatheringRanking";
    private final RedisLimiter redisLimiter;

    // 제목으로 모임을 검색하고 찾지 못한 경우 NOT_FOUND_GROUP 예외 처리
    @Transactional(readOnly = true)
    public Gathering getGatheringByTitle(String title) {
        return gatheringRepository.findByTitle(title)
                .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GROUP));
    }

    // 제목을 기준으로 모임을 검색하여 결과를 반환
    @Transactional(readOnly = true)
    public List<Gathering> searchGatheringsByTitle(String title) {
        return gatheringRepository.searchGatheringsByTitle(title);
    }

    // 지역을 기준으로 모임을 검색하고 검색 소요 시간을 로그로 기록
    @Transactional(readOnly = true)
    public List<Gathering> searchGatheringsByLocation(String location) {
        long startTime = System.currentTimeMillis();
        List<Gathering> results = gatheringRepository.searchGatheringsByLocation(location);
        long endTime = System.currentTimeMillis();
        System.out.println("지역 검색 소요 시간: " + (endTime - startTime) + "ms");
        return results;
    }

    // 제목과 지역을 기준으로 모임을 검색하고 검색 소요 시간을 로그로 기록
    @Transactional(readOnly = true)
    public List<Gathering> searchGatheringsByTitleAndLocation(String title, String location) {
        long startTime = System.currentTimeMillis();
        List<Gathering> results = gatheringRepository.searchGatheringsByTitleAndLocation(title, location);
        long endTime = System.currentTimeMillis();
        System.out.println("이름 및 지역 검색 소요 시간: " + (endTime - startTime) + "ms");
        return results;
    }

    // 그룹 생성 로직
        @Transactional
        public GatheringResponseDto createGroup (AuthUser authUser, GatheringRequestDto gatheringRequestDto){
            // 유저 조회
            User user = findUserById(authUser);

            // 그룹 생성
            Gathering savedGathering = new Gathering(user.getId(), gatheringRequestDto.getTitle(),
                    gatheringRequestDto.getDescription(),
                    1, gatheringRequestDto.getGatheringMaxCount(),
                    BigDecimal.valueOf(50), gatheringRequestDto.getLocation()
            );

            // 주최자 추가
            Member member = new Member(user, savedGathering, MemberRole.HOST, MemberStatus.APPROVED);
            savedGathering.getMembers().add(member);

            // 그룹 저장
            gatheringRepository.save(savedGathering);
            memberRepository.save(member);

//            kafkaNotificationUtil.notifyHostMember(user.getId(), "새로운 소모임이 생성되었습니다.");

            return GatheringResponseDto.of(savedGathering);
        }

        // 소모임 단 건 조회 로직
        public GatheringWithCountResponseDto getGathering (AuthUser authUser, Long gatheringId){

            // 레이트 리미팅 체크 (10초 동안 최대 5번 허용)
            boolean isAllowed = redisLimiter.isAllowed(authUser, "getGathering" + gatheringId, 10, 5);

            if (!isAllowed) {
                throw new ResponseCodeException(ResponseCode.TOO_MANY_REQUSETS);
            }

            // 소모임 조회
            Gathering gathering = findGatheringById(gatheringId);
            // Redis Set 의 Key 설정
            String todayGatheringViewSetKey = "todayGatheringSet:" + gatheringId;

            // 유저 ID를 Set 에 추가하고, 반환된 값으로 추가 성공 여부 확인
            redisTemplate.opsForSet().add(todayGatheringViewSetKey, authUser.getUserId());

            // 조회수를 Set의 크기로 계산
            Long todayGatheringViewCount = redisTemplate.opsForSet().size(todayGatheringViewSetKey);

            // 가장 인기 있는 Top3 카드 업데이트
            updateTopGatheringTitles(gatheringId, authUser);

            // Dto 반환
            return new GatheringWithCountResponseDto(gathering, todayGatheringViewCount);
        }

        // 인기 소모임 Top3 조회 ( redis )
        public Map<String, Integer> getTopViewGatheringList () {
            Set<ZSetOperations.TypedTuple<Object>> topGatheringsWithScores =
                    redisTemplate.opsForZSet().reverseRangeWithScores(TODAY_RANKING_KEY, 0, 2);

            return topGatheringsWithScores.stream()
                    .collect(Collectors.toMap(
                            tuple -> tuple.getValue().toString(), // title
                            tuple -> tuple.getScore().intValue()  // 조회수 ( score )
                    ));
        }

        // 유저가 가입한 소모임 다 건 조회 로직
        public List<GatheringResponseDto> getAllGatherings (AuthUser authUser){
            String cacheKey = "userGathering:" + authUser.getUserId();
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            ObjectMapper objectMapper = new ObjectMapper();

            // Redis에서 캐시 조회
            Object cachedData = valueOperations.get(cacheKey);
            if (cachedData != null) {
                // JSON 형식으로 저장된 데이터를 List<GatheringResponseDto>로 역직렬화
                return objectMapper.convertValue(cachedData, new TypeReference<List<GatheringResponseDto>>() {
                });
            }

            List<Member> members = memberRepository.findByUserId(authUser.getUserId());

            // 각 Member가 참여한 소모임을 모두 조회
            List<GatheringResponseDto> gatheringResponses = new ArrayList<>();

            for (Member member : members) {
                Gathering gathering = findGatheringByMember(member);
                if (gathering != null && (member.getStatus() == MemberStatus.APPROVED)) {
                    gatheringResponses.add(GatheringResponseDto.of(gathering));
                }
            }

            // Redis에 캐싱
            valueOperations.set(cacheKey, gatheringResponses, 10, TimeUnit.MINUTES);

            // DTO List 변환
            return gatheringResponses;
        }

        // 소모임 수정 로직
        @Transactional
        public GatheringResponseDto updateGathering (AuthUser authUser, Long gatheringId, GatheringRequestDto
        gatheringRequestDto){
            // 소모임 조회
            Gathering gathering = findGatheringById(gatheringId);

            // Host인지 권한 체크
            validateHostPermission(authUser, gathering);
            // MaxCount 체크
            validateMaxCount(gatheringRequestDto, gathering);

            // 소모임 정보 업데이트
            gathering.updateDetails(gatheringRequestDto.getTitle(),
                    gatheringRequestDto.getDescription(),
                    gatheringRequestDto.getGatheringMaxCount(),
                    gatheringRequestDto.getLocation());

            // 소모임 저장
            gatheringRepository.save(gathering);

//            kafkaNotificationUtil.notifyAllMembers(gatheringId, "소모임이 수정되었습니다.");

//            // 승인된 멤버들에게 알림 전송
//            List<Member> approvedMembers = memberRepository.findAllByGatheringId(gatheringId).stream()
//                    .filter(member -> member.getStatus() == MemberStatus.APPROVED)
//                    .collect(Collectors.toList());
//
//            approvedMembers.forEach(member -> {
//                kafkaNotificationUtil.notifyGuestMember(member.getUser().getId(), gathering.getTitle() + " 소모임이 수정되었습니다.");
//            });


            // 업데이트된 정보를 DTO로 반환
            return GatheringResponseDto.of(gathering);
        }

        @Transactional
        public void deleteGathering (AuthUser authUser, Long gatheringId){
            // 소모임 조회
            Gathering gathering = findGatheringById(gatheringId);

            // 사용자 권한 검증 (HOST or ADMIN)
            validateHostAndAdminPermission(authUser, gathering);

            // 승인된 멤버들을 조회
            List<Member> approvedMembers = memberRepository.findAllByGatheringAndStatus(gathering, MemberStatus.APPROVED);

//            // 승인된 멤버들에게 알림 전송
//            approvedMembers.forEach(member -> {
//                kafkaNotificationUtil.notifyGuestMember(member.getUser().getId(),
//                        gathering.getTitle() + " 소모임이 삭제되었습니다.");
//            });

            // 호스트 조회
            List<Member> hostMembers = memberRepository.findAllByGatheringId(gatheringId)
                    .stream()
                    .filter(member -> member.getRole() == MemberRole.HOST)
                    .collect(Collectors.toList());

//            // 호스트에게 알림 전송
//            hostMembers.forEach(host -> {
//                kafkaNotificationUtil.notifyHostMember(host.getUser().getId(),
//                        gathering.getTitle() + " 소모임이 삭제되었습니다.");
//            });

            // 모임과 관련된 멤버 삭제
            memberRepository.deleteByGathering(gathering); // 모임에 속한 멤버를 삭제하는 메서드

            // 모임 삭제
            gatheringRepository.delete(gathering);
        }

        ////////////////////// 에러 처리를 위한 메서드 ///////////////////////

        private User findUserById (AuthUser authUser){
            return userRepository.findById(authUser.getUserId()).orElseThrow(
                    () -> new ResponseCodeException(ResponseCode.NOT_FOUND_USER));
        }

        private List<Member> findMembersByUserId (AuthUser authUser){
            List<Member> members = memberRepository.findByUserId(authUser.getUserId());

            if (members.isEmpty()) {
                throw new ResponseCodeException(ResponseCode.NOT_FOUND_MEMBER);
            }
            return members;
        }

        private Gathering findGatheringByMember (Member member){
            return gatheringRepository.findByMembers(member).orElseThrow(
                    () -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING)
            );
        }

        private Gathering findGatheringById (Long gatheringId){
            return gatheringRepository.findById(gatheringId).orElseThrow(
                    () -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING)
            );
        }

        private void validateHostAndAdminPermission (AuthUser authUser, Gathering gathering){
            // 유저 조회
            User user = findUserById(authUser);
            Member member = null;
            // 유저가 어드민이라면 통과
            if (!(user.getUserRole() == UserRole.ROLE_ADMIN)) {
                // 유저와 소모임을 기반으로 멤버 조회
                member = memberRepository.findByUserAndGathering(user, gathering).orElseThrow(
                        () -> new ResponseCodeException(ResponseCode.FORBIDDEN, "권한이 없습니다.")
                );
            }
            // 호스트가 아니거나, 어드민이 아니면 권한 X
            if (!((member == null || (member.getRole() == MemberRole.HOST)))) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }
        }

        private void validateHostPermission (AuthUser authUser, Gathering gathering){
            // 유저 조회
            User user = findUserById(authUser);
            // 유저와 소모임을 기반으로 멤버 조회
            Member member = memberRepository.findByUserAndGathering(user, gathering).orElseThrow(
                    () -> new ResponseCodeException(ResponseCode.FORBIDDEN, "권한이 없습니다.")
            );
            // 호스트가 아니면 권한 X
            if ((member.getRole() != MemberRole.HOST)) {
                throw new ResponseCodeException(ResponseCode.FORBIDDEN);
            }
        }

        private static void validateMaxCount (GatheringRequestDto gatheringRequestDto, Gathering gathering){
            // 변경하려는 최대 인원이 소모임의 현재 인원보다 적은 경우
            if (gathering.getGatheringCount() > gatheringRequestDto.getGatheringMaxCount()) {
                throw new ResponseCodeException(ResponseCode.INVALID_MAX_COUNT);
            }
        }

        ////////////////////// Redis 조회 메서드 ///////////////////////

        // 소모임 제목을 Redis 에서 가져오고, 없으면 데이터베이스에서 조회 후 Redis 에 저장
        private String getGatheringTitle (Long gatheringId){
            String gatheringTitleKey = "gatheringTitle:" + gatheringId;
            String title = (String) redisTemplate.opsForValue().get(gatheringTitleKey);

            // Redis에 제목이 없을 경우 DB에 조회 후 캐싱
            if (title == null) {
                Gathering gathering = gatheringRepository.findById(gatheringId)
                        .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));
                title = gathering.getTitle();

                // 24시간 후 만료되게 Redis에 캐싱
                redisTemplate.opsForValue().set(gatheringTitleKey, title, 24, TimeUnit.HOURS);
            }

            return title;
        }

        // Gathering DB에서 조회
        public Gathering findGathering (Long gatheringId){
            Gathering gathering = gatheringRepository.findById(gatheringId)
                    .orElseThrow(() -> new ResponseCodeException(ResponseCode.NOT_FOUND_GATHERING));
            return gathering;
        }

        // Gathering DB에 저장
        public void saveGathering (Gathering gathering){
            gatheringRepository.save(gathering);
        }

        // TOP3 Gathering 업데이트 메서드
        private void updateTopGatheringTitles (Long gatheringId, AuthUser authUser){

            Long userId = authUser.getUserId();
            // Key : gatheringViewSet{gatheringId}
            String todayGatheringViewSetKey = "todayGatheringViewSet" + gatheringId;

            // 유저 ID를 문자열로 변환하여 Redis Set에 추가 (중복 조회 방지), Set에 추가된 요소가 새 값일 때만 Boolean true로 처리
            Long isNewViewerCount = redisTemplate.opsForSet().add(todayGatheringViewSetKey, userId.toString());
            Boolean isNewViewer = isNewViewerCount != null && isNewViewerCount > 0;

            // 유저가 처음 조회한 경우에만 조회수를 증가시킴
            if (Boolean.TRUE.equals(isNewViewer)) {
                // Gathering 제목 가져오기 (Redis 사용)
                String gatheringTitle = getGatheringTitle(gatheringId);

                // Gathering 제목과 조회수를 Sorted Set에 추가 (score 증가)
                redisTemplate.opsForZSet().incrementScore(TODAY_RANKING_KEY, gatheringTitle, 1);
            }
            // 상위 3개 카드 제목만 유지
            maintainTopGathering();
        }

        // Top Gathering 제목 가져오는 메서드
        private void maintainTopGathering () {
            // 전체 소모임 수 가져오기
            Long size = redisTemplate.opsForZSet().zCard(TODAY_RANKING_KEY);

            // 상위 3개를 제외한 나머지 제거 (3개 이하일 때는 아무것도 제거하지 않음)
            if (size == null || size > 3) {
            redisTemplate.opsForZSet().removeRange(TODAY_RANKING_KEY, 3, size - 1);
        }
    }

}

