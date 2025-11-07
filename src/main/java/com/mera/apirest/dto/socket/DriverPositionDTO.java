package com.mera.apirest.dto.socket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DriverPositionDTO {

    @JsonProperty("id_socket")
    private String idSocket;
    private int id;
    private double lat;
    private double lng;

}
