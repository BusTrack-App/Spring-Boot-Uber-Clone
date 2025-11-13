package com.mera.apirest.dto.client_request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateDriverRatingDTO {

    private Long id;
    @JsonProperty("driver_rating")
    private double driverRating;

}
