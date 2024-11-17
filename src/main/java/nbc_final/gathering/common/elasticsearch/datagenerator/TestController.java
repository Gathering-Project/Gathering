package nbc_final.gathering.common.elasticsearch.datagenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final DataGenerator dataGenerator;

    @PostMapping("/generate")
    public ResponseEntity<String> generateRandomData(@RequestParam int count) {
        if (count > 100) {
            return ResponseEntity.badRequest().body("최대 100개의 데이터만 생성할 수 있습니다.");
        }

        dataGenerator.generateAndSaveData(count);
        return ResponseEntity.ok("랜덤 데이터 생성 및 저장 완료: " + count + "개");
    }
}
