package com.mera.apirest.dto.driver_car_info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DriverCarInfoDTO {

    @JsonProperty("id_driver")
    private Long idDriver;

    private String brand;
    private String color;
    private String plate;

}

