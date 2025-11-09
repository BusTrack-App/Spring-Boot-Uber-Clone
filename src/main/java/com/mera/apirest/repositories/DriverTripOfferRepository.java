package com.mera.apirest.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mera.apirest.config.APIConfig;
import com.mera.apirest.dto.driver_trip_offer.DriverTripOfferRequest;
import com.mera.apirest.dto.driver_trip_offer.DriverTripOfferResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DriverTripOfferRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper; // Inyectado para reutilizar

    public Long insertDriverTripOffer(DriverTripOfferRequest request) {
        String sql = """
            INSERT INTO driver_trip_offers(
                id_driver,
                id_client_request,
                fare_offered,
                time,
                distance,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, ?, ?, NOW(), NOW())
            """;

        jdbcTemplate.update(sql,
                request.getIdDriver(),
                request.getIdClientRequest(),
                request.getFareOffered(),
                request.getTime(),
                request.getDistance()
        );
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public List<DriverTripOfferResponse> findByClientRequest(Long idClientRequest) {
        String sql = """
        SELECT
            DTO.id,
            DTO.id_driver,
            DTO.id_client_request,
            DTO.fare_offered,
            DTO.time,
            DTO.distance,
            DTO.created_at,
            DTO.updated_at,
            JSON_OBJECT(
                'name', U.name,
                'lastname', U.lastname,
                'phone', U.phone,
                'image', U.image
            ) AS driver
        FROM
            driver_trip_offers AS DTO
        INNER JOIN
            users AS U ON U.id = DTO.id_driver
        WHERE
            DTO.id_client_request = ?
        """;

        return jdbcTemplate.query(sql, new Object[]{idClientRequest}, (rs, rowNum) -> {
            DriverTripOfferResponse response = new DriverTripOfferResponse();

            response.setId(rs.getLong("id"));
            response.setIdDriver(rs.getLong("id_driver"));
            response.setIdClientRequest(rs.getLong("id_client_request"));
            response.setFareOffered(rs.getDouble("fare_offered"));
            response.setTime(rs.getDouble("time"));
            response.setDistance(rs.getDouble("distance"));
            response.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            response.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            String driverJson = rs.getString("driver");
            try {
                DriverTripOfferResponse.DriverInfoDTO driverInfo =
                        objectMapper.readValue(driverJson, DriverTripOfferResponse.DriverInfoDTO.class);

                if (driverInfo.getImage() != null) {
                    driverInfo.setImage(APIConfig.BASE_URL + driverInfo.getImage());
                }
                response.setDriver(driverInfo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error deserializando información del conductor", e);
            }

            return response;
        });
    }
}