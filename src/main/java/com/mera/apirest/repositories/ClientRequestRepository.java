package com.mera.apirest.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ClientRequestRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${google.api.key}")
    private String googleApiKey;

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

}
