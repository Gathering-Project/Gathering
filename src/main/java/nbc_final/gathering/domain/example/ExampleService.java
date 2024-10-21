package nbc_final.gathering.domain.example;

import nbc_final.gathering.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {

    // 반환 데이터 존재할 시
    public String example1() {
        return "반갑습니다람쥐";
    }

    public ExampleDto example11(ExampleDto dto) {
        return dto;
    }

    // 반환 데이터 존재하지 않을 시
    public void example2() {

    }

    // 에러 발생 예시
    public void example3() {
        throw new ExampleException(ErrorCode.EXAMPLE_EXCEPTION);
    }
}
