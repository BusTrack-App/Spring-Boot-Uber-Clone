package com.mera.apirest.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mera.apirest.dto.client_request.DistanceMatrixResponse;
import com.mera.apirest.models.TimeAndDistanceValues;
import com.mera.apirest.repositories.TimeAndDistanceValuesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class ClientRequestService {

    @Value("${google.api.key}")
    private String googleApiKey;

    @Autowired
    private TimeAndDistanceValuesRepository timeAndDistanceValuesRepository;

    public DistanceMatrixResponse getTimeAndDistance(double originLat, double originLng, double destinationLat, double destinationLng) {

        TimeAndDistanceValues values = timeAndDistanceValuesRepository.findById(1L).orElseThrow(
                () -> new RuntimeException("Los precios no han sido establecidos")
        );

        String origins = originLat + "," + originLng;
        String destinations = destinationLat + "," + destinationLng;

        URI uri = UriComponentsBuilder
                .fromUriString("https://maps.googleapis.com/maps/api/distancematrix/json")
                .queryParam("origins", origins)
                .queryParam("destinations", destinations)
                .queryParam("units", "metric")
                .queryParam("key", googleApiKey)
                .build(true)
                .toUri();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al conectarse al API de Google: " + response.getStatusCode());
        }

        JsonNode body = response.getBody();
        if (!body.get("status").asText().equals("OK")) {
            throw new RuntimeException("Respuesta invalida del API de Google: " + body.get("status").asText());
        }
        JsonNode element = body.get("rows").get(0).get("elements").get(0);

        String distanceText = element.get("distance").get("text").asText();
        double distanceValue = element.get("distance").get("value").asDouble();

        String durationText = element.get("duration").get("text").asText();
        double durationValue = element.get("duration").get("value").asDouble();

        double km = distanceValue / 1000;
        double minutes = durationValue / 60;
        double recommededValue = values.getKmValue() * km + values.getMinValue() * minutes;

        DistanceMatrixResponse responseDTO = new DistanceMatrixResponse();
        responseDTO.setOriginAddresses(body.get("origin_addresses").get(0).asText());
        responseDTO.setDestinationAddresses(body.get("destination_addresses").get(0).asText());

        DistanceMatrixResponse.Duration duration = new DistanceMatrixResponse.Duration();
        duration.setText(durationText);
        duration.setValue(durationValue);

        DistanceMatrixResponse.Distance distance = new DistanceMatrixResponse.Distance();
        distance.setText(distanceText);
        distance.setValue(distanceValue);

        responseDTO.setDistance(distance);
        responseDTO.setDuration(duration);
        responseDTO.setRecommendedValue(String.format("%.2f", recommededValue) );

        return responseDTO;
    }


}
