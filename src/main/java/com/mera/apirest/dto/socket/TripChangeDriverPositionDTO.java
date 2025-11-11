package com.mera.apirest.dto.socket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TripChangeDriverPositionDTO {

    private double lat;
    private double lng;

    @JsonProperty("id_socket")
    private String idSocket;

    @JsonProperty("id_client")
    private Long idClient;

}
