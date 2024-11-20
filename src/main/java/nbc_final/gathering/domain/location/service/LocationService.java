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

    private static final String LOCK_PREFIX = "lock:";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    @Value("${GEOCODE_URL}")
    private String GEOCODE_URL;
    @Value("${API_KEY}")
    private String API_KEY;
    @Value("${PLACES_URL}")
    private String PLACES_URL;

    // 주변 장소 추천 로직
    public RecommandResponseDto getNearbyPlacesFromAddress(RecommandRequestDto recommandRequestDto) {
        String address = recommandRequestDto.getAddress();
        CoordinateDto coordinates = getCoordinateDto(address);

        int radius = recommandRequestDto.getRadius();
        String type = recommandRequestDto.getType();

        return getNearbyPlacesCache(coordinates, radius, type);
    }

    // 주소를 바탕으로 위도, 경도 추출 메서드
    private CoordinateDto getCoordinateDto(String address) {
        String encodedAddress = address.replace(" ", "%20");
        String url = String.format("%s?address=%s&key=%s", GEOCODE_URL, encodedAddress, API_KEY);

        // 캐시된 데이터가 있다면
        String cachedData = redisTemplate.opsForValue().get(encodedAddress);
        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData, CoordinateDto.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 캐시가 없으면, 락을 걸고 진행
        String lockKey = LOCK_PREFIX + encodedAddress;
        boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 5, TimeUnit.SECONDS); // 10초 락 설정
        if (locked) {
            try {
                // 외부 API 호출
                return fetchCoordinatesFromAPI(url, encodedAddress);
            } finally {
                // 작업 후 락 해제
                redisTemplate.delete(lockKey);
            }
        }

        // 락을 얻지 못했을 경우, 다른 스레드가 작업을 진행하도록 기다림
        try {
            Thread.sleep(100);
            return getCoordinateDto(address);
        } catch (InterruptedException e) {
            throw new RuntimeException("잠시 후 다시 시도해주세요.");
        }
    }

    private CoordinateDto fetchCoordinatesFromAPI(String url, String encodedAddress) {
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode results = jsonNode.path("results");

            if (!results.isArray() || results.size() == 0) {
                throw new RuntimeException("Geocoding API 호출 실패: 주소에 해당하는 좌표를 찾을 수 없습니다.");
            }

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
    private RecommandResponseDto getNearbyPlacesCache(CoordinateDto coordinateDto, int requestRadius, String requestType) {
        double latitude = coordinateDto.getLatitude();
        double longitude = coordinateDto.getLongitude();
        String url = String.format("%s?location=%f,%f&radius=%d&type=%s&key=%s"
                , PLACES_URL, latitude, longitude, requestRadius, requestType, API_KEY);

        String cacheKey = String.format("places:%f:%f:%d:%s", latitude, longitude, requestRadius, requestType);
        String cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData, RecommandResponseDto.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String lockKey = LOCK_PREFIX + cacheKey;
        boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 5, TimeUnit.SECONDS);
        if (locked) {
            try {
                return getNearbyPlacesFromAPI(url, cacheKey, requestType);
            } finally {
                redisTemplate.delete(lockKey);
            }
        }

        // 다른 스레드가 작업을 진행할 때까지 대기 후 재시도
        try {
            Thread.sleep(100);
            return getNearbyPlacesCache(coordinateDto, requestRadius, requestType); // 재귀 호출
        } catch (InterruptedException e) {
            throw new RuntimeException("잠시 후 다시 시도해주세요.");
        }
    }

    private RecommandResponseDto getNearbyPlacesFromAPI(String url, String cacheKey, String requestType) {
        try {
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
                    String actualType = (types.isArray() && types.size() > 0) ? types.get(0).asText() : "";

                    if (actualType.equals(requestType) && !name.isEmpty() && placeLatitude != 0.0 && placeLongitude != 0.0) {
                        PlaceDto place = new PlaceDto(name, address, placeLatitude, placeLongitude, placeId, actualType);
                        places.add(place);
                    }
                }

                if (places.isEmpty()) {
                    throw new ResponseCodeException(ResponseCode.NOT_FOUND_LOCATION);
                }

                if (places.size() >= 5) {
                    places = places.subList(0, 5);
                }
            }

            RecommandResponseDto recommandResponseDto = new RecommandResponseDto(places);

            // 직렬화 & 캐싱
            String jsonLocationsData = objectMapper.writeValueAsString(recommandResponseDto);
            redisTemplate.opsForValue().set(cacheKey, jsonLocationsData, 7, TimeUnit.DAYS);

            return recommandResponseDto;
        } catch (Exception e) {
            throw new RuntimeException("Places API 호출 실패: " + e.getMessage());
        }
    }
}
