package nbc_final.gathering.domain.ad.entity;

public enum AdStatus {
    PENDING,  // 결제 완료 후 활성화 대기 상태
    ACTIVE,   // 광고가 활성화,
    EXPIRED,
    CANCELED  // 결제 취소
}
