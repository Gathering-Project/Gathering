package nbc_final.gathering.domain.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.domain.location.dto.response.LocationResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

  @Value("${GEOCODE_URL}")
  private String GEOCODE_URL;

  @Value("${API_KEY}")
  private String API_KEY;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public LocationResponseDto getCoordinatesFromAddress(String address) {
    try {
      // 주소를 URL에 맞게 인코딩
      String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
      String url = String.format(GEOCODE_URL, encodedAddress, API_KEY);

      // 구글 Geocoding API 호출
      String response = restTemplate.getForObject(url, String.class);

      // JSON 파싱 후 위도, 경도 추출
      double latitude = extractLatitude(response);
      double longitude = extractLongitude(response);

      return new LocationResponseDto(latitude, longitude);

    } catch (IOException e) {
      // 예외 처리: 잘못된 주소 또는 다른 API 호출 실패 시
      throw new RuntimeException("주소로부터 위도, 경도를 가져오는 데 실패했습니다.", e);
    }
  }

  // 실제 JSON 응답에서 위도 추출
  private double extractLatitude(String response) throws IOException {
    JsonNode rootNode = objectMapper.readTree(response);
    JsonNode locationNode = rootNode.path("results").get(0).path("geometry").path("location");
    return locationNode.path("lat").asDouble();
  }

  // 실제 JSON 응답에서 경도 추출
  private double extractLongitude(String response) throws IOException {
    JsonNode rootNode = objectMapper.readTree(response);
    JsonNode locationNode = rootNode.path("results").get(0).path("geometry").path("location");
    return locationNode.path("lng").asDouble();
  }
}
