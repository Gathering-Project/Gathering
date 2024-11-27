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
public class OptionId implements Serializable {

    @Column(name = "poll_id", nullable = false)
    private Long pollId;

    @Column(name = "option_num", nullable = false)
    private int optionNum;

}
