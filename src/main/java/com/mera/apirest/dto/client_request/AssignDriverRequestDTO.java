package com.mera.apirest.dto.client_request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AssignDriverRequestDTO {

    private Long id;
    @JsonProperty("id_driver_assigned")
    private Long idDriverAssigned;

    @JsonProperty("fare_assigned")
    private double fareAssigned;

}

