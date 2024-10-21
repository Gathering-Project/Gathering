package nbc_final.gathering.domain.example;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;

    // 반환 데이터 존재 O
    @GetMapping("/ex1")
    public ResponseEntity<ApiResponse<String>> example1() {
        String res = exampleService.example1();
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }
    /*
    {
    "statusCode": 200,
    "message": "요청이 성공적으로 처리되었습니다.",
    "data": "반갑습니다람쥐"
    }
     */

    // 반환 데이터 존재 O
    @GetMapping("/ex1-1")
    public ResponseEntity<ApiResponse<ExampleDto>> example1(@RequestBody ExampleDto dto) {
        ExampleDto res = exampleService.example11(dto);
        return ResponseEntity.ok(ApiResponse.createSuccess(res));
    }





    // 반환 데이터 존재 X
    @GetMapping("/ex2")
    public ResponseEntity<ApiResponse<Void>> example2() {
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }
    /*
    {
    "statusCode": 200,
    "message": "요청이 성공적으로 처리되었습니다.",
    "data": null
    }
    */

    // 에러 발생 예시
    @GetMapping("/ex3")
    public ResponseEntity<ApiResponse<Void>> example3() {
        exampleService.example3(); // 에러 발생
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }
    /*
    {
    "statusCode": 400,
    "message": "예시 예외입니당.",
    "data": null
    }
     */
}
