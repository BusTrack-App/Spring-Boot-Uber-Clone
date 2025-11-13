package com.mera.apirest.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mera.apirest.config.APIConfig;
import com.mera.apirest.dto.client_request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ClientRequestRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${google.api.key}")
    private String googleApiKey;

    public boolean findById(Long id) {
        String sql = "SELECT COUNT(*) FROM client_requests WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    // Crea una solicitud de viaje
    public Long insertClientRequest(
            Long idClient,
            double fareOffered,
            double pickupLat,
            double pickupLng,
            double destinationLat,
            double destinationLng,
            String pickupDescription,
            String destinationDescription
    ) {
        String sql = """
        INSERT INTO client_requests(
            id_client,
            fare_offered,
            pickup_description,
            destination_description,
            pickup_position,
            destination_position,
            status,
            created_at,
            updated_at
        )
        VALUES(
            ?,
            ?,
            ?,
            ?,
            ST_GeomFromText(CONCAT('POINT(', ?, ' ', ?, ')'), 4326),
            ST_GeomFromText(CONCAT('POINT(', ?, ' ', ?, ')'), 4326),
            'CREATED',
            NOW(),
            NOW()
        )
        """;

        jdbcTemplate.update(sql,
                idClient,
                fareOffered,
                pickupDescription,
                destinationDescription,
                pickupLng, pickupLat,
                destinationLng, destinationLat
        );

        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class); // ID
    }


    // Obtiene las solicitudes de viajes cercanas
    public List<NearbyClientRequestResponse> findNearbyClientRequest(double driverLat, double driverLng) {
        String sql = """
                SELECT
                	CR.id,
                    CR.id_client,
                    CR.fare_offered,
                    CR.pickup_description,
                    CR.destination_description,
                    CR.status,
                    CR.updated_at,
                    JSON_OBJECT(
                		"x", ST_X(pickup_position),
                        "y", ST_Y(pickup_position)
                    ) AS pickup_position,
                    JSON_OBJECT(
                		"x", ST_X(destination_position),
                        "y", ST_Y(destination_position)
                    ) AS destination_position,
                    ST_Distance_Sphere(pickup_position, ST_GeomFromText(CONCAT('POINT(', ?, ' ', ?, ')'), 4326)) AS distance,
                    timestampdiff(MINUTE, CR.updated_at, NOW()) AS time_difference,
                    JSON_OBJECT(
                		"name", U.name,
                        "lastname", U.lastname,
                        "phone", U.phone,
                        "image", U.image
                    ) AS client
                FROM
                	client_requests AS CR
                INNER JOIN
                	users AS U
                ON
                	U.id = CR.id_client
                WHERE
                	timestampdiff(MINUTE, CR.updated_at, NOW()) < 1000 AND status = "CREATED"
                HAVING
                	distance < 20000
        """;

        List<NearbyClientRequestResponse> data = jdbcTemplate.query(sql, new Object[]{driverLng, driverLat}, (result, rowNumber) -> {
            ObjectMapper mapper = new ObjectMapper();

            try {
                NearbyClientRequestResponse.ClientInfoDTO client = mapper.readValue(result.getString("client"), NearbyClientRequestResponse.ClientInfoDTO.class);
                client.setImage(APIConfig.BASE_URL + client.getImage());

                NearbyClientRequestResponse.CoordinatesDTO pickupPosition = mapper.readValue(result.getString("pickup_position"), NearbyClientRequestResponse.CoordinatesDTO.class);
                NearbyClientRequestResponse.CoordinatesDTO destinationPosition = mapper.readValue(result.getString("destination_position"), NearbyClientRequestResponse.CoordinatesDTO.class);

                NearbyClientRequestResponse response = new NearbyClientRequestResponse();
                response.setId(result.getLong("id"));
                response.setIdClient(result.getLong("id_client"));
                response.setFareOffered(result.getDouble("fare_offered"));
                response.setPickupDescription(result.getString("pickup_description"));
                response.setDestinationDescription(result.getString("destination_description"));
                response.setStatus(result.getString("status"));
                response.setDistance(result.getDouble("distance"));
                response.setTimeDifference(result.getInt("time_difference"));
                response.setUpdatedAt(result.getTimestamp("updated_at").toLocalDateTime());
                response.setClient(client);
                response.setPickupPosition(pickupPosition);
                response.setDestinationPosition(destinationPosition);
                return response;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error al deseralizar JSON NearbyClientRequestResponse", e);
            }


        });
        if (!data.isEmpty()) {
            try {
                String origins = driverLat + "," + driverLng;
                String destinations = data.stream()
                        .map(d -> d.getPickupPosition().getY() + "," + d.getPickupPosition().getX())
                        .collect(Collectors.joining("|"));

                URI uri = UriComponentsBuilder
                        .fromUriString("https://maps.googleapis.com/maps/api/distancematrix/json")
                        .queryParam("origins", origins)
                        .queryParam("destinations", destinations)
                        .queryParam("units", "metric")
                        .queryParam("mode", "driving")
                        .queryParam("key", googleApiKey)
                        .build()
                        .toUri();
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);
                JsonNode elements = response.getBody().get("rows").get(0).get("elements");

                for (int i = 0; i < data.size(); i++) {
                    JsonNode element = elements.get(i);
                    NearbyClientRequestResponse.GoogleDistanceMatrixDTO googleDTO = new NearbyClientRequestResponse.GoogleDistanceMatrixDTO();
                    googleDTO.setStatus(element.get("status").asText());

                    if (element.has("distance") && !element.get("distance").isNull()) {
                        googleDTO.setDistance(new NearbyClientRequestResponse.GoogleDistanceMatrixDTO.ValueText(
                                element.get("distance").get("text").asText(),
                                element.get("distance").get("value").asDouble()
                        ));
                    }

                    if (element.has("duration") && !element.get("duration").isNull()) {
                        googleDTO.setDuration(new NearbyClientRequestResponse.GoogleDistanceMatrixDTO.ValueText(
                                element.get("duration").get("text").asText(),
                                element.get("duration").get("value").asDouble()
                        ));
                    }

                    data.get(i).setGoogleDistanceMatrix(googleDTO);
                }

            } catch (Exception e) {
                throw new RuntimeException("Error al obtener datos del API Google Distance Matrix");
            }
        }
        return data;
    }

    public ClientRequestResponse getByClientRequest(Long id) {
        String sql = """
                SELECT
                	CR.id,
                    CR.id_client,
                    CR.id_driver_assigned,
                    CR.fare_offered,
                    CR.fare_assigned,
                    CR.pickup_description,
                    CR.destination_description,
                    CR.status,
                    CR.updated_at,
                    JSON_OBJECT(
                		"x", ST_X(pickup_position),
                        "y", ST_Y(pickup_position)
                    ) AS pickup_position,
                    JSON_OBJECT(
                		"x", ST_X(destination_position),
                        "y", ST_Y(destination_position)
                    ) AS destination_position,
                    JSON_OBJECT(
                		"name", U.name,
                        "lastname", U.lastname,
                        "phone", U.phone,
                        "image", U.image
                    ) AS client,
                    JSON_OBJECT(
                		"name", D.name,
                        "lastname", D.lastname,
                        "phone", D.phone,
                        "image", D.image
                    ) AS driver,
                    JSON_OBJECT(
                		"brand", DCI.brand,
                        "color", DCI.color,
                        "plate", DCI.plate
                    ) AS car
                FROM
                	client_requests AS CR
                INNER JOIN
                	users AS U
                ON
                	U.id = CR.id_client
                LEFT JOIN
                    users AS D
                ON
                    D.id = CR.id_driver_assigned
                LEFT JOIN
                    driver_car_info AS DCI
                ON
                    DCI.id_driver = CR.id_driver_assigned
                WHERE
                	CR.id = ? AND CR.status = 'ACCEPTED'
        """;

        List<ClientRequestResponse> data = jdbcTemplate.query(sql, new Object[]{id}, (result, rowNumber) -> {
            ObjectMapper mapper = new ObjectMapper();

            try {
                ClientRequestResponse.ClientInfoDTO client = mapper.readValue(result.getString("client"), ClientRequestResponse.ClientInfoDTO.class);
                client.setImage(APIConfig.BASE_URL + client.getImage());

                ClientRequestResponse.DriverInfoDTO driver = mapper.readValue(result.getString("driver"), ClientRequestResponse.DriverInfoDTO.class);
                driver.setImage(APIConfig.BASE_URL + driver.getImage());

                ClientRequestResponse.CarInfoDTO car = mapper.readValue(result.getString("car"), ClientRequestResponse.CarInfoDTO.class);

                ClientRequestResponse.CoordinatesDTO pickupPosition = mapper.readValue(result.getString("pickup_position"), ClientRequestResponse.CoordinatesDTO.class);
                ClientRequestResponse.CoordinatesDTO destinationPosition = mapper.readValue(result.getString("destination_position"), ClientRequestResponse.CoordinatesDTO.class);

                ClientRequestResponse response = new ClientRequestResponse();
                response.setId(result.getLong("id"));
                response.setIdClient(result.getLong("id_client"));
                response.setIdDriverAssigned(result.getLong("id_driver_assigned"));
                response.setFareOffered(result.getDouble("fare_offered"));
                response.setFareAssigned(result.getDouble("fare_assigned"));
                response.setPickupDescription(result.getString("pickup_description"));
                response.setDestinationDescription(result.getString("destination_description"));
                response.setStatus(result.getString("status"));

                response.setUpdatedAt(result.getTimestamp("updated_at").toLocalDateTime());
                response.setClient(client);
                response.setDriver(driver);
                response.setCar(car);
                response.setPickupPosition(pickupPosition);
                response.setDestinationPosition(destinationPosition);

                return response;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error al deseralizar JSON NearbyClientRequestResponse", e);
            }


        });

        return data.get(0);
    }

    public boolean updateDriverAssigned(AssignDriverRequestDTO request) {
        String sql = """
            UPDATE
                client_requests
            SET
                id_driver_assigned = ?,
                status = 'ACCEPTED',
                updated_at = NOW(),
                fare_assigned = ?
            WHERE
                id = ?
        """;

        int rowsAffected = jdbcTemplate.update(sql, request.getIdDriverAssigned(), request.getFareAssigned(), request.getId());
        return rowsAffected > 0;
    }

    public boolean updateStatus(UpdateStatusClientRequestDTO request) {
        String sql = """
            UPDATE
                client_requests
            SET
                status = ?,
                updated_at = NOW()
            WHERE
                id = ?
        """;

        int rowsAffected = jdbcTemplate.update(sql, request.getStatus(), request.getId());
        return rowsAffected > 0;
    }


    public boolean updateClientRating(UpdateClientRatingDTO request) {
        String sql = """
            UPDATE 
                client_requests
            SET 
                client_rating = ?,
                updated_at = NOW()
            WHERE
                id = ?
        """;

        int rowsAffected = jdbcTemplate.update(sql, request.getClientRating(), request.getId());
        return rowsAffected > 0;
    }

    public boolean updateDriverRating(UpdateDriverRatingDTO request) {
        String sql = """
            UPDATE 
                client_requests
            SET 
                driver_rating = ?,
                updated_at = NOW()
            WHERE
                id = ?
        """;

        int rowsAffected = jdbcTemplate.update(sql, request.getDriverRating(), request.getId());
        return rowsAffected > 0;
    }


}
