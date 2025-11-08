package com.mera.apirest.services;

import com.mera.apirest.dto.driver_position.DriverPositionRequest;
import com.mera.apirest.dto.driver_position.DriverPositionResponse;
import com.mera.apirest.models.DriverPosition;
import com.mera.apirest.models.User;
import com.mera.apirest.repositories.DriverPositionRepository;
import com.mera.apirest.repositories.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class DriverPositionService {

    @Autowired
    private DriverPositionRepository driverPositionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public DriverPosition create(DriverPositionRequest request) {
        User user = userRepository.findById(request.getIdDriver()).orElseThrow(
                () -> new RuntimeException("Usuario no encontrado")
        );
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = geometryFactory.createPoint(new Coordinate(request.getLng(), request.getLat()));
        point.setSRID(4326);

        Optional<DriverPosition> existingPosition = driverPositionRepository.findById(request.getIdDriver());

        DriverPosition driverPosition = new DriverPosition();

        if (existingPosition.isPresent()){
            driverPosition = existingPosition.get();
            driverPosition.setPosition(point);

        } else {
            driverPosition.setUser(user);
            driverPosition.setPosition(point);
        }

        return  driverPositionRepository.save(driverPosition);

    }


}

