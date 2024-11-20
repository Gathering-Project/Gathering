package nbc_final.gathering.common.alarmconfig;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("alarm_messages")
public class AlarmMessage {
    @Id
    private String id;
    private Long userId;
    private String message;
    private LocalDateTime createdAt;
}
