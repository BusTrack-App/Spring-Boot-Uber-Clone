package com.mera.apirest.models;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "drivers_position")
@Data
public class DriverPosition {

    @Id
    @Column(name = "id_driver")
    private Long idDriver;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_driver")
    private User user;

    @Column(columnDefinition = "POINT")
    private Point position;

}
