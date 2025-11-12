package com.mera.apirest.dto.client_request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestResponse {

    private Long id;

    @JsonProperty("id_client")
    private Long idClient;

    @JsonProperty("id_driver_assigned")
    private Long idDriverAssigned;

    @JsonProperty("pickup_description")
    private String pickupDescription;

    @JsonProperty("destination_description")
    private String destinationDescription;

    @JsonProperty("fare_offered")
    private double fareOffered;

    @JsonProperty("fare_assigned")
    private double fareAssigned;

    @JsonProperty("client_rating")
    private double clientRating;

    @JsonProperty("driver_rating")
    private double driverRating;

    private String status;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private ClientInfoDTO client;
    private DriverInfoDTO driver;
    private CarInfoDTO car;

    @JsonProperty("pickup_position")
    private CoordinatesDTO pickupPosition;

    @JsonProperty("destination_position")
    private CoordinatesDTO destinationPosition;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfoDTO {
        private String name;
        private String lastname;
        private String phone;
        private String image;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverInfoDTO {
        private String name;
        private String lastname;
        private String phone;
        private String image;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarInfoDTO {
        private String brand;
        private String color;
        private String plate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesDTO {
        private double x;
        private double y;
    }



}
