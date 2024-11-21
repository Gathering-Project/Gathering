package nbc_final.gathering.common.alarmconfig;

import lombok.*;

import java.time.LocalDateTime;

public class AlarmDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmMessageReq {
        private Long userId;
        private String message;

        public AlarmMessage toEntity() {
            return AlarmMessage.builder()
                    .userId(userId)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor // 기본 생성자 추가
    public static class AlarmMessageRes {
        private Long userId;
        private String message;

        // AlarmMessageRes 객체를 생성할 때 날짜 정보는 제외하고 생성
        public static AlarmMessageRes createRes(AlarmMessage alarmMessage) {
            return AlarmMessageRes.builder()
                    .userId(alarmMessage.getUserId())
                    .message(alarmMessage.getMessage())
                    .build();
        }

        @Override
        public String toString() {
            return "AlarmMessageRes{" +
                    "userId=" + userId +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
