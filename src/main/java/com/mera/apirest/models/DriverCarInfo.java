package com.mera.apirest.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "driver_car_info")
@Data
public class DriverCarInfo {

    @Id
    @Column(name = "id_driver")
    private Long idDriver;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_driver", referencedColumnName = "id")
    private User driver;

    @Column(name = "brand", length = 50, nullable = false)
    private String brand;

    @Column(name = "color", length = 50, nullable = false)
    private String color;

    @Column(name = "plate", length = 50, nullable = false)
    private String plate;

}
