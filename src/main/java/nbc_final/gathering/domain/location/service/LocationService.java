package nbc_final.gathering.domain.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.common.exception.ResponseCode;
import nbc_final.gathering.common.exception.ResponseCodeException;
import nbc_final.gathering.domain.location.dto.request.RecommandRequestDto;
import nbc_final.gathering.domain.location.dto.response.CoordinateDto;
import nbc_final.gathering.domain.location.dto.response.PlaceDto;
import nbc_final.gathering.domain.location.dto.response.RecommandResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

  @Value("${GEOCODE_URL}")
  private String GEOCODE_URL;

  @Value("${API_KEY}")
  private String API_KEY;

  @Value("${PLACES_URL}")
  private String PLACES_URL;  // Places API URL (예시로 추가)

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final StringRedisTemplate redisTemplate;

  // 주변 장소 추천 로직
  public RecommandResponseDto getNearbyPlacesFromAddress(RecommandRequestDto recommandRequestDto) {
    String address = recommandRequestDto.getAddress();
    CoordinateDto coordinates = getCoordinateDto(address);

    int radius = recommandRequestDto.getRadius();
    String type = recommandRequestDto.getType();


    return getNearbyPlaces(coordinates, radius, type);
  }

  // 주소를 바탕으로 위도, 경도 추출 메서드
  private CoordinateDto getCoordinateDto(String address) {
    // ex) 서울특별시%20강남구%20서초동
    String encodedAddress = address.replace(" ", "%20");
    // GeoCoding Api 호출 Url
    String url = String.format("%s?address=%s&key=%s", GEOCODE_URL, encodedAddress, API_KEY);

    // 캐시있는지 불러오기
    String cachedData = redisTemplate.opsForValue().get(encodedAddress);
    // 캐시된 데이터가 있다면
    if (cachedData != null) {
      // 역직렬화
      try {
        return objectMapper.readValue(cachedData, CoordinateDto.class);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // 캐시된 데이터가 없으면

    // 외부 api 호출
    try {
      String response = restTemplate.getForObject(url, String.class);
      JsonNode jsonNode = objectMapper.readTree(response);

      JsonNode results = jsonNode.path("results");

      // results 배열이 비어있는지 확인
      if (!results.isArray() || results.size() == 0) {
        throw new RuntimeException("Geocoding API 호출 실패: 주소에 해당하는 좌표를 찾을 수 없습니다.");
      }
      // 위도, 경도 추출
      JsonNode location = results.get(0).path("geometry").path("location");
      double latitude = location.path("lat").asDouble(0.0);
      double longitude = location.path("lng").asDouble(0.0);

      if (latitude == 0.0 && longitude == 0.0) {
        throw new RuntimeException("좌표 정보를 가져올 수 없습니다.");
      }

      CoordinateDto coordinateDto = new CoordinateDto(latitude, longitude);

      // 직렬화 & 캐싱
      String jsonData = objectMapper.writeValueAsString(coordinateDto);
      redisTemplate.opsForValue().set(encodedAddress, jsonData, 365, TimeUnit.DAYS);

      return coordinateDto;
    } catch (Exception e) {
      throw new RuntimeException("Geocoding API 호출 실패: " + e.getMessage());
    }
  }


  // 위도, 경도를 바탕으로 주변 장소를 추천
  private RecommandResponseDto getNearbyPlaces(CoordinateDto coordinateDto, int requestRadius, String requestType) {
    double latitude = coordinateDto.getLatitude();
    double longitude = coordinateDto.getLongitude();
    // Place Api 호출 Url
    String url = String.format("%s?location=%f,%f&radius=%d&type=%s&key=%s",
        PLACES_URL, latitude, longitude, requestRadius, requestType, API_KEY);

    // 좌표로 장소 캐시있는지 불러오기
    // 직렬화
    try {
      // 좌표와 타입, 반경을 기반으로 String 형태로 바꾸기
      PlaceDto placeDto = new PlaceDto(latitude, longitude, requestRadius ,requestType);
      String jsonCoordinateData = objectMapper.writeValueAsString(placeDto);
      String cachedData = redisTemplate.opsForValue().get(jsonCoordinateData);

      // 캐시된 좌표가 있다면
      if (cachedData != null) {
        // 역직렬화
        try {
          return objectMapper.readValue(cachedData, RecommandResponseDto.class);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // 캐시된 데이터가 없으면

      // 외부 api 호출
      // Place Api 결과 가져오기
      String response = restTemplate.getForObject(url, String.class);
      List<PlaceDto> places = new ArrayList<>();

      JsonNode jsonNode = objectMapper.readTree(response);
      JsonNode results = jsonNode.path("results");

      if (results.isArray()) {
        for (JsonNode placeNode : results) {
          String name = placeNode.path("name").asText("");
          String address = placeNode.path("vicinity").asText("");
          double placeLatitude = placeNode.path("geometry").path("location").path("lat").asDouble(0.0);
          double placeLongitude = placeNode.path("geometry").path("location").path("lng").asDouble(0.0);
          String placeId = placeNode.path("place_id").asText("");
          JsonNode types = placeNode.path("types");
          String actualType = (types.isArray() && types.size() > 0) ? types.get(0).asText() : "";  // 첫 번째 타입만 사용

          if (actualType.equals(requestType) && !name.isEmpty() && placeLatitude != 0.0 && placeLongitude != 0.0) {
            PlaceDto place = new PlaceDto(name, address, placeLatitude, placeLongitude, placeId, actualType);
            places.add(place);
          }
        }

        // 장소가 없을 경우 예외처리
        if (places.isEmpty()) {
          throw new ResponseCodeException(ResponseCode.NOT_FOUND_LOCATION);
        }
        // 최대 5개로 표시제한
        if (places.size() >= 5) {
          places = places.subList(0, 5);
        }
      }

      RecommandResponseDto recommandResponseDto = new RecommandResponseDto(places);

      // 직렬화 & 캐싱
      String jsonLocationsData = objectMapper.writeValueAsString(recommandResponseDto);
      redisTemplate.opsForValue().set(jsonCoordinateData, jsonLocationsData, 7, TimeUnit.DAYS);

      return recommandResponseDto;

    } catch (Exception e) {
      throw new RuntimeException("Places API 호출 실패: " + e.getMessage());
    }
  }


}
