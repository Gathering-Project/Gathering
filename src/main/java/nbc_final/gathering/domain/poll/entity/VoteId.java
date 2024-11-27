package nbc_final.gathering.domain.poll.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VoteId implements Serializable {

    @Column(name = "poll_id")
    private Long pollId;

    @Column(name = "user_id")
    private Long userId;
}
