package nbc_final.gathering.domain.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nbc_final.gathering.domain.location.dto.request.RecommandRequestDto;
import nbc_final.gathering.domain.location.dto.response.CoordinatesDto;
import nbc_final.gathering.domain.location.dto.response.PlaceDto;
import nbc_final.gathering.domain.location.dto.response.RecommandResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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

  // 주변 장소 추천 로직
  public RecommandResponseDto getNearbyPlacesFromAddress(RecommandRequestDto recommandRequestDto) {
    String address = recommandRequestDto.getAddress();
    CoordinatesDto coordinates = getCoordinatesFromAddress(address);

    int radius = recommandRequestDto.getRadius();
    String type = recommandRequestDto.getType();


    return getNearbyPlaces(coordinates, radius, type);
  }

  // 주소를 바탕으로 위도, 경도 추출 메서드
  private CoordinatesDto getCoordinatesFromAddress(String address) {
    try {
      String encodedAddress = address.replace(" ", "%20");
      String url = String.format("%s?address=%s&key=%s", GEOCODE_URL, encodedAddress, API_KEY);

      String response = restTemplate.getForObject(url, String.class);
      JsonNode jsonNode = objectMapper.readTree(response);

      JsonNode results = jsonNode.path("results");

      // results 배열이 비어있는지 확인
      if (!results.isArray() || results.size() == 0) {
        throw new RuntimeException("Geocoding API 호출 실패: 주소에 해당하는 좌표를 찾을 수 없습니다.");
      }

      JsonNode location = results.get(0).path("geometry").path("location");
      double latitude = location.path("lat").asDouble(0.0);
      double longitude = location.path("lng").asDouble(0.0);

      if (latitude == 0.0 && longitude == 0.0) {
        throw new RuntimeException("좌표 정보를 가져올 수 없습니다.");
      }

      return new CoordinatesDto(latitude, longitude);

    } catch (Exception e) {
      throw new RuntimeException("Geocoding API 호출 실패: " + e.getMessage());
    }
  }


  // 위도, 경도를 바탕으로 주변 장소를 추천
  private RecommandResponseDto getNearbyPlaces(CoordinatesDto coordinatesDto, int requestRadius, String requestType) {
    double latitude = coordinatesDto.getLatitude();
    double longitude = coordinatesDto.getLongitude();

    String url = String.format("%s?location=%f,%f&radius=%d&type=%s&key=%s",
        PLACES_URL, latitude, longitude, requestRadius, requestType, API_KEY);

    String response = restTemplate.getForObject(url, String.class);
    List<PlaceDto> places = new ArrayList<>();

    try {
      JsonNode jsonNode = objectMapper.readTree(response);
      JsonNode results = jsonNode.path("results");

      if (results.isArray()) {
        for (JsonNode placeNode : results) {
          String name = placeNode.path("name").asText("");
          String address = placeNode.path("vicinity").asText("");
          double placeLatitude = placeNode.path("geometry").path("location").path("lat").asDouble(0.0);
          double placeLongitude = placeNode.path("geometry").path("location").path("lng").asDouble(0.0);
          String placeId = placeNode.path("place_id").asText("");
          String type = placeNode.path("types").get(0).asText("");

          if (!name.isEmpty() && placeLatitude != 0.0 && placeLongitude != 0.0) {
            PlaceDto place = new PlaceDto(name, address, placeLatitude, placeLongitude, placeId, requestType);
            places.add(place);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Places API 호출 실패: " + e.getMessage());
    }

    return new RecommandResponseDto(places);
  }
}
