package nbc_final.gathering.domain.ad.entity;

public enum AdStatus {
    PENDING,  // 광고 신청 후 결제 대기 상태
    PAID,    // 결제가 완료된 상태
    ACTIVE,   // 현재 노출 중
    EXPIRED,  // 노출 기간 종료
    CANCELED, // 결제승인 후 취소
    FAILED    // 결제 실패
}
