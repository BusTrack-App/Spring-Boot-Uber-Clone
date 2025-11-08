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

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class DriverPositionService {

    @Autowired
    private DriverPositionRepository driverPositionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public DriverPositionResponse create(DriverPositionRequest request) {
        User user = userRepository.findById(request.getIdDriver()).orElseThrow(
                () -> new RuntimeException("Usuario no encontrado")
        );

        String sql = """
            REPLACE INTO drivers_position(id_driver, position)
            VALUES(?, ST_GeomFromText(?, 4326))
        """;
        String point = String.format("POINT(%s %s)", request.getLng(), request.getLat());
        jdbcTemplate.update(sql, request.getIdDriver(), point);

        return new DriverPositionResponse(request.getIdDriver(), request.getLat(), request.getLng());
    }

    @Transactional
    public DriverPositionResponse getDriverPosition(Long idDriver) {
        List<Object[]> result = driverPositionRepository.findDriverPosition(idDriver);
        if (result.isEmpty()) {
            throw new RuntimeException("El conductor no existe");
        }

        Object[] row = result.get(0);
        Long id = ((Number) row[0]).longValue();
        String positionText = (String) row[1];
        Pattern pattern = Pattern.compile("POINT\\(([-\\d.]+) ([-\\d.]+)\\)");
        Matcher matcher = pattern.matcher(positionText);

        if (matcher.matches()) {
            double lng = Double.parseDouble(matcher.group(1)); // X = lng
            double lat = Double.parseDouble(matcher.group(2)); // Y = lat
            return new DriverPositionResponse(id, lat, lng);
        }
        else {
            throw new RuntimeException("Error al obtener la posicion");
        }
    }


}

