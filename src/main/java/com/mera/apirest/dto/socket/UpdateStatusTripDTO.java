package com.mera.apirest.dto.socket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateStatusTripDTO {

    private String status;

    @JsonProperty("id_socket")
    private String idSocket;

    @JsonProperty("id_client_request")
    private Long idClientRequest;

}
