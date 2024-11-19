package nbc_final.gathering.domain.payment.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PayStatus {
    PENDING, READY, PAID, FAILED, CANCELED;

    @JsonValue
    public String toJson() {
        return this.name();
    }
}
