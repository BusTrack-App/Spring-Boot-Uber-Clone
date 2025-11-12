package com.mera.apirest.dto.client_request;

import lombok.Data;

@Data
public class UpdateStatusClientRequestDTO {

    private Long id;
    private String status;

}
