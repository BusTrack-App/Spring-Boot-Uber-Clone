package com.mera.apirest.dto.client_request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateClientRatingDTO {

    private Long id;
    @JsonProperty("client_rating")
    private double clientRating;

}

