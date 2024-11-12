package nbc_final.gathering.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    // 공통 응답
    INVALID_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "다시 시도해주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Search 관련 예외
    INVALID_SEARCH(HttpStatus.NOT_FOUND, "검색 조건이 필요합니다."),

    // 유저 관련 예외
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 사용자는 존재하지 않습니다."),
    WRONG_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다."),
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    VIOLATION_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호는 8자 이상이어야 하고, 숫자와 영문자를 포함해야 합니다."),
    USER_ALREADY_DELETED(HttpStatus.GONE, "이미 탈퇴한 회원입니다."),
    INVALID_ADMIN_TOKEN(HttpStatus.BAD_REQUEST, "관리자 인증이 확인되지 않았습니다"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호는 기존 비밀번호와 같을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다"),

    // OAuth 2.0 관련 예외
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    KAKAO_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "카카오 서버와의 통신 중 오류가 발생했습니다."),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다."),
    UNLINK_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "카카오 서버와의 통신 오류로 계정 연결 해제에 실패했습니다."),
    NO_PERMISSION_CHANGE_PASSWORD(HttpStatus.FORBIDDEN,"SNS 로그인 사용자는 비밀번호를 변경할 수 없습니다."),

    // 멤버 관련 예외
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "해당 멤버를 찾을 수 없습니다."),
    ALREADY_REQUESTED(HttpStatus.CONFLICT, "이미 가입 신청한 멤버입니다."),
    ALREADY_MEMBER(HttpStatus.CONFLICT, "이미 가입된 멤버입니다."),
    FULL_MEMBER(HttpStatus.CONFLICT, "소모임에 인원이 가득 찼습니다."),
    REJECTED_MEMBER(HttpStatus.CONFLICT, "소모임 참여가 거부되었습니다."),
    NOT_ACCEPTED_MEMBER(HttpStatus.NOT_ACCEPTABLE, "아직 가입 신청이 처리되지 않은 멤버입니다"),

    // 소모임 관련 예외
    NOT_FOUND_GATHERING(HttpStatus.NOT_FOUND, "해당 소모임을 찾을 수 없습니다."),
    NOT_FOUND_GROUP(HttpStatus.NOT_FOUND, "해당 소모임을 찾을 수 없습니다."),
    INVALID_TITLE(HttpStatus.NOT_FOUND, "타이틀을 입력해주세요."),
    INVALID_MAX_COUNT(HttpStatus.NOT_FOUND, "인원 수 입력이 잘못 되었습니다."),
    TOO_MANY_REQUSETS(HttpStatus.TOO_MANY_REQUESTS, "너무 많은 요청입니다. 나중에 다시 시도해주세요"),


    // 이벤트 관련 예외
    NOT_FOUND_EVENT(HttpStatus.NOT_FOUND, "해당 이벤트를 찾을 수 없습니다."),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "이미 참가한 이벤트입니다."),
    PARTICIPANT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "참가 가능한 인원이 초과되었습니다."),
    NOT_PARTICIPATED(HttpStatus.NOT_FOUND, "참가하지 않은 이벤트입니다."),
    EVENT_CREATOR_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "이벤트 생성자는 참가 취소를 할 수 없습니다."),
    EVENT_CREATOR_CANNOT_PARTICIPATE(HttpStatus.BAD_REQUEST, "이벤트 생성자는 참가할 수 없습니다."),
    INVALID_MAX_PARTICIPANTS(HttpStatus.BAD_REQUEST, "현재 참가자 수보다 적은 인원으로 설정할 수 없습니다."),
    ADMIN_CANNOT_PARTICIPATE(HttpStatus.BAD_REQUEST, "관리자 계정은 이벤트에 참가할 수 없습니다."),
    ADMIN_CANNOT_CANCEL_PARTICIPATION(HttpStatus.BAD_REQUEST,"관리자 계정은 이벤트 참가를 취소할 수 없습니다"),

    // 댓글 관련 예외
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),

    // 첨부파일 관련 예외
    NOT_FOUND_FILE(HttpStatus.NOT_FOUND, "해당 첨부파일을 찾을 수 없습니다."),
    NOT_SERVICE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    TOO_LARGE_SIZE_FILE(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기가 너무 큽니다."),
    NOT_USER_OR_GATHERING(HttpStatus.NOT_FOUND,"유저나 소모임의 정보가 없습니다."),
    ONLY_ONE_FILE(HttpStatus.BAD_REQUEST, "한 번에 하나의 파일만 업로드할 수 있습니다."),
    NOT_YET_CHOOOSE_FILE(HttpStatus.BAD_REQUEST,"파일이 선택되지 않았습니다."),

    // 장소 관련 예외
    NOT_FOUND_LOCATION(HttpStatus.NOT_FOUND, "관련된 장소를 찾을 수 없습니다."),

    // 락 관련 예외
    LOCK_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "락을 획득하는 데 실패했습니다."),
    TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 오류가 발생했습니다. 다시 시도해 주세요."),
    LOCK_ACQUISITION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus httpStatus;
    private final String message;
}
