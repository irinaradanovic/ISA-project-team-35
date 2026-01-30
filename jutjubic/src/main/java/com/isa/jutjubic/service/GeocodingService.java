package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.VideoPostUploadDto;
import com.isa.jutjubic.model.GeoLocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class GeocodingService {
    @Value("${locationiq.api.key}")
    private String apiKey;

    public void fillLocationData(VideoPostUploadDto dto, GeoLocation location) {
        String query = dto.getAddress();
        if (query == null || query.isBlank()) return;

        String url = "https://us1.locationiq.com/v1/search.php?key=" + apiKey +
                "&q=" + query + "&format=json";

        try {
            RestTemplate restTemplate = new RestTemplate();
            // LocationIQ vraća niz rezultata, uzimamo prvi (najrelevantniji)
            List<Map<String, Object>> results = restTemplate.getForObject(url, List.class);

            if (results != null && !results.isEmpty()) {
                Map<String, Object> first = results.get(0);
                location.setLatitude(Double.parseDouble(first.get("lat").toString()));
                location.setLongitude(Double.parseDouble(first.get("lon").toString()));
                location.setAddress(query);

                // LocationIQ vraca "display_name"
                location.setCity(extractCity(first.get("display_name").toString()));
            }
        } catch (Exception e) {
            System.err.println("Geocoding failed for: " + query);
        }
    }

    private String extractCity(String displayName) {
        // izvlacenje prvog dela adrese
        return displayName.split(",")[0].trim();
    }
}